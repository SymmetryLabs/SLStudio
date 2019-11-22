package com.symmetrylabs.slstudio.output;

import com.google.gson.annotations.Expose;
import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.PerceptualColorScale;
import com.symmetrylabs.shows.base.SLShow;
import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.tree.TreeModel;
import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.ui.UIOfflineRender;
import com.symmetrylabs.util.NetworkUtils;
import com.symmetrylabs.util.hardware.SLControllerInventory;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.LXDatagramOutput;
import heronarts.lx.output.OPCConstants;
import heronarts.lx.parameter.BooleanParameter;
import org.jetbrains.annotations.NotNull;

import java.net.*;

// All of our controllers operate on UDP but the business logic isn't fundamentally coupled to the transport... (i.e. merits refactor to LXOutput)
public abstract class DiscoverableController extends LXDatagramOutput implements Comparable<DiscoverableController>, OPCConstants, SLControllerInventory.Listener {
    @Expose
    public String humanID;
    public int idInt;
    public final boolean isBroadcast;
    @Expose
    public final NetworkDevice networkDevice;

    public String notes = "";

    private final PerceptualColorScale outputScaler; // should this really be part of the controller logic?
    private final SLControllerInventory inventory;
    public Integer switchPortNumber; // the physical port index which this controller is plugged to.  May be null.
    public String newControllerID = ""; // tmp holder for new controller ID to write
    public BooleanParameter isBroadcastDevice = new BooleanParameter("this is the special 10.255.255.255 device", false);
    protected int numPixels;
    protected boolean is16BitColorEnabled = false;

    // must be public to render?
    public BooleanParameter testOutput = new BooleanParameter("send test data", false);
    public BooleanParameter momentaryAltTestOutput = new BooleanParameter("output on alt down", false);
    public BooleanParameter momentaryAltShiftTestBlackout = new BooleanParameter("blackout on alt shift down", false);

    // variable set to true when on network, and unmapped "should be black
    protected BooleanParameter unmappedSendBlack = new BooleanParameter("black when unmapped", true);

    private final LX lx;
    private int port;
//    private MappingMode mappingMode;

    public DiscoverableController(LX lx, NetworkDevice device, SLControllerInventory inventory, PerceptualColorScale outputScaler) throws SocketException {
        this(lx, device, device.ipAddress, device.deviceId, inventory, false, outputScaler);
    }

    public DiscoverableController(LX lx, String _host, String _id) throws SocketException {
        this(lx, _host, _id, false);
    }

    public DiscoverableController(LX lx, String _host) throws SocketException {
        this(lx, _host, "", true);
    }

    private DiscoverableController(LX lx, String host, String humanID, boolean isBroadcast) throws SocketException {
        this(lx, null, NetworkUtils.toInetAddress(host), humanID, null, isBroadcast, null);
    }

    private DiscoverableController(LX lx, NetworkDevice networkDevice, InetAddress host, String humanID, SLControllerInventory inventory, boolean isBroadcast, PerceptualColorScale outputScaler) throws SocketException {
        super(lx);

        System.out.println("created socket for controller: " + ((networkDevice == null) ? "null" : networkDevice.ipAddress));

        this.lx = lx;
        this.networkDevice = networkDevice;
        this.humanID = humanID;
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
        if (newId != null && (humanID == null || !humanID.equals(newId))) {
            humanID = newId;
            onIdUpdated();
        }
    }

    private void onIdUpdated() {
        int idInt = Integer.MAX_VALUE;
        try {
            idInt = Integer.parseInt(humanID);
        } catch (NumberFormatException e) {}
        this.idInt = idInt;
    }

    public void set16BitColorEnabled(boolean enable) {
        this.is16BitColorEnabled = enable;
    }

    protected abstract void initPacketData(int numPixels, boolean use16);

    // methods for setting pixel
    protected abstract void setPixel8(int number, int c);
    protected abstract void setPixel16(int i, long srcLong);

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
//        if (dsocket == null) {
//            try {
//                dsocket = new DatagramSocket();
//                dsocket.connect(new InetSocketAddress(networkDevice.ipAddress.getHostName(), this.port));
//            }
//            catch (IOException e) {}
//            finally {
//                if (dsocket == null) {
//                    ApplicationState.setWarning("AbstractSLController", "could not create datagram socket");
//                    return;
//                }
//            }
//        }

        // Find the Cube we're outputting to
        // If we're on broadcast, use cube 0 for all cubes, even
        // if that cube isn't modelled yet
        // Use the mac address to find the cube if we have it
        // Otherwise use the cube humanID
//        if (!(lx.model instanceof CubesModel)) {
//            ApplicationState.setWarning("CubesController", "model is not a cube model");
//            return;
//        }

        PointsGrouping points = null;

//        points = SLShow.getPointsMappedToControllerID(this.humanID); // returns null if we're not broadcast
        points = SLShow.mapping.getPointsMappedToControllerID(this.humanID); // returns null if we're not broadcast

        numPixels = points == null ? 0 : points.size();

        // Mapping Mode: manually get color to animate "unmapped" fixtures that are not network
        // TODO: refactor here
//        if (mappingMode.enabled.isOn() && !mappingMode.isFixtureMapped(humanID)) {
//            initPacketData(numPixels, false);
//            if (mappingMode.inUnMappedMode()) {
//                if (mappingMode.inDisplayAllMode()) {
//                    int col = mappingMode.getUnMappedColor();
//
//                    for (int i = 0; i < numPixels; i++)
//                        setPixel(i, col);
//                } else {
//                    if (mappingMode.isSelectedUnMappedFixture(humanID)) {
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

