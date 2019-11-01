package com.symmetrylabs.shows.base;

import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.network.OpcSysExMsg;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.slstudio.output.SLController;
import heronarts.lx.LX;

import java.io.IOException;
import java.net.*;

public class TreeController extends SLController {
    private static final int DEFAULT_TREE_CONTROLLER_PORT = 1337;
    DatagramSocket machineSock;
    private static InetSocketAddress addr;


    class PowerSamples{
        private int samples[] = new int[8];
    }
    public TreeController(LX lx, NetworkDevice networkDevice, InetAddress host, PointsGrouping points, boolean isBroadcast, String id) {
        super(lx, networkDevice, host, points, isBroadcast, id);
    }

    /*
     * Returns delta since last sample.
     */
    public long testSendOPCSysEx(){
        OpcSysExMsg requestID = new OpcSysExMsg(0, 2, 0, new byte[0]);

        OpcSysExMsg readRam = new OpcSysExMsg(0, 2, 0x20, new byte[0]);
        try {
            machineSock = new DatagramSocket();
            machineSock.connect(new InetSocketAddress(networkDevice.ipAddress, DEFAULT_TREE_CONTROLLER_PORT));
            machineSock.send(requestID.getDatagramPacket());
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("couldn't send packet");
        }
        byte respBuf[] = new byte[256];
        DatagramPacket recvPacket = new DatagramPacket(respBuf, 256);
//        try {
//            machineSock.receive(recvPacket);
//            System.out.println(new String(recvPacket.getData(), 8, recvPacket.getLength() - 8));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        try {
            machineSock.send(readRam.getDatagramPacket());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return System.currentTimeMillis();
    }


}
