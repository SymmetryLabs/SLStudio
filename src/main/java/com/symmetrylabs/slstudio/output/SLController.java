package com.symmetrylabs.slstudio.output;

import java.net.Socket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ConnectException;
import java.io.OutputStream;
import java.io.IOException;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.LXOutput;
import heronarts.lx.color.LXColor;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.CubesModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.util.NetworkUtils;

public class SLController extends LXOutput {
    public final String cubeId;
    public final InetAddress host;
    public final boolean isBroadcast;
    public final NetworkDevice networkDevice;

    private Socket socket;
    private DatagramSocket dsocket;
    private OutputStream output;

    final int[] STRIP_ORD = new int[] {
        6, 7, 8,   // white
        9, 10, 11, // red
        0, 1, 2,   // green
        3, 4, 5    // blue
    };

    static final int HEADER_LENGTH = 4;
    static final int BYTES_PER_PIXEL = 3;

    final int numStrips = STRIP_ORD.length;
    int numPixels;
    int contentSizeBytes;
    int packetSizeBytes;
    byte[] packetData;

    private LX lx;

    public SLController(LX lx, NetworkDevice device, String cubeId) {
        this(lx, device, device.ipAddress, cubeId, false);
    }

    public SLController(LX lx, String _host, String _cubeId) {
        this(lx, _host, _cubeId, false);
    }

    public SLController(LX lx, String _host) {
        this(lx, _host, "", true);
    }

    private SLController(LX lx, String host, String cubeId, boolean isBroadcast) {
        this(lx, null, NetworkUtils.ipAddrToInetAddr(host), cubeId, isBroadcast);
    }

    private SLController(LX lx, NetworkDevice networkDevice, InetAddress host, String cubeId, boolean isBroadcast) {
        super(lx);

        this.networkDevice = networkDevice;
        this.host = host;
        this.cubeId = cubeId;
        this.isBroadcast = isBroadcast;
        this.lx = lx;

        enabled.setValue(true);
    }

    private void initPacketData(int numPixels) {
        this.numPixels = numPixels;
        contentSizeBytes = BYTES_PER_PIXEL * numPixels;
        packetSizeBytes = HEADER_LENGTH + contentSizeBytes; // add header length
        packetData = new byte[packetSizeBytes];

        setHeader();
    }

    private void setHeader() {
        packetData[0] = 0; // Channel
        packetData[1] = 0; // Command (Set pixel colors)
        // indices 2,3 = high byte, low byte
        // 3 bytes * 180 pixels = 540 bytes = 0x021C
        packetData[2] = (byte)((contentSizeBytes >> 8) & 0xFF);
        packetData[3] = (byte)((contentSizeBytes >> 0) & 0xFF);
    }

    private void setPixel(int number, int c) {
        //println("number: "+number);
        int offset = 4 + number * 3;

        // Extract individual colors
            int r = c >> 16 & 0xFF;
            int g = c >> 8 & 0xFF;
            int b = c & 0xFF;

            // Repack gamma corrected colors
        packetData[offset + 0] = (byte) SLStudio.redGamma[r];
        packetData[offset + 1] = (byte) SLStudio.greenGamma[g];
        packetData[offset + 2] = (byte) SLStudio.blueGamma[b];
    }

    @Override
    protected void onSend(int[] colors) {
        if (isBroadcast != SLStudio.applet.outputControl.broadcastPacket.isOn())
            return;

        // Create data socket connection if needed
        if (dsocket == null) {
            try {
                dsocket = new DatagramSocket();
                dsocket.connect(new InetSocketAddress(host, 7890));
                //socket.setTcpNoDelay(true);
                // output = socket.getOutputStream();
            }
            catch (ConnectException e) { dispose(); }
            catch (IOException e) { dispose(); }

            if (dsocket == null)
                return;
        }

        // Find the Cube we're outputting to
        // If we're on broadcast, use cube 0 for all cubes, even
        // if that cube isn't modelled yet
        // Use the mac address to find the cube if we have it
        // Otherwise use the cube id
        if (!(lx.model instanceof CubesModel))
            return;

        CubesModel cubesModel = (CubesModel)lx.model;
        CubesModel.Cube cube = null;
        if ((SLStudio.applet.outputControl.testBroadcast.isOn() || isBroadcast) && cubesModel.getCubes().size() > 0) {
            cube = cubesModel.getCubes().get(0);
        } else {
            for (CubesModel.Cube c : cubesModel.getCubes()) {
                if (c.id != null && c.id.equals(cubeId)) {
                    cube = c;
                    break;
                }
            }
        }

        // Initialize packet data base on cube type.
        // If we don't know the cube type, default to
        // using the cube type with the most pixels
        CubesModel.Cube.Type cubeType = cube != null ? cube.type : CubesModel.Cube.CUBE_TYPE_WITH_MOST_PIXELS;
        int numPixels = cubeType.POINTS_PER_CUBE;
        if (packetData == null || packetData.length != numPixels) {
            initPacketData(numPixels);
        }

        // Fill the datagram with pixel data
        // Fill with all black if we don't have cube data
        if (cube != null) {
            for (int stripNum = 0; stripNum < numStrips; stripNum++) {
                int stripId = STRIP_ORD[stripNum];
                Strip strip = cube.getStrips().get(stripId);

                for (int i = 0; i < strip.metrics.numPoints; i++) {
                    LXPoint point = strip.getPoints().get(i);
                    setPixel(stripNum * strip.metrics.numPoints + i, colors[point.index]);
                }
            }
        } else {
            for (int i = 0; i < numPixels; i++) {
                setPixel(i, LXColor.BLACK);
            }
        }

        // Send the cube data to the cube. yay!
        try {
            //println("packetSizeBytes: "+packetSizeBytes);
            dsocket.send(new java.net.DatagramPacket(packetData,packetSizeBytes));}
        catch (Exception e) {dispose();}
    }

    @Override
    public void dispose() {
        if (dsocket != null) {
            System.err.println("Disconnected from OPC server");
        }
        System.err.println("Failed to connect to OPC server " + host);
        socket = null;
        dsocket = null;
    }
}
