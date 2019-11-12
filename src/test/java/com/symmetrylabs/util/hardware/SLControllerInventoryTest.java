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
}
