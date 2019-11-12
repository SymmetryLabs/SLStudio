package com.symmetrylabs.util.NetworkChannelDebugMonitor;

import com.symmetrylabs.shows.base.SLShow;
import com.symmetrylabs.slstudio.network.OpcSysExMsg;
import com.symmetrylabs.slstudio.output.SLController;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class MachinePortMonitor extends Thread {
    private DatagramSocket staticMachineSock;
    private boolean started = true;
    private SLShow show;

    public MachinePortMonitor (SLShow show){
        this.show = show;
    }
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

            // which ip do we have?
            InetAddress sourceControllerAddr = recvPacket.getAddress();

//            SLController controller = show.getControllerByInetAddr(sourceControllerAddr);
//            if (controller != null){
//                controller.writeSample(sysExIn.metaPowerSample);
//            }
        }
    }

    public void keepAlive(boolean b) {
        started = b;
    }
}
