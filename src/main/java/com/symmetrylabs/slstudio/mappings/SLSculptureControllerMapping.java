package com.symmetrylabs.slstudio.mappings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.util.hardware.SLControllerInventory;

import java.io.*;
import java.util.*;


public class SLSculptureControllerMapping {
    public static final String CTRL_MAP_FILENAME = "sl-controller-mapping.json";

    public static final class PhysIdAssignment {
        public String modelId;
        public String humanID;

        protected PhysIdAssignment(String modelId, String humanID) {
            this.modelId = modelId;
            this.humanID = humanID;
        }

        @Override
        public String toString() {
            return String.format("phys %s = model %s", humanID, modelId);
        }
    }

    protected final List<PhysIdAssignment> assignments = new ArrayList<>();
    public transient final Map<String, PhysIdAssignment> assignmentsByHumanID = new HashMap<>();
    protected transient final Map<String, PhysIdAssignment> assignmentsByModelId = new HashMap<>();
    protected transient SLControllerInventory inventory;
    protected final String showName;

    protected SLSculptureControllerMapping() {
        this.showName = null;
        this.inventory = null;
    }

    protected SLSculptureControllerMapping(String showName, SLControllerInventory inventory) {
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
        return assignmentsByHumanID.get(physId);
    }

    public PhysIdAssignment lookUpByControllerId(String ctrlId) {
        if (assignmentsByHumanID.containsKey(ctrlId)) {
            return assignmentsByHumanID.get(ctrlId);
        }
        SLControllerInventory.ControllerMetadata pc = inventory.controllerByCtrlId.get(ctrlId);
        if (pc == null) return null;
        return assignmentsByHumanID.get(pc.getHumanID());
    }

    public PhysIdAssignment lookUpByModelID(String modelId) {
        return assignmentsByModelId.get(modelId);
    }

    public void setControllerAssignment(String modelId, String humanID) {
        /* if physical output was mapped to another model, remove that assignment */
        if (assignmentsByHumanID.containsKey(humanID)) {
            assignments.remove(assignmentsByHumanID.get(humanID));
        }
        /* if model was mapped to another physical output, update that assignment */
        if (!assignmentsByModelId.containsKey(modelId)) {
            assignments.add(new PhysIdAssignment(modelId, humanID));
        }
        /* otherwise create a new assignment */
        else {
            assignmentsByModelId.get(modelId).humanID = humanID;
        }
        /* rebuild lookup tables */
        onUpdate();
    }

    public void onUpdate() {
        assignmentsByHumanID.clear();
        assignmentsByModelId.clear();
        for (Iterator<PhysIdAssignment> iter = assignments.iterator(); iter.hasNext();){
            PhysIdAssignment ca = iter.next();
            assignmentsByHumanID.put(ca.humanID, ca);
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

    public static SLSculptureControllerMapping loadFromDisk(String showName, SLControllerInventory inventory) {
        File f = showFile(showName);
        if (f.exists()) {
            try {
                SLSculptureControllerMapping res = new Gson().fromJson(
                    new InputStreamReader(new FileInputStream(f)), SLSculptureControllerMapping.class);
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
            ApplicationState.setWarning("SL Controller Mapping", "no addressing information for show " + showName);
        }
        return new SLSculptureControllerMapping(showName, inventory);
    }

}
