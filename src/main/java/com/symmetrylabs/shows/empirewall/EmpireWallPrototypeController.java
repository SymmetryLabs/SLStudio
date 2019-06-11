package com.symmetrylabs.shows.empirewall;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.InetAddress;
import heronarts.lx.parameter.StringParameter;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.LXDatagram;
import heronarts.lx.output.LXDatagramOutput;

import com.symmetrylabs.shows.vines.VineModel;
import com.symmetrylabs.slstudio.output.TenereDatagram;
import com.symmetrylabs.shows.tree.TreeModel;


public class EmpireWallPrototypeController extends LXDatagramOutput {

    private static final int TWIGS_PER_PACKET = 3;
    private static final int POINTS_PER_PACKET = TreeModel.Twig.NUM_LEDS * TWIGS_PER_PACKET;
    private static final int OPC_PORT = 1337;

    private String ipAddress;
    private VineModel model;
    private final LX lx;
    private final int[][] packets;

    public EmpireWallPrototypeController(LX lx, String ipAddress, VineModel model) throws SocketException {
        super(lx);
        this.lx = lx;
        this.ipAddress = ipAddress;
        this.model = model;

        packets = new int[][] {
            new int[POINTS_PER_PACKET],
            new int[POINTS_PER_PACKET],
            new int[POINTS_PER_PACKET],
        };

        init();
    }

    public void init() {
        for (int i = 0; i < packets.length; ++i) {
            // Initialize to nothing
            for (int j = 0; j < POINTS_PER_PACKET; j++) {
                packets[i][j] = -1;
            }
        }

        //for (TreeModel.Twig twig : branch.getTwigs()) {
        int i = 0;
        for (VineModel.Vine vine : model.vines) {
            int index = i;
            int packet = index / TWIGS_PER_PACKET; // truncates to floor
            int pindex = TreeModel.Twig.NUM_LEDS * (index - (TWIGS_PER_PACKET * packet));
            for (int indexInTwig = 0; indexInTwig < TreeModel.Twig.NUM_LEDS; indexInTwig++) {
                // boolean skip = false;
                // int[] disabled = twig.getConfig().disabledPixels;
                // if (disabled != null) {
                //     for (int d = 0; d < disabled.length; d++) {
                //         if (disabled[d] == indexInTwig) {
                //             skip = true;
                //         }
                //     }
                // }
                packets[packet][pindex++] = (indexInTwig >= vine.points.length) ? 0 : vine.points[indexInTwig].index;
            }
            i++;
        }

        clearDatagrams();
        byte channel = 0;
        for (int[] packet : packets) {
            TenereDatagram datagram = new TenereDatagram(lx, packet, channel);
            try {
                datagram.setAddress(ipAddress).setPort(OPC_PORT);
                addDatagram(datagram);
            } catch (IOException e) {
                e.printStackTrace();
            }
            channel += TWIGS_PER_PACKET;
        }
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;

        try {
            for (LXDatagram datagram : getDatagrams()) {
                datagram.setAddress(ipAddress);
            }
        } catch (Exception e) { }
    }

    public String getIpAddress() {
        return ipAddress;
    }
}



// // SPECIFIC TO EMPIREWALLPROTOTYPESHOW!!!
// public class EmpireWallPrototypeController extends LXDatagramOutput {

// 	private static final int OPC_PORT = 1337;
//     private static final int TWIGS_PER_PACKET = 3;
//     private static final int POINTS_PER_PACKET = TreeModel.Twig.NUM_LEDS * TWIGS_PER_PACKET;

// 	private String ipAddress;
//     private final LX lx;
//     private final int[][] packets;

// 	public EmpireWallPrototypeController(LX lx, String ipAddress, VineModel model) throws SocketException {
// 		super(lx);
//         this.lx = lx;
// 		this.ipAddress = ipAddress;

//         packets = new int[][] {
//             new int[POINTS_PER_PACKET],
//             new int[POINTS_PER_PACKET]
//         };

//         for (int i = 0; i < packets.length; ++i) {
//             // Initialize to nothing
//             for (int j = 0; j < POINTS_PER_PACKET; j++) {
//                 packets[i][j] = -1;
//             }
//         }

//         for (int i = 0; i < model.vines.size(); i++) {
//             int packet = i > 2 ? 1 : 0;

//             VineModel.Vine vine = model.vines.get(i);
//             for (int j = 0; j < 150; j++) {
//                 packets[packet][j] = (j < vine.points.length) ? vine.points[j].index : 0;
//                 System.out.print("(" + packet + "," + packets[packet][j] + "), ");
//             }
//             System.out.println("\n");
//         }

//         byte channel = 0;
//         for (int[] packet : packets) {
//             TenereDatagram datagram = new TenereDatagram(lx, packet, channel);
//             try {
//                 datagram.setAddress(ipAddress).setPort(OPC_PORT);
//                 addDatagram(datagram);
//             } catch (IOException e) {
//                 e.printStackTrace();
//             }
//             channel += 4;
//         }
// 	}
// }
