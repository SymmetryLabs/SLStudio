package com.symmetrylabs.shows.base;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.ShowRegistry;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.slstudio.server.VolumeCore;
import com.symmetrylabs.slstudio.server.VolumeServer;
import com.symmetrylabs.slstudio.ui.v2.VolumeApplication;
import heronarts.lx.LX;
import heronarts.lx.data.LXVersion;
import heronarts.lx.model.LXModel;
import org.junit.jupiter.api.Test;

import java.net.*;

import static com.symmetrylabs.slstudio.server.VolumeCore.RUNTIME_VERSION;
import static org.junit.jupiter.api.Assertions.*;

class TreeControllerTest {

    @Test
    void acquirePowerSample() {
        VolumeServer volumeServer = new VolumeServer();
        volumeServer.start();
        LX lx = volumeServer.core.lx;

        InetAddress inetAddr;
        NetworkDevice networkDevice = null;
        try {
            inetAddr = InetAddress.getByName("10.5.47.23");
            networkDevice = NetworkDevice.fromIdentifier(inetAddr, "symmeTree/14a7cf7 (tree_rgb8,powerManager) [e::7::b::5::2f::17]".getBytes());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        PointsGrouping pointsGrouping = new PointsGrouping();

        TreeController controller = new TreeController(lx, networkDevice, networkDevice.ipAddress, pointsGrouping, true, "hi");

        controller.acquirePowerSample();
    }

    @Test
    void debugPortMonitor() {
        VolumeServer volumeServer = new VolumeServer();
        volumeServer.start();
        LX lx = volumeServer.core.lx;

        long TEST_TIME_MILLIS = 1000;

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

        long TEST_TIME_MILLIS = 1000;

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
