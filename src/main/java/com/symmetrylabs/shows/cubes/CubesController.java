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
import com.symmetrylabs.shows.cubes.CubesModel.Cube;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;

public class CubesController extends LXOutput implements Comparable<CubesController>, OPCConstants, CubeInventory.Listener {
    /*
    * @macro MAX_POWER_CAP ==> a value from 0.0 to 1.0 which dictates the max power of data that can be sent to a cube.
    *
    * This is a very important macro.
    * It was implemented by Nate during Twenty One Pilots cubes install which uses the new UBNT managed switches.
    * These switches are awesome but they have one horrible failure mode - the cubes epilepsy flash uncontrollably
    * if the cubes tries to drive too much power instantaneously (i.e. from blackout to Full white in an instant)
    * In this state they become unresponsive to the network, and obviously a total show killer.  I literally ran on stage
    * to manually unplug things the first time it happened.
    *
    * The macro is used by the following method to decrease the intensity of data going to any individual cube:
    * private boolean power_level_has_fault_condition (PointsGrouping points, boolean is16BitColorEnabled, long[] poly) {}
    *
    * Luckily the same switches that cause this problem also provide relief in this worst case scenario
    * because they can cut power to individual ports via network command.  I have scripted this.
    * Currently this exists at
    * Please go look at the `abstract class ManagedPoEswitch()` and `class ubnt ()`
     */

    private static final double MAX_POWER_CAP = 0.62;

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
        return networkDevice == null ? null : networkDevice.deviceId;
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
        contentSizeBytes = (use16 ? BYTES_PER_16BIT_PIXEL : BYTES_PER_PIXEL) * numPixels;
        packetSizeBytes = HEADER_LEN + contentSizeBytes;
        if (packetData == null || packetData.length != packetSizeBytes) {
            packetData = new byte[packetSizeBytes];
            packet = new DatagramPacket(packetData, packetSizeBytes);
        }
        packetData[0] = (byte)lx.engine.layer.getValuei(); // Channel
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
        int numPixels = points == null ? 0 : points.size();

        // OLD CODE BEGIN
        // Initialize packet data base on cube type.
        // If we don't know the cube type, default to
        // using the cube type with the most pixels

    //    CubesModel.Cube.Type cubeType = cube != null ? cube.type : CubesModel.Cube.CUBE_TYPE_WITH_MOST_PIXELS;
    //    int numPixels = cubeType.POINTS_PER_CUBE;

        // Mapping Mode: manually get color to animate "unmapped" fixtures that are not network
        // TODO: refactor here
        boolean fault_condition = true;
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
                for (int i = 0; i < numPixels; i++)
                    setPixel(i, (i % 2 == 0) ? LXColor.scaleBrightness(LXColor.RED, 0.2f) : LXColor.BLACK);
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
            /* we want the test pattern to work even if we aren't mapped, and if
               we aren't mapped we don't know how many pixels we have. Since
               controllers are happy to receive more pixels than they have
               connected to them, and we're just sending a constant color, we just
               send enough pixels that our highest pixel-count-per-controller cube
               would get enough pixels to turn on everything. */
            if (numPixels == 0) {
                numPixels = CubesModel.Cube.MAX_PIXELS_PER_CONTROLLER;
            }
            initPacketData(numPixels, false);
            for (int i = 0; i < numPixels; i++) {
                setPixel(i, c);
            }
        } else if (points != null) {
            // Fill the datagram with pixel data
            if (is16BitColorEnabled && src.isFresh(PolyBuffer.Space.RGB16)) {
                initPacketData(numPixels, true);
                long[] srcLongs = (long[]) src.getArray(PolyBuffer.Space.RGB16);
//                for (int stripNum = 0; stripNum < numStrips; stripNum++) {
//                    Strip strip = cube.getStrips().get(STRIP_ORD[stripNum]);
//                    for (int i = 0; i < strip.metrics.numPoints; i++) {
//                        LXPoint point = strip.getPoints().get(i);
//                        setPixel(stripNum * strip.metrics.numPoints + i, srcLongs[point.index]);
//                    }
//                }

                // check 16 bit power
//                fault_condition = power_level_has_fault_condition(points, is16BitColorEnabled, srcLongs);
                if (!fault_condition) {
                    for (int i = 0; i < numPixels; i++) {
                        LXPoint point = points.getPoint(i);
                        setPixel(i, srcLongs[point.index]);
                    }
                }
                else{
                    for (int i = 0; i < numPixels; i++) {
                        LXPoint point = points.getPoint(i);

                        long color = srcLongs[point.index];

//                      long nurfed = Ops16.hsb(0,0,lx.engine.powerCap.getValuef());
                        long nurfed = Ops16.hsb(0,0, MAX_POWER_CAP);
                        long defanged = Ops16.multiply(color, nurfed);

//                        setPixel(i, Ops16.hsb(0,100,0.1));
                        setPixel(i, defanged);
                    }
                }
                // NATE:: end 16 bit power mon;



            } else {
                initPacketData(numPixels, false);
                int[] srcInts = (int[]) src.getArray(PolyBuffer.Space.RGB8);
//                for (int stripNum = 0; stripNum < numStrips; stripNum++) {
//                    Strip strip = cube.getStrips().get(STRIP_ORD[stripNum]);
//                    for (int i = 0; i < strip.metrics.numPoints; i++) {
//                        LXPoint point = strip.getPoints().get(i);
//                        setPixel(stripNum * strip.metrics.numPoints + i, srcInts[point.index]);
//                    }
//                }


                // power check before we send this data accross
                fault_condition = true; // if we ever have 8 bit for now just do red
                if (!fault_condition){
                    for (int i = 0; i < numPixels; i++) {
                        LXPoint point = points.getPoint(i);
                        setPixel(i, srcInts[point.index]);
                    }
                }
                // if there's a fault for now set it to BLUE we'll scale later
                else {
                    for (int i = 0; i < numPixels; i++) {
                        LXPoint point = points.getPoint(i);
                        setPixel(i, LXColor.BLACK);
                    }
                }
                // NATE: end 8 bit power mon


            }
        } else {
            // Fill with all black if we don't have cube data or if blacklisted
            initPacketData(numPixels, false);
            for (int i = 0; i < numPixels; i++) {
                setPixel(i, LXColor.BLACK);
            }
        }

        // Send the cube data to the cube. yay!
        try {
            dsocket.send(packet);
        }
        catch (Exception e) { connectionWarning(e); }
    }


//        private boolean power_level_has_fault_condition (PointsGrouping points, boolean is16BitColorEnabled, long[] poly) {
////            double THRESH = 0.7;
////            double THRESH = lx.engine.powerCap.getValuef();
//            double THRESH = MAX_POWER_CAP;
//            numPixels = points.size();
//
//            double aggregate = 0; // just count up the mean intensity
//            double pixel_contribution = 0; // contribution from one pixel
//
//            for (int i = 0; i < numPixels; i++) {
//                LXPoint point = points.getPoint(i);
//                long c = poly[point.index];
//
//                if (is16BitColorEnabled){
//                    pixel_contribution = Ops16.mean(c);
//                }
//                aggregate += pixel_contribution;
//            }
//            double normalize = aggregate/numPixels;
//
//            return (normalize > THRESH);
//
//        }
    // this function looks at the buffer getting sent out every time and if there's a fault condition it reports it.

    private void connectionWarning(Exception e) {
        if (dsocket != null) {
            // System.err.println("Disconnected from OPC server");
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
