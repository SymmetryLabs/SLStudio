package com.symmetrylabs.shows.base;

import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.network.OpcSysExMsg;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.slstudio.output.SLController;
import heronarts.lx.LX;
import heronarts.lx.output.OPCDatagram;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;

public class TreeController extends SLController {
    private static final int DEFAULT_TREE_CONTROLLER_PORT = 7890;
    DatagramSocket dsock;

    class PowerSamples{
        private int samples[] = new int[8];
    }
    TreeController(LX lx, NetworkDevice networkDevice, InetAddress host, PointsGrouping points, boolean isBroadcast, String id) {
        super(lx, networkDevice, host, points, isBroadcast, id);
    }

    /*
     * Returns delta since last sample.
     */
    public long acquirePowerSample(){
        OpcSysExMsg sysEx = new OpcSysExMsg(0, 2, 0, new byte[0]);
        try {
            dsock = new DatagramSocket();
            dsock.connect(new InetSocketAddress(networkDevice.ipAddress, DEFAULT_TREE_CONTROLLER_PORT));
            dsock.send(sysEx.getDatagramPacket());
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("couldn't send packet");
        }
        byte respBuf[] = new byte[256];
        DatagramPacket recvPacket = new DatagramPacket(respBuf, 256);
        try {
            dsock.receive(recvPacket);
            System.out.println(new String(recvPacket.getData(), 8, recvPacket.getData().length - 8));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return System.currentTimeMillis();
    }
}
