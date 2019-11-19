package com.symmetrylabs.shows.base;

//public class SLController extends LXOutput implements Comparable<SLController>, OPCConstants {
//    public String humanID;
//    public int idInt;
//    public final InetAddress host;
//    public final boolean isBroadcast;
//    public final NetworkDevice networkDevice;
//
//    private final PerceptualColorScale outputScaler;
//    private final Inventory inventory;
//    private DatagramSocket dsocket;
//    private DatagramPacket packet;
//    private OutputStream output;
//    protected boolean is16BitColorEnabled = false;
//
//    int contentSizeBytes;
//    int packetSizeBytes;
//    byte[] packetData;
//    boolean sendTestPattern = false;
//
//    private final LX lx;
////    private CubesMappingMode mappingMode;
//
//    public SLController(LX lx, NetworkDevice device, Inventory inventory, PerceptualColorScale outputScaler) {
//        this(lx, device, device.ipAddress, null, inventory, false, outputScaler);
//    }
//
//    public SLController(LX lx, String _host, String _id) {
//        this(lx, _host, _id, false);
//    }
//
//    public SLController(LX lx, String _host) {
//        this(lx, _host, "", true);
//    }
//
//    private SLController(LX lx, String host, String humanID, boolean isBroadcast) {
//        this(lx, null, NetworkUtils.toInetAddress(host), humanID, null, isBroadcast, null);
//    }
//
//    private SLController(LX lx, NetworkDevice networkDevice, InetAddress host, String humanID, CubeInventory inventory, boolean isBroadcast, PerceptualColorScale outputScaler) {
//        super(lx);
//
//        this.lx = lx;
//        this.networkDevice = networkDevice;
//        this.host = host;
//        this.humanID = humanID;
////        this.inventory = inventory;
//        this.outputScaler = outputScaler;
//        this.isBroadcast = isBroadcast;
//
////        if (inventory != null) {
////            inventory.addListener(this);
////            onCubeListUpdated();
////        } else {
////            onIdUpdated();
////        }
//            onIdUpdated();
//
////        mappingMode = CubesMappingMode.getInstance(lx);
//
//        enabled.setValue(true);
//    }
//
//    public String getMacAddress() {
//        return networkDevice == null ? null : networkDevice.deviceId;
//    }
//
//    public void onControllerListUpdated() {
////        String newId = inventory.getControllerId(networkDevice.deviceId);
////        if (newId != null && (humanID == null || !humanID.equals(newId))) {
////            humanID = newId;
////            onIdUpdated();
////        }
//            onIdUpdated();
//    }
//
//    private void onIdUpdated() {
//        int idInt = Integer.MAX_VALUE;
//        try {
//            idInt = Integer.parseInt(humanID);
//        } catch (NumberFormatException e) {}
//        this.idInt = idInt;
//    }
//
//    public void set16BitColorEnabled(boolean enable) {
//        this.is16BitColorEnabled = enable;
//    }
//
//    private void initPacketData(int numPixels, boolean use16) {
//        contentSizeBytes = (use16 ? BYTES_PER_16BIT_PIXEL : BYTES_PER_PIXEL) * numPixels;
//        packetSizeBytes = HEADER_LEN + contentSizeBytes;
//        if (packetData == null || packetData.length != packetSizeBytes) {
//            packetData = new byte[packetSizeBytes];
//            packet = new DatagramPacket(packetData, packetSizeBytes);
//        }
//        packetData[0] = 0; // Channel
//        packetData[1] = use16 ? COMMAND_SET_16BIT_PIXEL_COLORS : COMMAND_SET_PIXEL_COLORS;
//        packetData[2] = (byte) ((contentSizeBytes >> 8) & 0xFF);
//        packetData[3] = (byte) (contentSizeBytes & 0xFF);
//    }
//
//    private void setPixel(int number, int c) {
//        if (outputScaler != null) {
//            c = outputScaler.apply8(c);
//        }
//        int index = 4 + number * 3;
//        packetData[index++] = LXColor.red(c);
//        packetData[index++] = LXColor.green(c);
//        packetData[index++] = LXColor.blue(c);
//    }
//
//    private void setPixel(int number, long c) {
//        int index = 4 + number * 6;
//        if (outputScaler != null) {
//            c = outputScaler.apply16(c);
//        }
//        int red = Ops16.red(c);
//        int green = Ops16.green(c);
//        int blue = Ops16.blue(c);
//        packetData[index++] = (byte) (red >>> 8);
//        packetData[index++] = (byte) (red & 0xff);
//        packetData[index++] = (byte) (green >>> 8);
//        packetData[index++] = (byte) (green & 0xff);
//        packetData[index++] = (byte) (blue >>> 8);
//        packetData[index++] = (byte) (blue & 0xff);
//    }
//
//    @Override
//    protected void onSend(PolyBuffer src) {
//        if (isBroadcast != ApplicationState.outputControl().broadcastPacket.isOn())
//            return;
//
//        // Create data socket connection if needed
//        if (dsocket == null) {
//            try {
//                dsocket = new DatagramSocket();
//                dsocket.connect(new InetSocketAddress(host, 7890));
//            }
//            catch (IOException e) {}
//            finally {
//                if (dsocket == null) {
//                    ApplicationState.setWarning("CubesController", "could not create datagram socket");
//                    return;
//                }
//            }
//        }
//
//        // Find the Cube we're outputting to
//        // If we're on broadcast, use cube 0 for all cubes, even
//        // if that cube isn't modelled yet
//        // Use the mac address to find the cube if we have it
//        // Otherwise use the cube humanID
//        if (!(lx.model instanceof CubesModel)) {
//            ApplicationState.setWarning("CubesController", "model is not a cube model");
//            return;
//        }
//
//        PointsGrouping points = null;
////        CubesModel cubesModel = (CubesModel)lx.model;
//
//    }
//
//    private void resetSocket() {
//        if (dsocket != null) {
//            dsocket.close();
//            dsocket = null;
//        }
//    }
//
//    @Override
//    public void dispose() {
//        this.resetSocket();
////        if (inventory != null) {
////            inventory.removeListener(this);
////        }
//        super.dispose();
//    }
//
//    @Override
//    public int compareTo(@NotNull SLController other) {
//        return idInt != other.idInt ? Integer.compare(idInt, other.idInt) : humanID.compareTo(other.humanID);
//    }
//
//    @Override
//    public String toString() {
//        return String.format("cube humanID=%s ip=%s bcast=%s", humanID, host, isBroadcast ? "yes" : "no");
//    }
//}
