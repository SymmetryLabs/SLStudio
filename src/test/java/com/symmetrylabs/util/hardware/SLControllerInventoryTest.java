package com.symmetrylabs.util.hardware;

import com.symmetrylabs.util.NetworkUtil.MACAddress;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class SLControllerInventoryTest {


    @Test
    public void allMacAddrsConformant() {
        ArrayList<String> keysToRemove = new ArrayList<>();
        int  numOverlap = 0;

        System.out.println("hi");
        SLControllerInventory slControllerInventory = SLControllerInventory.loadFromDisk();

        System.out.println("Size Initial: " + slControllerInventory.macAddrToControllerMetadataMap.size());
        for (String key : slControllerInventory.macAddrToControllerMetadataMap.keySet()){
            MACAddress mac;
            if (key.contains("::")){
                // ok we have the old style entry, create a new conformant entry.
                mac = MACAddress.valueOf(key);
                String newStyle = mac.toString();

                ControllerMetadata origValue = slControllerInventory.macAddrToControllerMetadataMap.get(key);

                // swap
                keysToRemove.add(key);
                if((slControllerInventory.macAddrToControllerMetadataMap.containsKey(newStyle))){
                    numOverlap++;
                }
                slControllerInventory.macAddrToControllerMetadataMap.put(newStyle, origValue);
            }
            else {
//                System.out.println(key);
            }
        }
        for (String key : keysToRemove){
            slControllerInventory.macAddrToControllerMetadataMap.remove(key);
        }
        System.out.println("Size End: " + slControllerInventory.macAddrToControllerMetadataMap.size());
        System.out.println("overlap: " + numOverlap);

        // ok do the refactor.
        try {
            slControllerInventory.writeInventoryToDisk();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
