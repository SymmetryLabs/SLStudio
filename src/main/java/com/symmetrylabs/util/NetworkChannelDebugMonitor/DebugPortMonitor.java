package com.symmetrylabs.util.NetworkChannelDebugMonitor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class DebugPortMonitor extends Thread {

    private static DatagramSocket debugSock = null;
    boolean started = true;
    // common debug port for all Tree Controllers .. probably makes sense to abstract to all cubes controllers
    public void run(){
        while(started) {
            if (debugSock == null){
                try {
                    debugSock = new DatagramSocket(5678);
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
            byte[] respBuf = new byte[256];
            DatagramPacket recvPacket = new DatagramPacket(respBuf, 256);
            try {
                debugSock.receive(recvPacket);
                System.out.print(new String(recvPacket.getData(), 0, recvPacket.getLength()));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
