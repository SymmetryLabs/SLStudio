package com.symmetrylabs.controllers.symmeTreeController.infrastructure;

import com.google.gson.annotations.Expose;
import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.output.DiscoverableController;
import com.symmetrylabs.util.persistance.ClassWriterLoader;
import org.apache.commons.collections4.iterators.IteratorChain;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

// persistent port power mask mapped to controllers.  Should be indexed on a per-show basis.

public class PersistentControllerByHumanIdMap {
    private static String CONTROLLER_INDEX_FILENAME = "persistent-controller-by-human-id.json";

    @Expose
    final TreeMap<String, String> macAddrToHumanIdMap = new TreeMap<>();
    @Expose
    final TreeMap<String, NetworkDevice> slControllerIndex = new TreeMap<>();

    @Expose
    final TreeMap<String, NetworkDevice> macIndex = new TreeMap<>();

    private transient HashMap<InetAddress, String> controllerByIP = new HashMap<>();
    private transient List<String> networkErrors = new ArrayList<>();

    public void validateNetwork(){
        controllerByIP.clear();

        for (String key : slControllerIndex.keySet()){
            NetworkDevice deviceToCheck = slControllerIndex.get(key);
            if (controllerByIP.containsKey(deviceToCheck.ipAddress)){
                networkErrors.add("Conflict! " + deviceToCheck.ipAddress  +  "  Controller " + key + " matches IP address with " + controllerByIP.get(deviceToCheck.ipAddress));
            }
            controllerByIP.put(deviceToCheck.ipAddress, key);
        }
        onErrorMessagesUpdated();
    }

    @NotNull
    public Iterator<CharSequence> getErrors() {
        IteratorChain<CharSequence> iter = new IteratorChain<>();
        iter.addIterator(networkErrors.iterator());
        return iter;
    }

    private String getErrorString() {
        String res = String.join("\n", this::getErrors);
        if (res.length() == 0) {
            return null;
        }
        return res;
    }
    private void onErrorMessagesUpdated() {
        ApplicationState.setWarning("CubeInventory", getErrorString());
    }

    public void indexController(String newControllerID, DiscoverableController dc) throws IOException {
        slControllerIndex.put(newControllerID, dc.networkDevice);
        macIndex.put(dc.networkDevice.deviceId, dc.networkDevice);
        validateNetwork();
        saveToDisk();
    }

    public void saveToDisk() throws IOException {
        new ClassWriterLoader<>(CONTROLLER_INDEX_FILENAME, PersistentControllerByHumanIdMap.class).writeObj(this);
    }

    public static PersistentControllerByHumanIdMap loadFromDisk (){
        return new ClassWriterLoader<>(CONTROLLER_INDEX_FILENAME, PersistentControllerByHumanIdMap.class).loadObj();
    }

    public String getNameByMac(String deviceId) {
        return macAddrToHumanIdMap.get(deviceId);
    }

    public void buildMacAddrMap() {
        macAddrToHumanIdMap.clear();

        for (String key : slControllerIndex.keySet()){
            NetworkDevice deviceToCheck = slControllerIndex.get(key);
            macAddrToHumanIdMap.put(deviceToCheck.deviceId, key);
        }

    }
}

