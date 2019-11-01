package com.symmetrylabs.shows.base;

import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.slstudio.server.VolumeServer;
import com.symmetrylabs.util.NetworkChannelDebugMonitor.DebugPortMonitor;
import com.symmetrylabs.util.NetworkChannelDebugMonitor.MachinePortMonitor;
import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
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

        long TEST_TIME_MILLIS = 600000;

        long millis_start = System.currentTimeMillis();

        long delta = System.currentTimeMillis() - millis_start;

        DebugPortMonitor debugPortMonitor = new DebugPortMonitor();
        lx.engine.addLoopTask(new LXLoopTask() {
            @Override
            public void loop(double v){
                debugPortMonitor.start();
            }
        });
    }

    @Test
    void machinePortMonitor() {
        VolumeServer volumeServer = new VolumeServer();
        volumeServer.start();
        LX lx = volumeServer.core.lx;

        long TEST_TIME_MILLIS = 600;

        long millis_start = System.currentTimeMillis();

        long delta = System.currentTimeMillis() - millis_start;

//        MachinePortMonitor machinePortMonitor = new MachinePortMonitor();
//        machinePortMonitor.start();
//
//        do {
//            machinePortMonitor.keepAlive(true);
//        } while(delta  < TEST_TIME_MILLIS );
    }
}
