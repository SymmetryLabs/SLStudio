package com.symmetrylabs.slstudio.output;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.component.GammaExpander;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import heronarts.lx.LX;
import heronarts.lx.output.LXOutput;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;

public class SLController extends LXOutput {
    public final InetAddress host;
    public final boolean isBroadcast;
    public final NetworkDevice networkDevice;
    public final PointsGrouping points;

    private Socket socket;
    private DatagramSocket dsocket;
    private OutputStream output;

    static final int HEADER_LENGTH = 4;
    static final int BYTES_PER_PIXEL = 3;

    int contentSizeBytes;
    int packetSizeBytes;
    byte[] packetData;

    private final LX lx;
    private GammaExpander GammaExpander;

    public SLController(LX lx, NetworkDevice device, PointsGrouping points) {
        this(lx, device, device.ipAddress, points, false);
    }

    private SLController(LX lx, NetworkDevice networkDevice, InetAddress host, PointsGrouping points, boolean isBroadcast) {
        super(lx);

        this.lx = lx;
        this.networkDevice = networkDevice;
        this.host = host;
        this.points = points;
        this.isBroadcast = isBroadcast;

        GammaExpander = GammaExpander.getInstance(lx);
        initPacketData(points.size());
        enabled.setValue(true);
    }

    private void initPacketData(int numPixels) {
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
        int offset = 4 + number * 3;
        int gammaExpanded = GammaExpander.getExpandedColor(c);
        packetData[offset + 0] = (byte) Ops8.red(gammaExpanded);
        packetData[offset + 1] = (byte) Ops8.green(gammaExpanded);
        packetData[offset + 2] = (byte) Ops8.blue(gammaExpanded);
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

        for (int i = 0; i < points.size(); i++) {
            setPixel(i, colors[points.getPoint(i).index]);
        }

        // Send the data. yay!
        try {
            //println("packetSizeBytes: "+packetSizeBytes);
            dsocket.send(new java.net.DatagramPacket(packetData,packetSizeBytes));
        }
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
