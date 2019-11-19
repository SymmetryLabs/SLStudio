package com.symmetrylabs.util;

import com.symmetrylabs.util.hardware.SLControllerInventory;
import org.junit.Test;

import java.io.IOException;

public class SLControllerInventoryTest {

    @Test
    public void parseInRawData() {
        SLControllerInventory inventoryWriter = new SLControllerInventory();
        try {
            inventoryWriter.parseInRawToMapByHumanID();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void parseInRawToMacToHumanID() {
        SLControllerInventory inventoryWriter = new SLControllerInventory();
        try {
            inventoryWriter.parseInRawToMapByMAC();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
