package com.symmetrylabs.slstudio.output.symmetree;

import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.output.TenereDatagram;
import heronarts.lx.LX;
import heronarts.lx.output.LXDatagram;

import java.io.IOException;
import java.util.ArrayList;

public class TenereDatagramSet {
    private static final byte TWIGS_PER_PACKET = 3;
    static int TENERE_OPC_PORT = 1337;
    TenereDatagram[] datagrams = new TenereDatagram[3];
    public TenereDatagramSet(LX lx, int[][] colorPackets, NetworkDevice networkDevice) {
        byte channel = 0;
        int packetIndex = 0;
        for (int[] packet : colorPackets) {
            TenereDatagram datagram = new TenereDatagram(lx, packet, channel, true);
            try {
                datagram.setAddress(networkDevice.ipAddress.getHostAddress()).setPort(TENERE_OPC_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            channel += TWIGS_PER_PACKET;
            datagrams[packetIndex++] = datagram;
        }
    }

    public TenereDatagram[] getDatagrams() {
        return datagrams;
    }
}
