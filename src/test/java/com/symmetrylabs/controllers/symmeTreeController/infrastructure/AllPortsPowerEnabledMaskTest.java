package com.symmetrylabs.controllers.symmeTreeController.infrastructure;

import com.symmetrylabs.shows.tree.AssignableTenereController;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.output.AbstractSLControllerBase;
import com.symmetrylabs.util.hardware.powerMon.MetaSample;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AllPortsPowerEnabledMaskTest {
    AllPortsPowerEnabledMask objUnderTest = new AllPortsPowerEnabledMask();

    @Test
    public void loadCurrentSculptureOutputStateTo_RAM() {
        List<AbstractSLControllerBase> treeControllers = new ArrayList<>();
        AssignableTenereController controller = null;
        try {
            for (int i = 0; i < 4; i++){
                controller = new AssignableTenereController(null, new NetworkDevice(InetAddress.getByName("10.2.3." + i), "123", "2", "a10" + i, new String[]{"cool", "feature", "bro"}), null);

                // pretend we just got a power reading...
                MetaSample pretendSample = new MetaSample();
                pretendSample.powerOnStateMask = 0xf0;

                controller.writeSample(pretendSample);

                treeControllers.add(controller);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        objUnderTest.loadControllerSetMaskStateTo_RAM(treeControllers);
        assertNotNull(objUnderTest);
    }

    @Test
    public void RAM_ApplyMaskAllControllers() {
    }

    @Test
    public void saveToDisk() {
        loadCurrentSculptureOutputStateTo_RAM();

        try {
            objUnderTest.saveToDisk();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void loadFromDisk() {
        AllPortsPowerEnabledMask objUnderTest = AllPortsPowerEnabledMask.loadFromDisk();
        assertNotNull(objUnderTest);
    }
}
