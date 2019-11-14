package com.symmetrylabs.shows.tree;

import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.util.hardware.powerMon.MetaSample;
import org.junit.Test;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import static org.junit.Assert.*;

public class AssignableTenereControllerTest {

    private AssignableTenereController controller = null;

    private void loadMockController() {
        try {
            for (int i = 0; i < 4; i++){
                controller = new AssignableTenereController(null, new NetworkDevice(InetAddress.getByName("10.2.3." + i), "123", "2", "a10" + i, new String[]{"cool", "feature", "bro"}), null);

                // pretend we just got a power reading...
                MetaSample pretendSample = new MetaSample();
                pretendSample.powerOnStateMask = 0xf0;

                controller.writeSample(pretendSample);
            }
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void setPortPowerMask() {
        loadMockController();

        // 00000010
        controller.setPortPowerMask((byte) 0x02);

        assertFalse(controller.portIsMasked(0)); // port 0
        assertTrue(controller.portIsMasked(1));
        assertFalse(controller.portIsMasked(2));
        assertFalse(controller.portIsMasked(3));
        assertFalse(controller.portIsMasked(4));
        assertFalse(controller.portIsMasked(5));
        assertFalse(controller.portIsMasked(6));
        assertFalse(controller.portIsMasked(7));

        // 11110001
        controller.setPortPowerMask((byte) 0xf1);

        assertTrue(controller.portIsMasked(0)); // port 0
        assertFalse(controller.portIsMasked(1));
        assertFalse(controller.portIsMasked(2));
        assertFalse(controller.portIsMasked(3));
        assertTrue(controller.portIsMasked(4));
        assertTrue(controller.portIsMasked(5));
        assertTrue(controller.portIsMasked(6));
        assertTrue(controller.portIsMasked(7));
    }
}
