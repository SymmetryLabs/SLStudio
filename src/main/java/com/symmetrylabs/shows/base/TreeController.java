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

    static DatagramSocket debugSock;
    static DatagramSocket staticMachineSock;

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
            machineSock = new DatagramSocket();
            machineSock.connect(new InetSocketAddress(networkDevice.ipAddress, DEFAULT_TREE_CONTROLLER_PORT));
            machineSock.send(sysEx.getDatagramPacket());
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("couldn't send packet");
        }
        byte respBuf[] = new byte[256];
        DatagramPacket recvPacket = new DatagramPacket(respBuf, 256);
        try {
            machineSock.receive(recvPacket);
            System.out.println(new String(recvPacket.getData(), 8, recvPacket.getLength() - 8));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return System.currentTimeMillis();
    }

    // common debug port for all Tree Controllers .. probably makes sense to abstract to all cubes controllers
    static public long debugPortMonitor(){
        byte[] respBuf = new byte[256];
        DatagramPacket recvPacket = new DatagramPacket(respBuf, 256);
        try {
            debugSock.receive(recvPacket);
            System.out.println(new String(recvPacket.getData(), 0, recvPacket.getLength()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return System.currentTimeMillis();
    }

    static public long machinePortMonitor(){
        byte[] respBuf = new byte[256];
        DatagramPacket recvPacket = new DatagramPacket(respBuf, 256);
        try {
            staticMachineSock.receive(recvPacket);
            OpcSysExMsg sysExIn = new OpcSysExMsg(recvPacket.getData(), recvPacket.getLength());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return System.currentTimeMillis();
    }
}
