package com.symmetrylabs.util.hardware;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
//import com.symmetrylabs.slstudio.output.AbstractSLControllerBase;
import com.symmetrylabs.util.NetworkUtil.MACAddress;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class SLControllerInventory {
    private final static String RESOURCES_DIR = "src/main/resources";
    private final static String RAW_CONTROLLER_PHYSIDS = "src/main/resources/tree_controllers.raw.txt";
    private final static String TREE_INVENTORY_FILENAME = "tree-inventory.json";
    private final static String MAC_TO_HUMAN_ID_FILENAME = "mac_to_humanID.json";

    private final transient List<SLControllerInventory.Listener> listeners = new ArrayList<>();

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public String getControllerId(String deviceId) {
        return treeInventoryMap.get(deviceId).humanID;
    }

    public interface Listener {
        void onControllerListUpdated();
    }

    public class ControllerMetadata{
        @Expose
        Inet4Address ipAddr;

        MACAddress macAddress;
        @Expose
        String macAddr;

        @Expose
        String humanID;

        @Expose
        String statusNotes;

        public ControllerMetadata(String[] chunkArr) {
            if (chunkArr.length > 4|| chunkArr.length < 3) {
                throw new IllegalStateException("Chunk malformed, incorrect number data elts.");
            }
            try {
                ipAddr = (Inet4Address) InetAddress.getByName(chunkArr[0]);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            macAddress = MACAddress.valueOf(chunkArr[1]);
            macAddr = macAddress.toString();
            humanID = chunkArr[2];
            statusNotes = chunkArr[3] == null ? "null" : chunkArr[3];
        }

        public String getHumanID() {
            return humanID;
        }
    }

    ArrayList<ControllerMetadata> treeInventory = new ArrayList<>();

    public TreeMap<String, ControllerMetadata> treeInventoryMap = new TreeMap<>();

    public final transient Map<String, ControllerMetadata> controllerByMacAddrs = new TreeMap<>();
    public final transient Map<String,ControllerMetadata> controllerByCtrlId = new TreeMap<>();
    public final transient Map<String, ControllerMetadata> controllerByIP = new TreeMap<>();

    public SLControllerInventory(){
        try {
            this.loadFromDisk();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void onUpdated() {
        for (SLControllerInventory.Listener l : listeners) {
            l.onControllerListUpdated();
        }
    }
    public void parseInRawToMapByHumanID() throws IOException {
        FileReader r=new FileReader(RAW_CONTROLLER_PHYSIDS);
        BufferedReader br=new BufferedReader(r);
        Scanner s= new Scanner(br);

        String chunkArr[] = new String[4];
        int idx = 0;
        while (s.hasNext()){
            String line = s.nextLine();
            if (!line.isEmpty() && idx < 4){
                chunkArr[idx++] = line;
            }
            else {
                if (idx < 3){
                    idx = 0;
                    continue; // don't take the bad entries (only have two lines)
                }
                ControllerMetadata meta = new ControllerMetadata(chunkArr);
                idx = 0;
                treeInventory.add(meta);
                treeInventoryMap.put(meta.humanID, meta);

            }
        }

        int count = 0;
        for(Map.Entry<String,ControllerMetadata> entry : treeInventoryMap.entrySet()) {
            String key = entry.getKey();
            ControllerMetadata value = entry.getValue();

            String ipAddrString = value.ipAddr.toString();
            if (controllerByIP.containsKey(ipAddrString)){
                System.out.println("contains: " + ipAddrString);
                System.out.println(controllerByIP.get(ipAddrString).macAddr);
                System.out.println(value.macAddr);
                System.out.println(value.humanID);
                System.out.println(controllerByIP.get(ipAddrString).humanID);
                System.out.println(count++);
            }
            controllerByIP.put(ipAddrString, value);

//            System.out.println(key + " => " + value);
        }
//        Gson gson = new GsonBuilder()
//            .setPrettyPrinting()
//            .excludeFieldsWithoutExposeAnnotation()
//            .create();
//        System.out.println(gson.toJson(treeInventoryMap));

        // need to do something here regarding saving a source distribution or not .. see CubeInventory class
        File resFile = new File("src/main/resources", TREE_INVENTORY_FILENAME);
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(resFile));
            writer.setIndent("  ");
            new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create().toJson(treeInventoryMap, treeInventoryMap.getClass(), writer);
            writer.close();
            System.out.println("inventory map written to: " + resFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseInRawToMapByMAC() throws IOException {
        FileReader r=new FileReader(RAW_CONTROLLER_PHYSIDS);
        BufferedReader br=new BufferedReader(r);
        Scanner s= new Scanner(br);

        String chunkArr[] = new String[4];
        int idx = 0;
        while (s.hasNext()){
            String line = s.nextLine();
            if (!line.isEmpty() && idx < 4){
                chunkArr[idx++] = line;
            }
            else {
                if (idx < 3){
                    idx = 0;
                    continue; // don't take the bad entries (only have two lines)
                }
                ControllerMetadata meta = new ControllerMetadata(chunkArr);
                idx = 0;
                treeInventory.add(meta);
                treeInventoryMap.put(meta.macAddr, meta);
            }
        }
//        Gson gson = new GsonBuilder()
//            .setPrettyPrinting()
//            .excludeFieldsWithoutExposeAnnotation()
//            .create();
//        System.out.println(gson.toJson(treeInventoryMap));

        // need to do something here regarding saving a source distribution or not .. see CubeInventory class
        File resFile = new File("src/main/resources", MAC_TO_HUMAN_ID_FILENAME);
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(resFile));
            writer.setIndent("  ");
            new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create().toJson(treeInventoryMap, treeInventoryMap.getClass(), writer);
            writer.close();
            System.out.println("inventory map written to: " + resFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromDisk() throws FileNotFoundException {
        JsonReader reader = new JsonReader(new FileReader(RESOURCES_DIR + "/" + TREE_INVENTORY_FILENAME));
        treeInventoryMap = new Gson().fromJson(reader, treeInventoryMap.getClass());
    }
}