        if (testOutput.isOn() || momentaryAltTestOutput.isOn()) {
            int col = (int) ((System.nanoTime() / 1_000_000_000L) % 3L);
            int c = 0;
            switch (col) {
                /* don't use full-bright colors here, since they bust some of our fixtures. */
                case 0:
                    c = 0xFFf20000;
                    break;
                case 1:
                    c = 0xFF00f200;
                    break;
                case 2:
                    c = 0xFF0000f2;
                    break;
            }
            if (momentaryAltTestOutput.isOn()) {
                c = LXColor.hsb(0, 0, lx.engine.output.brightness.getNormalizedf() * 100);
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
        } else if (ApplicationState.outputControl().testUnicast.isOn() || isBroadcast || ApplicationState.outputControl().testBroadcast.isOn()) {
            // first get the fixture.
            SLModel model = ((SLModel) lx.model);
            if (model instanceof TreeModel){
                points = new PointsGrouping(((TreeModel) model).getBranches().get(0).getPoints());
            }
            if (is16BitColorEnabled && src.isFresh(PolyBuffer.Space.RGB16)) {
                initPacketData(numPixels, true);
                long[] srcLongs = (long[]) src.getArray(PolyBuffer.Space.RGB16);
                for (int i = 0; i < numPixels; i++) {
                    LXPoint point = points.getPoint(i);
                    setPixel16(i, srcLongs[point.index]);
                }
            } else {
                // WARNING!! static desperate to get tree working - change later
                numPixels = 1200;

                initPacketData(numPixels, false);
                int[] srcInts = (int[]) src.getArray(PolyBuffer.Space.RGB8);
                for (int i = 0; i < numPixels; i++) {
                    LXPoint point = points.getPoint(i);
                    setPixel8(i, srcInts[point.index]);
                }
            }

            // then add output depending on controller
            if (ApplicationState.outputControl().testBroadcast.isOn()){
                // ok only if we are the special broadcast device
                if (!this.isBroadcastDevice.isOn()){
                    return; // do nothing if we're not the device
                }
            }
            else { // if we're outputing normally we don't want this.
                if (this.isBroadcastDevice.isOn()){
                    return;
                }
            }
            fillDatagramsAndAddToOutput();
        } else if (points != null) { // there is a fixture for this one
            numPixels = 1200;
            // Fill the datagram with pixel data
            if (is16BitColorEnabled && src.isFresh(PolyBuffer.Space.RGB16)) {
                initPacketData(numPixels, true);
                long[] srcLongs = (long[]) src.getArray(PolyBuffer.Space.RGB16);
                for (int i = 0; i < numPixels; i++) {
                    LXPoint point = points.getPoint(i);
                    setPixel16(i, srcLongs[point.index]);
                }
            } else {
                initPacketData(numPixels, false);
                int[] srcInts = (int[]) src.getArray(PolyBuffer.Space.RGB8);
                for (int i = 0; i < numPixels; i++) {
                    LXPoint point = points.getPoint(i);
                    setPixel8(i, srcInts[point.index]);
                }
            }
            fillDatagramsAndAddToOutput();
        } else {
            // Fill with all black if we don't have cube data
//            initPacketData(numPixels, false);
            unmappedSendBlack.setValue(true);
            if (numPixels == 0) {
                setNumPixels();
            }
            // set value red
            for (int i = 0; i < numPixels; i++) {
                int unmappedColor = LXColor.rgba(0xff, 0, 0, (int) (lx.engine.output.brightness.getValue() * 0xff));
                unmappedColor = LXColor.scaleBrightness(unmappedColor, lx.engine.output.brightness.getNormalizedf());
                setPixel8(i, unmappedColor);
            }
            fillDatagramsAndAddToOutput();
        }

        // Send the correctly formatted data to the fixture. yay!
        super.onSend(src);
    }



    private PointsGrouping getBroadcastPointsFromFixture0(LXModel model) {
        PointsGrouping points = null;
        if (model instanceof CubesModel){
            CubesModel cubesModel = (CubesModel) model;
            if ((ApplicationState.outputControl().testUnicast.isOn() || isBroadcast) && cubesModel.getCubes().size() > 0) {
                CubesModel.Cube cube = cubesModel.getCubes().get(0);
                if (cube instanceof CubesModel.DoubleControllerCube) {
                    points = ((CubesModel.DoubleControllerCube)cube).getPointsA();
                }
                else {
                    points = new PointsGrouping(cube.getPoints());
                }
            } else {
                points = cubesModel.getControllerPoints(humanID);
            }
        }
        return points;
    }

    protected abstract void setNumPixels();

    // inheriting class must impliment
    protected abstract void fillDatagramsAndAddToOutput();

    private void resetSocket() {
        if (socket != null) {
            System.out.println("closing socket for controller: " + this.humanID + "---" + this.networkDevice.ipAddress);
            socket.close();
        }
    }

    @Override
    public void dispose() {
        this.resetSocket();
        super.dispose();
    }

    @Override
    public int compareTo(@NotNull DiscoverableController other) {
//        return idInt != other.idInt ? Integer.compare(idInt, other.idInt) : humanID.compareTo(other.humanID);
        assert !humanID.equals(other.humanID);
        return humanID.compareTo(other.humanID);
    }

    @Override
    public String toString() {
        return String.format("cube humanID=%s ip=%s bcast=%s", humanID, networkDevice.ipAddress.getHostAddress(), isBroadcast ? "yes" : "no");
    }
}
