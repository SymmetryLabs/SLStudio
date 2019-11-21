package com.symmetrylabs.controllers.symmeTreeController.infrastructure;

import com.symmetrylabs.util.hardware.SLControllerInventory;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.*;

public class PersistentControllerByHumanIdMapTest {

    @Test
    public void indexController() {
    }

    @Test
    public void validateNetwork() {
    }

    @Test
    public void getErrors() {
    }

    @Test
    public void indexController1() {
    }

    @Test
    public void mergeOldPersistentControllerToSLControllerInventory() {
        PersistentControllerByHumanIdMap loadr = PersistentControllerByHumanIdMap.loadFromDisk();

        loadr.buildMacAddrMap();
        try {
            loadr.saveToDisk();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SLControllerInventory loadMerge = SLControllerInventory.loadFromDisk();

        for (String hID : loadr.slControllerIndex.keySet()){
            loadMerge.addNetworkDeviceByName(hID, loadr.slControllerIndex.get(hID));
        }
        loadMerge.rebuildAllControllers();
        try {
            loadMerge.writeInventoryToDisk();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertNotNull(loadr);
    }

    @Test
    public void loadFromDisk() {
        PersistentControllerByHumanIdMap loadr = PersistentControllerByHumanIdMap.loadFromDisk();
        assertNotNull(loadr);
    }
}
