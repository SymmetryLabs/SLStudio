package com.symmetrylabs.util.NetworkChannelDebugMonitor;

import com.symmetrylabs.slstudio.network.OpcSysExMsg;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class MachinePortMonitor extends Thread {
    private DatagramSocket staticMachineSock;
    private boolean started = true;
    public void run(){
        try {
            staticMachineSock = new DatagramSocket(5679);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while (started){

            byte[] respBuf = new byte[256];
            DatagramPacket recvPacket = new DatagramPacket(respBuf, 256);
            OpcSysExMsg sysExIn = null;
            try {
                staticMachineSock.receive(recvPacket);
                sysExIn = new OpcSysExMsg(recvPacket.getData(), recvPacket.getLength());
            } catch (IOException e) {
                e.printStackTrace();
            }

            sysExIn.deserializeSysEx_0x7();

        }
    }

    public void keepAlive(boolean b) {
        started = b;
    }
}
