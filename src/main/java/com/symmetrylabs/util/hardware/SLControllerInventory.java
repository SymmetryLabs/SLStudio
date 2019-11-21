package com.symmetrylabs.util.hardware;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.stream.JsonWriter;
//import com.symmetrylabs.slstudio.output.DiscoverableController;
import com.symmetrylabs.slstudio.output.DiscoverableController;
import com.symmetrylabs.util.NetworkUtil.MACAddress;
import heronarts.lx.parameter.DiscreteParameter;
import org.apache.commons.collections4.iterators.IteratorChain;

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
    private final static String PERSISTENT_SLCONTROLLER_INVENTORY = "slcontrollerinventory.json";

    private final transient List<SLControllerInventory.Listener> listeners = new ArrayList<>();

    @Expose
    public final ArrayList<ControllerMetadata> allControllers = new ArrayList<>();

    private transient List<String> inventoryErrors = new ArrayList<>();

    private transient HashMap<String, DiscoverableController> sl_controller_index = new HashMap<>();

    protected SLControllerInventory() {
//        allControllers = new ArrayList<DiscoverableController>();
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public String getControllerId(String deviceId) {
        //TODO: actually impliment SLController inventory.
//        return macAddrToControllerMetadataMap.get(deviceId).humanID;
        MACAddress macAddress = MACAddress.valueOf(deviceId);
        ControllerMetadata deviceMetadata = macAddrToControllerMetadataMap.get(macAddress.toString());
        if (deviceMetadata != null){
            return deviceMetadata.humanID;
        }
        return deviceId;
    }

    public String getNameByMac(String deviceId) {
        ControllerMetadata metadata = macAddrToControllerMetadataMap.get(deviceId);
        return metadata == null ? deviceId : metadata.humanID;
    }

    public Iterator<CharSequence> getErrors() {
        IteratorChain<CharSequence> iter = new IteratorChain<>();
        iter.addIterator(inventoryErrors.iterator());
        return iter;
    }

    public void rebuild() {
        // logic to rebuild inventory
    }

    public boolean save() {
        // logic to save
        return false;
    }

    public interface Listener {
        void onControllerListUpdated();
    }

    public String getHostAddressByControllerID(String controllerId) {
        ControllerMetadata controller = macAddrToControllerMetadataMap.get(controllerId);
        String hostAddr = controller == null ? "0.0.0.0" : controller.ipAddr.getHostAddress();
        return hostAddr;
    }


//    ArrayList<ControllerMetadata> treeInventory = new ArrayList<>();

    // Map to controller metadata based on MAC
    @Expose
    public TreeMap<String, ControllerMetadata> macAddrToControllerMetadataMap = new TreeMap<>();

    public final transient Map<String, ControllerMetadata> controllerByMacAddrs = new TreeMap<>();
    public final transient Map<String,ControllerMetadata> controllerByCtrlId = new TreeMap<>();
    public final transient Map<String, ControllerMetadata> controllerByIP = new TreeMap<>();

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
//                treeInventory.add(meta);
                macAddrToControllerMetadataMap.put(meta.humanID, meta);

            }
        }

        int count = 0;
        for(Map.Entry<String,ControllerMetadata> entry : macAddrToControllerMetadataMap.entrySet()) {
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
//        System.out.println(gson.toJson(macAddrToControllerMetadataMap));

        // need to do something here regarding saving a source distribution or not .. see CubeInventory class
        File resFile = new File("src/main/resources", TREE_INVENTORY_FILENAME);
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(resFile));
            writer.setIndent("  ");
            new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create().toJson(macAddrToControllerMetadataMap, macAddrToControllerMetadataMap.getClass(), writer);
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
//                treeInventory.add(meta);
                macAddrToControllerMetadataMap.put(meta.macAddrString, meta);
            }
        }
//        Gson gson = new GsonBuilder()
//            .setPrettyPrinting()
//            .excludeFieldsWithoutExposeAnnotation()
//            .create();
//        System.out.println(gson.toJson(macAddrToControllerMetadataMap));

        // need to do something here regarding saving a source distribution or not .. see CubeInventory class
        File resFile = new File("src/main/resources", MAC_TO_HUMAN_ID_FILENAME);
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(resFile));
            writer.setIndent("  ");
            new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create().toJson(macAddrToControllerMetadataMap, macAddrToControllerMetadataMap.getClass(), writer);
            writer.close();
            System.out.println("inventory map written to: " + resFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rebuildAllControllers() {
        allControllers.clear();
        for (String key : macAddrToControllerMetadataMap.keySet()){
            ControllerMetadata meta = macAddrToControllerMetadataMap.get(key);
            allControllers.add(meta);
        }
    }

    public void writeInventoryToDisk() throws IOException {

        File resFile = new File(RESOURCES_DIR, PERSISTENT_SLCONTROLLER_INVENTORY);
        JsonWriter writer = new JsonWriter(new FileWriter(resFile));
        writer.setIndent("  ");

        new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this, SLControllerInventory.class, writer);
        writer.close();
        System.out.println("inventory file writen " + resFile);
    }


    public static SLControllerInventory loadFromDisk() {
        ClassLoader cl = SLControllerInventory.class.getClassLoader();
        InputStream resourceStream = cl.getResourceAsStream(PERSISTENT_SLCONTROLLER_INVENTORY);
        if (resourceStream != null) {
            SLControllerInventory loadMe = new Gson().fromJson(new InputStreamReader(resourceStream), SLControllerInventory.class);
            return loadMe;
        }
        return new SLControllerInventory();
    }
}
