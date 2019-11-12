package com.symmetrylabs.slstudio.output;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.PerceptualColorScale;
import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.util.NetworkUtils;
import com.symmetrylabs.util.hardware.SLControllerInventory;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.output.LXDatagramOutput;
import heronarts.lx.output.OPCConstants;
import heronarts.lx.parameter.BooleanParameter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;

// All of our controllers operate on UDP but the business logic isn't fundamentally coupled to the transport... (i.e. merits refactor to LXOutput)
public abstract class AbstractSLControllerBase extends LXDatagramOutput implements Comparable<AbstractSLControllerBase>, OPCConstants, SLControllerInventory.Listener {
    public String id;
    public int idInt;
    public final boolean isBroadcast;
    public final NetworkDevice networkDevice;

    private final PerceptualColorScale outputScaler; // should this really be part of the controller logic?
    private final SLControllerInventory inventory;
    protected int numPixels;
    private DatagramSocket dsocket;
    protected boolean is16BitColorEnabled = false;

    // must be public to render?
    public BooleanParameter testOutput = new BooleanParameter("send test data", false);

    private final LX lx;
    private int port;
//    private MappingMode mappingMode;

    public AbstractSLControllerBase(LX lx, NetworkDevice device, SLControllerInventory inventory, PerceptualColorScale outputScaler) throws SocketException {
        this(lx, device, device.ipAddress, null, inventory, false, outputScaler);
    }

    public AbstractSLControllerBase(LX lx, String _host, String _id) throws SocketException {
        this(lx, _host, _id, false);
    }

    public AbstractSLControllerBase(LX lx, String _host) throws SocketException {
        this(lx, _host, "", true);
    }

    private AbstractSLControllerBase(LX lx, String host, String id, boolean isBroadcast) throws SocketException {
        this(lx, null, NetworkUtils.toInetAddress(host), id, null, isBroadcast, null);
    }

    private AbstractSLControllerBase(LX lx, NetworkDevice networkDevice, InetAddress host, String id, SLControllerInventory inventory, boolean isBroadcast, PerceptualColorScale outputScaler) throws SocketException {
        super(lx);

        this.lx = lx;
        this.networkDevice = networkDevice;
        this.id = id;
        this.inventory = inventory;
        this.outputScaler = outputScaler;
        this.isBroadcast = isBroadcast;

        this.addParameter("testOut", testOutput);

        if (inventory != null) {
            inventory.addListener(this);
            onControllerListUpdated();
        } else {
            onIdUpdated();
        }

//        mappingMode = CubesMappingMode.getInstance(lx);

        enabled.setValue(true);
        testOutput.setValue(false);
    }

    public String getMacAddress() {
        return networkDevice == null ? null : networkDevice.deviceId;
    }

    @Override
    public void onControllerListUpdated() {
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

    protected abstract void initPacketData(int numPixels, boolean use16);

    protected abstract void setPixel8(int number, int c);

    private void setPixel16(int number, long c, int[] packetData) {
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
                dsocket.connect(new InetSocketAddress(networkDevice.ipAddress.getHostName(), this.port));
            }
            catch (IOException e) {}
            finally {
                if (dsocket == null) {
                    ApplicationState.setWarning("AbstractSLController", "could not create datagram socket");
                    return;
                }
            }
        }

        // Find the Cube we're outputting to
        // If we're on broadcast, use cube 0 for all cubes, even
        // if that cube isn't modelled yet
        // Use the mac address to find the cube if we have it
        // Otherwise use the cube id
//        if (!(lx.model instanceof CubesModel)) {
//            ApplicationState.setWarning("CubesController", "model is not a cube model");
//            return;
//        }

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
        numPixels = points == null ? 0 : points.size();

        // Mapping Mode: manually get color to animate "unmapped" fixtures that are not network
        // TODO: refactor here
//        if (mappingMode.enabled.isOn() && !mappingMode.isFixtureMapped(id)) {
//            initPacketData(numPixels, false);
//            if (mappingMode.inUnMappedMode()) {
//                if (mappingMode.inDisplayAllMode()) {
//                    int col = mappingMode.getUnMappedColor();
//
//                    for (int i = 0; i < numPixels; i++)
//                        setPixel(i, col);
//                } else {
//                    if (mappingMode.isSelectedUnMappedFixture(id)) {
//                        int col = mappingMode.getUnMappedColor();
//
//                        for (int i = 0; i < numPixels; i++)
//                            setPixel(i, col);
//                    } else {
//                        for (int i = 0; i < numPixels; i++)
//                            setPixel(i, (i % 2 == 0) ? LXColor.scaleBrightness(LXColor.RED, 0.2f) : LXColor.BLACK);
//                    }
//                }
//            } else {
//                for (int i = 0; i < numPixels; i++) {
//                    setPixel(i, (i % 2 == 0) ? LXColor.scaleBrightness(LXColor.RED, 0.2f) : LXColor.BLACK);
//                }
//            }
//        }
        if (testOutput.isOn()) {
            int col = (int) ((System.nanoTime() / 1_000_000_000L) % 3L);
            int c = 0;
            switch (col) {
            /* don't use full-bright colors here, since they bust some of our fixtures. */
            case 0: c = 0xFF020000; break;
            case 1: c = 0xFF000200; break;
            case 2: c = 0xFF000002; break;
            }
            /* we want the test pattern to work even if we aren't mapped, and if
               we aren't mapped we don't know how many pixels we have. Since
               controllers are happy to receive more pixels than they have
               connected to them, and we're just sending a constant color, we just
               send enough pixels that our highest pixel-count-per-controller cube
               would get enough pixels to turn on everything. */
            if (numPixels == 0) {
                setNumPixels();
            }
//            initPacketData(numPixels, false);
            for (int i = 0; i < numPixels; i++) {
                setPixel8(i, c);
            }

            fillDatagramsAndAddToOutput();
        } else if (points != null) {
            // Fill the datagram with pixel data
            fillDatagramsAndAddToOutput();
        } else {
            // Fill with all black if we don't have cube data
//            initPacketData(numPixels, false);
            for (int i = 0; i < numPixels; i++) {
                setPixel8(i, LXColor.BLACK);
            }
        }


        // Send the cube data to the cube. yay!
        super.onSend(src);
//        try {
//            dsocket.send(packet);
//        }
//        catch (Exception e) {
//            ApplicationState.setWarning("CubesController", "failed to send packet: " + e.getMessage());
//        }
    }

    protected abstract void setNumPixels();

    // inheriting class must impliment
    protected abstract void fillDatagramsAndAddToOutput();

    private void resetSocket() {
        if (dsocket != null) {
            System.out.println("closing socket for controller: " + this.id + "---" + this.networkDevice.ipAddress);
            dsocket.close();
            dsocket = null;
        }
    }

    @Override
    public void dispose() {
        this.resetSocket();
        super.dispose();
    }

    @Override
    public int compareTo(@NotNull AbstractSLControllerBase other) {
        return idInt != other.idInt ? Integer.compare(idInt, other.idInt) : id.compareTo(other.id);
    }

    @Override
    public String toString() {
        return String.format("cube id=%s ip=%s bcast=%s", id, networkDevice.ipAddress.getHostAddress(), isBroadcast ? "yes" : "no");
    }
}
