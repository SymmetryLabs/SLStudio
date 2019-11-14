package com.symmetrylabs.util.hardware;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.stream.JsonWriter;
import com.symmetrylabs.slstudio.output.DiscoverableController;

import java.io.*;
import java.util.TreeMap;

public class PhysicalSwitchLayout {
    private static String RESOURCE_DIR = "src/main/resources";
    private static String RESOURCE_FILENAME = "switchLayout.json";
    @Expose
    TreeMap<Integer, SLControllerInventory.ControllerMetadata> switchLayoutMap = new TreeMap<>();

    public static PhysicalSwitchLayout loadFromDisk(){
        ClassLoader cl = PhysicalSwitchLayout.class.getClassLoader();
        InputStream resourceStream = cl.getResourceAsStream(RESOURCE_FILENAME);
        if (resourceStream != null) {
            PhysicalSwitchLayout res = new Gson().fromJson(new InputStreamReader(resourceStream), PhysicalSwitchLayout.class);
            if (res != null) {
                return res;
            }
        }
        return new PhysicalSwitchLayout();
    }

    public void writeInventoryToDisk () throws IOException {

        File resFile = new File(RESOURCE_DIR, RESOURCE_FILENAME);
        JsonWriter writer = new JsonWriter(new FileWriter(resFile));
        writer.setIndent("  ");


        new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this, PhysicalSwitchLayout.class, writer);
        writer.close();
        System.out.println("switch layout written " + resFile);


    }

    public void putController(DiscoverableController cc) {
        switchLayoutMap.put(cc.switchPortNumber, new SLControllerInventory.ControllerMetadata(cc));
    }

}
