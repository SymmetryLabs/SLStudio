// package com.symmetrylabs.slstudio.output.symmetree;

// import java.io.IOException;
// import java.net.SocketException;
// import java.net.UnknownHostException;
// import java.net.InetAddress;
// import heronarts.lx.parameter.StringParameter;

// import heronarts.lx.LX;
// import heronarts.lx.model.LXPoint;
// import heronarts.lx.output.LXDatagram;
// import heronarts.lx.output.LXDatagramOutput;

// import com.symmetrylabs.slstudio.output.TenereDatagram;
// import com.symmetrylabs.shows.tree.TreeModel_v2;


// public class SymmetreeController extends LXDatagramOutput {

// 	private static final int TWIGS_PER_PACKET = 3;
// 	private static final int POINTS_PER_PACKET = TreeModel_v2.Twig.NUM_LEDS * TWIGS_PER_PACKET;
// 	private static final int OPC_PORT = 1337;

//     private final LX lx;
//     private final int[][] packets = new int[][] {
//         new int[POINTS_PER_PACKET],
//         new int[POINTS_PER_PACKET],
//         new int[POINTS_PER_PACKET],
//     };

//     private String ipAddress;

//     private TreeModel_v2.Branch branch;

//     public enum FirwareMode {
//         DEFAULT, BYPASS, TEST
//     }

//     private final EnumParameter<FirmwareMode> firwareMode = new EnumParameter<>("firmwareMode", FirmwareMode.DEFAULT);

//     private byte outputConfig = 0xff;

// 	public SymmetreeController(LX lx) throws SocketException {
// 		super(lx);
//         this.lx = lx;   

//         // ipaddress.setValue(branch.getConfig().ipAddress);
//         // if (getIpAddress().equals("0.0.0.0")) {
//         //     enabled.setValue(false);
//         // }
//         //updateTwigIndices();

//         // final TreeModelingTool tmt = TreeModelingTool.getInstance(lx);
//         // tmt.twigManipulator.index.addListener(parameter -> {
//         //     String ipAddress = tmt.branchManipulator.ipAddress.getString();
//         //     if (getIpAddress().equals(ipAddress)) {
//         //         updateTwigIndices();
//         //     }
//         // });
//         // tmt.branchManipulator.ipAddress.addListener(parameter -> {
//         //     String ipAddress = ((StringParameter)parameter).getString();
//         //     if (getIpAddress().equals(ipAddress)) {
//         //         this.branch = TreeModelingTool.getInstance(lx).getSelectedBranch();
//         //         updateTwigIndices();
//         //     }
//         // });
// 	}

//     public void setIpAddress(String ipAddress) {
//         this.ipAddress.setValue(ipAddress);

//         try {
//             for (LXDatagram datagram : getDatagrams()) {
//                 datagram.setAddress(getIpAddress());
//             }
//         } catch (Exception e) { }
//     }

//     public String getIpAddress() {
//         return ipAddress.getString();
//     }

//     public void setFirmwareMode(FirmwareMode mode) {
//         firmwareMode.setValue(mode);
//         updateControllerState();
//     }

//     public FirmwareMode getFirmwareMode() {
//         return firmwareMode.getEnum();
//     }

//     public void setOutputConfig(int i, boolean on) {
//         // todo...
//         updateControllerState();
//     }

//     public void setOutputConfig(byte config) {
//         this.outputConfig = config;
//         updateControllerState();
//     }

//     private void updateControllerState() {
//         // send to controller!
//     }

//     public void setBranch(TreeModel_v2.Branch branch) {
//         this.branch = branch;
//         updateTwigIndices();
//     }

//     public void updateTwigIndices() {
// 		for (int i = 0; i < packets.length; ++i) {
// 		    for (int j = 0; j < POINTS_PER_PACKET; j++) packets[i][j] = -1; // Initialize to nothing
// 		}
//         for (TreeModel_v2.Twig twig : branch.getTwigs()) {
//             int index = twig.index;
//             int packet = index / TWIGS_PER_PACKET; // truncates to floor
//             int pindex = TreeModel_v2.Twig.NUM_LEDS * (index - (TWIGS_PER_PACKET * packet));
//             for (int indexInTwig = 0; indexInTwig < twig.points.length; indexInTwig++) {
//                 boolean skip = false;
//                 int[] disabled = twig.getConfig().disabledPixels;
//                 if (disabled != null) {
//                     for (int d = 0; d < disabled.length; d++) {
//                         if (disabled[d] == indexInTwig) skip = true;
//                     }
//                 }
//                 packets[packet][pindex++] = skip ? -1 : twig.points[indexInTwig].index;
//             }
//         }
//         setupDatagrams();
//     }

//     private void setupDatagrams() {
//         clearDatagrams();
//         byte channel = 0;
//         for (int[] packet : packets) {
//             TenereDatagram datagram = new TenereDatagram(lx, packet, channel);
//             try {
//                 datagram.setAddress(getIpAddress()).setPort(OPC_PORT);
//                 addDatagram(datagram);
//             } catch (IOException e) {
//                 e.printStackTrace();
//             }
//             channel += TWIGS_PER_PACKET;
//         }
//     }
// }
