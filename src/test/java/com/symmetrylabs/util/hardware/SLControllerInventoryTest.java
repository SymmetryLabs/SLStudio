package com.symmetrylabs.util.hardware;

import org.junit.Test;

import java.io.FileNotFoundException;

import static org.junit.Assert.*;

public class SLControllerInventoryTest {

    @Test
    public void loadFromDisk() throws FileNotFoundException {
        SLControllerInventory slControllerInventory = new SLControllerInventory();
        slControllerInventory.loadFromDisk();
        assertNotNull(slControllerInventory.treeInventoryMap);
    }
}
