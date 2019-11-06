package com.symmetrylabs.util;

import org.junit.Test;

import java.io.IOException;

public class TreeControllerInventoryWriterTest {

    @Test
    public void parseInRawData() {
        TreeControllerInventoryWriter inventoryWriter = new TreeControllerInventoryWriter();
        try {
            inventoryWriter.parseInRawToMapByHumanID();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void parseInRawToMacToHumanID() {
        TreeControllerInventoryWriter inventoryWriter = new TreeControllerInventoryWriter();
        try {
            inventoryWriter.parseInRawToMapByMAC();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
