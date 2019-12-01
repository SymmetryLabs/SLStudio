package com.symmetrylabs.util.hardware;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class SLControllerInventoryTest {

    @Test
    public void loadFromDisk() throws IOException {
        SLControllerInventory slControllerInventory = SLControllerInventory.loadFromDisk();
        assertNotNull(slControllerInventory.macAddrToControllerMetadataMap);
    }

    @Test
    public void parseInRawToMapByHumanID() {
    }

    @Test
    public void parseInRawToMapByMAC() {
    }

    @Test
    public void writeToDisk() throws IOException {
        SLControllerInventory slControllerInventory = new SLControllerInventory();
        slControllerInventory.parseInRawToMapByMAC();
        slControllerInventory.writeInventoryToDisk();
    }

    @Test
    public void loadFromDisk1() {
    }

    @Test
    public void rebuildAllControllers() {
        SLControllerInventory slControllerInventory = SLControllerInventory.loadFromDisk();
        slControllerInventory.rebuildAllControllers();
        assertNotNull(slControllerInventory.allControllers);
        assertTrue(slControllerInventory.allControllers.size() > 0);

        try {
            slControllerInventory.writeInventoryToDisk();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    @Test
//    public void parseInRawData() {
//        SLControllerInventory inventoryWriter = new SLControllerInventory();
//        try {
//            inventoryWriter.parseInRawToMapByHumanID();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void parseInRawToMacToHumanID() {
//        SLControllerInventory inventoryWriter = new SLControllerInventory();
//        try {
//            inventoryWriter.parseInRawToMapByMAC();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
