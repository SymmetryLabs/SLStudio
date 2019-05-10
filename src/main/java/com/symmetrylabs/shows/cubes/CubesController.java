package com.symmetrylabs.shows.cubes;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.util.NetworkUtils;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.LXOutput;
import heronarts.lx.output.OPCConstants;
import org.jetbrains.annotations.NotNull;
import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.util.CubeInventory;
import com.symmetrylabs.color.PerceptualColorScale;
import com.symmetrylabs.color.Ops8;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;

public class CubesController extends LXOutput implements Comparable<CubesController>, OPCConstants, CubeInventory.Listener {
    public String id;
    public int idInt;
    public final InetAddress host;
    public final boolean isBroadcast;
    public final NetworkDevice networkDevice;

    private final PerceptualColorScale outputScaler;
    private final CubeInventory inventory;
    private DatagramSocket dsocket;
    private DatagramPacket packet;
    private OutputStream output;
    protected boolean is16BitColorEnabled = false;

    final int[] STRIP_ORD = new int[] {
        0, 1, 2, // red
        3, 4, 5, // green
        6, 7, 8, // blue
        9, 10, 11 // white
    };

    final int numStrips = STRIP_ORD.length;
    int numPixels;
    int contentSizeBytes;
    int packetSizeBytes;
    byte[] packetData;
    boolean sendTestPattern = false;

    private final LX lx;
    private CubesMappingMode mappingMode;

    public CubesController(LX lx, NetworkDevice device, CubeInventory inventory, PerceptualColorScale outputScaler) {
        this(lx, device, device.ipAddress, null, inventory, false, outputScaler);
    }

    public CubesController(LX lx, String _host, String _id) {
        this(lx, _host, _id, false);
    }

    public CubesController(LX lx, String _host) {
        this(lx, _host, "", true);
    }

    private CubesController(LX lx, String host, String id, boolean isBroadcast) {
        this(lx, null, NetworkUtils.toInetAddress(host), id, null, isBroadcast, null);
    }

    private CubesController(LX lx, NetworkDevice networkDevice, InetAddress host, String id, CubeInventory inventory, boolean isBroadcast, PerceptualColorScale outputScaler) {
        super(lx);

        this.lx = lx;
        this.networkDevice = networkDevice;
        this.host = host;
        this.id = id;
        this.inventory = inventory;
        this.outputScaler = outputScaler;
        this.isBroadcast = isBroadcast;

        if (inventory != null) {
            inventory.addListener(this);
            onCubeListUpdated();
        } else {
            onIdUpdated();
        }

        mappingMode = CubesMappingMode.getInstance(lx);

        enabled.setValue(true);
    }

    public String getMacAddress() {
        return networkDevice.deviceId;
    }

    @Override
    public void onCubeListUpdated() {
        String newId = inventory.getControllerId(networkDevice.deviceId);
        if (newId != null && (id == null || !id.equals(newId))) {
            id = newId;
            onIdUpdated();
        }
    }

    private void onIdUpdated() {
        int idInt = Integer.MAX_VALUE;
        try {
            idInt = Integer.parseInt(id);
        } catch (NumberFormatException e) {}
        this.idInt = idInt;
    }

    public void set16BitColorEnabled(boolean enable) {
        this.is16BitColorEnabled = enable;
    }

    private void initPacketData(int numPixels, boolean use16) {
        this.numPixels = numPixels;
        contentSizeBytes = (use16 ? BYTES_PER_16BIT_PIXEL : BYTES_PER_PIXEL) * numPixels;
        packetSizeBytes = HEADER_LEN + contentSizeBytes;
        if (packetData == null || packetData.length != packetSizeBytes) {
            packetData = new byte[packetSizeBytes];
            packet = new DatagramPacket(packetData, packetSizeBytes);
        }
        packetData[0] = 0; // Channel
        packetData[1] = use16 ? COMMAND_SET_16BIT_PIXEL_COLORS : COMMAND_SET_PIXEL_COLORS;
        packetData[2] = (byte) ((contentSizeBytes >> 8) & 0xFF);
        packetData[3] = (byte) (contentSizeBytes & 0xFF);
    }

    private void setPixel(int number, int c) {
        if (outputScaler != null) {
            c = outputScaler.apply8(c);
        }
        int index = 4 + number * 3;
        packetData[index++] = LXColor.red(c);
        packetData[index++] = LXColor.green(c);
        packetData[index++] = LXColor.blue(c);
    }

    private void setPixel(int number, long c) {
        int index = 4 + number * 6;
        if (outputScaler != null) {
            c = outputScaler.apply16(c);
        }
        int red = Ops16.red(c);
        int green = Ops16.green(c);
        int blue = Ops16.blue(c);
        packetData[index++] = (byte) (red >>> 8);
        packetData[index++] = (byte) (red & 0xff);
        packetData[index++] = (byte) (green >>> 8);
        packetData[index++] = (byte) (green & 0xff);
        packetData[index++] = (byte) (blue >>> 8);
        packetData[index++] = (byte) (blue & 0xff);
    }

