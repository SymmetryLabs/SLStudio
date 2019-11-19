package com.symmetrylabs.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class InventoryWriterTest {

    @Test
    void loadPhysidToMac() {
        InventoryWriter writer = new InventoryWriter();
        writer.loadPhysidToMac();
    }

    @Test
    void writeOutDescriptors() {
        InventoryWriter writer = new InventoryWriter();
        writer.loadPhysidToMac();
        try {
            writer.writeOutDescriptors();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
