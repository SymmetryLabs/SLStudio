package com.symmetrylabs.controllers.symmeTreeController.infrastructure;

import com.symmetrylabs.shows.tree.AssignableTenereController;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.output.DiscoverableController;
import com.symmetrylabs.util.hardware.powerMon.MetaSample;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

public class AllPortsPowerEnableMaskTest {
    private AllPortsPowerEnableMask objUnderTest = new AllPortsPowerEnableMask();

    // some test objects
    Collection<DiscoverableController> treeControllers = new ArrayList<>();

    private void loadMockControllers() {
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
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void loadCurrentSculptureOutputStateTo_RAM() {
        loadMockControllers();
        objUnderTest.loadControllerSetMaskStateTo_RAM(treeControllers);
        assertNotNull(objUnderTest);
    }

    /**
     * Should set the state of all controllers to be equivalent to the mask.
     */
    @Test
    public void RAM_ApplyMaskAllControllers() {
        // For every controller in the show..
        objUnderTest = AllPortsPowerEnableMask.loadFromDisk();
        objUnderTest.RAM_ApplyMaskAllControllers(treeControllers);
        // set the power state to equal the mask.
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
        objUnderTest = AllPortsPowerEnableMask.loadFromDisk();
        assertNotNull(objUnderTest);
    }
}