    @Override
    protected void onSend(PolyBuffer src) {
        if (isBroadcast != ApplicationState.outputControl().broadcastPacket.isOn())
            return;

        // Create data socket connection if needed
        if (dsocket == null) {
            try {
                dsocket = new DatagramSocket();
                dsocket.connect(new InetSocketAddress(host, 7890));
            }
            catch (IOException e) {}
            finally {
                if (dsocket == null) {
                    ApplicationState.setWarning("CubesController", "could not create datagram socket");
                    return;
                }
            }
        }

        // Find the Cube we're outputting to
        // If we're on broadcast, use cube 0 for all cubes, even
        // if that cube isn't modelled yet
        // Use the mac address to find the cube if we have it
        // Otherwise use the cube id
        if (!(lx.model instanceof CubesModel)) {
            ApplicationState.setWarning("CubesController", "model is not a cube model");
            return;
        }

        PointsGrouping points = null;
        CubesModel cubesModel = (CubesModel)lx.model;

        if ((ApplicationState.outputControl().testBroadcast.isOn() || isBroadcast) && cubesModel.getCubes().size() > 0) {
            CubesModel.Cube cube = cubesModel.getCubes().get(0);
            if (cube instanceof CubesModel.DoubleControllerCube) {
                points = ((CubesModel.DoubleControllerCube)cube).getPointsA();
            }
            else {
                points = new PointsGrouping(cube.getPoints());
            }
        } else {
            points = cubesModel.getControllerPoints(id);
        }

        // Mapping Mode: manually get color to animate "unmapped" fixtures that are not network
        // TODO: refactor here
        if (mappingMode.enabled.isOn() && !mappingMode.isFixtureMapped(id)) {
            initPacketData(numPixels, false);
            if (mappingMode.inUnMappedMode()) {
                if (mappingMode.inDisplayAllMode()) {
                    int col = mappingMode.getUnMappedColor();

                    for (int i = 0; i < numPixels; i++)
                        setPixel(i, col);
                } else {
                    if (mappingMode.isSelectedUnMappedFixture(id)) {
                        int col = mappingMode.getUnMappedColor();

                        for (int i = 0; i < numPixels; i++)
                            setPixel(i, col);
                    } else {
                        for (int i = 0; i < numPixels; i++)
                            setPixel(i, (i % 2 == 0) ? LXColor.scaleBrightness(LXColor.RED, 0.2f) : LXColor.BLACK);
                    }
                }
            } else {
                for (int i = 0; i < numPixels; i++) {
                    setPixel(i, (i % 2 == 0) ? LXColor.scaleBrightness(LXColor.RED, 0.2f) : LXColor.BLACK);
                }
            }
        } else if (sendTestPattern) {
            int col = (int) ((System.nanoTime() / 1_000_000_000L) % 3L);
            int c = 0;
            switch (col) {
            /* don't use full-bright colors here, since they bust some of our fixtures. */
            case 0: c = 0xFF880000; break;
            case 1: c = 0xFF008800; break;
            case 2: c = 0xFF000088; break;
            }
            initPacketData(numPixels, false);
            for (int i = 0; i < numPixels; i++) {
                setPixel(i, c);
            }
        } else if (points != null) {
            int numPixels = points.size();

            // Fill the datagram with pixel data
            if (is16BitColorEnabled && src.isFresh(PolyBuffer.Space.RGB16)) {
                initPacketData(numPixels, true);
                long[] srcLongs = (long[]) src.getArray(PolyBuffer.Space.RGB16);
                for (int i = 0; i < numPixels; i++) {
                    LXPoint point = points.getPoint(i);
                    setPixel(i, srcLongs[point.index]);
                }
            } else {
                initPacketData(numPixels, false);
                int[] srcInts = (int[]) src.getArray(PolyBuffer.Space.RGB8);
                for (int i = 0; i < numPixels; i++) {
                    LXPoint point = points.getPoint(i);
                    setPixel(i, srcInts[point.index]);
                }
            }
        } else {
            // Fill with all black if we don't have cube data
            initPacketData(numPixels, false);
            for (int i = 0; i < numPixels; i++) {
                setPixel(i, LXColor.BLACK);
            }
        }

        // Send the cube data to the cube. yay!
        try {
            dsocket.send(packet);
        }
        catch (Exception e) {
            ApplicationState.setWarning("CubesController", "failed to send packet: " + e.getMessage());
        }
    }

    private void resetSocket() {
        if (dsocket != null) {
            dsocket.close();
            dsocket = null;
        }
    }

    @Override
    public void dispose() {
        this.resetSocket();
        if (inventory != null) {
            inventory.removeListener(this);
        }
        super.dispose();
    }

    @Override
    public int compareTo(@NotNull CubesController other) {
        return idInt != other.idInt ? Integer.compare(idInt, other.idInt) : id.compareTo(other.id);
    }

    @Override
    public String toString() {
        return String.format("cube id=%s ip=%s bcast=%s", id, host, isBroadcast ? "yes" : "no");
    }
}
