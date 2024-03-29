package com.symmetrylabs.slstudio.output;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.io.IOException;
import com.symmetrylabs.util.hardware.CubeInventory;
import com.google.gson.GsonBuilder;
import java.io.File;
import com.google.gson.stream.JsonWriter;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.symmetrylabs.slstudio.ApplicationState;
import java.util.ArrayList;
import com.google.gson.Gson;


public class CubeModelControllerMapping {
    public static final String CTRL_MAP_FILENAME = "controller-mapping.json";

    public static final class PhysIdAssignment {
        public String modelId;
        public String physicalId;

        protected PhysIdAssignment(String modelId, String physicalId) {
            this.modelId = modelId;
            this.physicalId = physicalId;
        }

        @Override
        public String toString() {
            return String.format("phys %s = model %s", physicalId, modelId);
        }
    }

    protected final List<PhysIdAssignment> assignments = new ArrayList<>();
    protected transient final Map<String, PhysIdAssignment> assignmentsByPhysId = new HashMap<>();
    protected transient final Map<String, PhysIdAssignment> assignmentsByModelId = new HashMap<>();
    protected transient CubeInventory inventory;
    protected final String showName;

    protected CubeModelControllerMapping() {
        this.showName = null;
        this.inventory = null;
    }

    protected CubeModelControllerMapping(String showName, CubeInventory inventory) {
        this.showName = showName;
        this.inventory = inventory;
    }

    protected File showFile() {
        return showFile(showName);
    }

    protected static File showFile(String showName) {
        return new File("shows/" + showName, CTRL_MAP_FILENAME);
    }

    public PhysIdAssignment lookUpByPhysId(String physId) {
        return assignmentsByPhysId.get(physId);
    }

    public PhysIdAssignment lookUpByControllerId(String ctrlId) {
        if (assignmentsByPhysId.containsKey(ctrlId)) {
            return assignmentsByPhysId.get(ctrlId);
        }
        CubeInventory.PhysicalCube pc = inventory.cubeByCtrlId.get(ctrlId);
        if (pc == null) return null;
        return assignmentsByPhysId.get(pc.getPhysicalId());
    }

    public PhysIdAssignment lookUpModel(String modelId) {
        return assignmentsByModelId.get(modelId);
    }

    public void setControllerAssignment(String modelId, String physId) {
        /* if physical output was mapped to another model, remove that assignment */
        if (assignmentsByPhysId.containsKey(physId)) {
            assignments.remove(assignmentsByPhysId.get(physId));
        }
        /* if model was mapped to another physical output, update that assignment */
        if (!assignmentsByModelId.containsKey(modelId)) {
            assignments.add(new PhysIdAssignment(modelId, physId));
        }
        /* otherwise create a new assignment */
        else {
            assignmentsByModelId.get(modelId).physicalId = physId;
        }
        /* rebuild lookup tables */
        onUpdate();
    }

    public void onUpdate() {
        assignmentsByPhysId.clear();
        assignmentsByModelId.clear();
        for (PhysIdAssignment ca : assignments) {
            assignmentsByPhysId.put(ca.physicalId, ca);
            assignmentsByModelId.put(ca.modelId, ca);
        }
    }

    public boolean save() {
        ClassLoader cl = getClass().getClassLoader();
        File resFile = showFile();
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(resFile));
            writer.setIndent("  ");
            new GsonBuilder().create().toJson(this, getClass(), writer);
            writer.close();
            System.out.println("cube controller map written to " + resFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static CubeModelControllerMapping loadFromDisk(String showName, CubeInventory inventory) {
        File f = showFile(showName);
        if (f.exists()) {
            try {
                CubeModelControllerMapping res = new Gson().fromJson(
                    new InputStreamReader(new FileInputStream(f)), CubeModelControllerMapping.class);
                if (res != null) {
                    res.inventory = inventory;
                    res.onUpdate();
                    System.out.println("loaded mapping for show " + res.showName);
                    return res;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            ApplicationState.setWarning("CubeModelControllerMapping", "no addressing information for show " + showName);
        }
        return new CubeModelControllerMapping(showName, inventory);
    }
}
