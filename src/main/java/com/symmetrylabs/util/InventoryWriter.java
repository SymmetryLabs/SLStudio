package com.symmetrylabs.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;


// delegate here to write metadata regarding inventory bits and pieces.
public class InventoryWriter {
    class PhysicalCube {
        public String addrA;
        public String idA;
        public String addrB;
        public String idB;

        /**
         * true if this is a legacy import cube
         */
        public boolean imported;
    }

    class DeviceMetadata {
        String productID;
        String label_id;
        String description;
    }

    public final HashMap<String, DeviceMetadata> descriptors = new HashMap<>();

    public final List<PhysicalCube> allCubes = null;

    InventoryWriter loadMe;

    public InventoryWriter(){

    }

    public void loadPhysidToMac(){
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        String jsonString = readLineByLineJava8("src/main/resources/cubeinventory.json");

        Gson gson = builder.create();
        loadMe = gson.fromJson(jsonString, InventoryWriter.class);
    }

    public void writeOutDescriptors () throws IOException {

        for (PhysicalCube cubeItem : loadMe.allCubes ){
            DeviceMetadata meta = new DeviceMetadata();
            // Side A
            meta.description = "none";
            meta.label_id = cubeItem.idA;
            meta.productID = "CubeController";
            descriptors.put(cubeItem.addrA, meta);
            // Side B
            meta.description = "none";
            meta.label_id = cubeItem.idB;
            meta.productID = "CubeController";
            descriptors.put(cubeItem.addrB, meta);
        }

        File resFile = new File("src/main/resources/device_inventory.json");
        JsonWriter writer = new JsonWriter(new FileWriter(resFile));
        writer.setIndent("  ");
        new GsonBuilder().create().toJson(this, InventoryWriter.class, writer);
        writer.close();
        System.out.println("cube inventory written to " + resFile);


    }


    private static String readLineByLineJava8(String filePath)
    {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }
}
