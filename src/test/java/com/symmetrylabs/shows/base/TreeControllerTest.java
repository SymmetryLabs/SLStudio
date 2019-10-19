package com.symmetrylabs.shows.base;

import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.slstudio.server.VolumeServer;
import heronarts.lx.LX;
import org.junit.jupiter.api.Test;

import java.net.*;

class TreeControllerTest {

    @Test
    void testSendOPCSysEx() {
        VolumeServer volumeServer = new VolumeServer();
        volumeServer.start();
        LX lx = volumeServer.core.lx;

        InetAddress inetAddr;
        NetworkDevice networkDevice = null;
        try {
            inetAddr = InetAddress.getByName("10.2.42.21");
            networkDevice = NetworkDevice.fromIdentifier(inetAddr, "symmeTree/14a7cf7 (tree_rgb8,powerManager) [e::7::b::5::2f::17]".getBytes());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        PointsGrouping pointsGrouping = new PointsGrouping();

        TreeController controller = new TreeController(lx, networkDevice, networkDevice.ipAddress, pointsGrouping, true, "hi");

        controller.testSendOPCSysEx();
    }

    @Test
    void debugPortMonitor() {
        VolumeServer volumeServer = new VolumeServer();
        volumeServer.start();
        LX lx = volumeServer.core.lx;

        long TEST_TIME_MILLIS = 60000;

        long millis_start = System.currentTimeMillis();

        long delta = System.currentTimeMillis() - millis_start;

        try {
            TreeController.debugSock = new DatagramSocket(5678);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        do {
            TreeController.debugPortMonitor();
            delta = System.currentTimeMillis() - millis_start;
        } while(delta  < TEST_TIME_MILLIS );
    }

    @Test
    void machinePortMonitor() {
        VolumeServer volumeServer = new VolumeServer();
        volumeServer.start();
        LX lx = volumeServer.core.lx;

        long TEST_TIME_MILLIS = 60000;

        long millis_start = System.currentTimeMillis();

        long delta = System.currentTimeMillis() - millis_start;

        try {
            TreeController.staticMachineSock = new DatagramSocket(5679);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        do {
            TreeController.machinePortMonitor();
            delta = System.currentTimeMillis() - millis_start;
        } while(delta  < TEST_TIME_MILLIS );
    }
}
