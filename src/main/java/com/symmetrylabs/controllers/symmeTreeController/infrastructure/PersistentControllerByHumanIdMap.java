package com.symmetrylabs.controllers.symmeTreeController.infrastructure;

import com.google.gson.annotations.Expose;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.output.DiscoverableController;
import com.symmetrylabs.util.persistance.ClassWriterLoader;

import java.io.IOException;
import java.util.TreeMap;

// persistent port power mask mapped to controllers.  Should be indexed on a per-show basis.

public class PersistentControllerByHumanIdMap {
    private static String CONTROLLER_INDEX_FILENAME = "persistent-controller-by-human-id.json";

    @Expose
    private final TreeMap<String, NetworkDevice> slControllerIndex = new TreeMap<>();

    public void indexController(String newControllerID, DiscoverableController dc) throws IOException {
        slControllerIndex.put(newControllerID, dc.networkDevice);
        saveToDisk();
    }

    public void saveToDisk () throws IOException {
        new ClassWriterLoader<>(CONTROLLER_INDEX_FILENAME, PersistentControllerByHumanIdMap.class).writeObj(this);
    }

    public static PersistentControllerByHumanIdMap loadFromDisk (){
        return new ClassWriterLoader<>(CONTROLLER_INDEX_FILENAME, PersistentControllerByHumanIdMap.class).loadObj();
    }
}

