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
    public void loadFromDisk() {
        PersistentControllerByHumanIdMap loadr = PersistentControllerByHumanIdMap.loadFromDisk();

        loadr.buildMacAddrMap();
        try {
            loadr.saveToDisk();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SLControllerInventory loadMerge = SLControllerInventory.loadFromDisk();

        assertNotNull(loadr);
    }
}
