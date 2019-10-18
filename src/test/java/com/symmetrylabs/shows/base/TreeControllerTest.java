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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
}
