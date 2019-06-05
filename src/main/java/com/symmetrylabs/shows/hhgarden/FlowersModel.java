package com.symmetrylabs.shows.hhgarden;

import com.google.gson.GsonBuilder;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.util.FileUtils;

import de.javagl.obj.ReadableObj;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import processing.core.PGraphics;

public class FlowersModel extends SLModel implements LXModel.Listener {
    private ReadableObj model;
    private List<FlowerModel> flowers;
    private List<PanelRecord> panels;
    private HashMap<Integer, HashMap<Integer, List<FlowerModel>>> flowerByHarness = new HashMap<>();

    protected FlowersModel(String showName, List<FlowerModel> flowers, ReadableObj model) {
        super(showName, new FlowerFixture(flowers));
        this.flowers = flowers;
        for (FlowerModel m : flowers) {
            m.addListener(this);
        }
        this.model = model;
        panels = new ArrayList<>();
        onDataUpdated();
    }

    public List<FlowerModel> getFlowers() {
        return flowers;
    }

    public List<PanelRecord> getPanels() {
        return panels;
    }

    public Set<Integer> getPixliteIds() {
        return flowerByHarness.keySet();
    }

    public Map<Integer, List<FlowerModel>> getPixliteHarnesses(Integer pixliteId) {
        if (!flowerByHarness.containsKey(pixliteId) || flowerByHarness.get(pixliteId) == null) {
            return null;
        }
        return flowerByHarness.get(pixliteId);
    }

    public void onDataUpdated() {
        HashSet<String> toRemove = new HashSet<>();
        HashSet<String> toAdd = new HashSet<>();
        for (FlowerModel fm : flowers) {
            toAdd.add(fm.getFlowerData().record.panelId);
        }
        for (PanelRecord pr : panels) {
            toAdd.remove(pr.id);
            toRemove.add(pr.id);
        }
        for (FlowerModel fm : flowers) {
            toRemove.remove(fm.getFlowerData().record.panelId);
        }
        panels.removeIf(r -> toRemove.contains(r.id));
        for (String id : toAdd) {
            panels.add(new PanelRecord(id));
        }
        Collections.sort(panels);

        flowerByHarness.clear();
        for (FlowerModel fm : flowers) {
            FlowerRecord fr = fm.getFlowerData().record;
            if (fr.pixliteId == FlowerRecord.UNKNOWN_PIXLITE_ID ||
                    fr.harness == FlowerRecord.UNKNOWN_HARNESS ||
                    fr.harnessIndex == FlowerRecord.UNKNOWN_HARNESS_INDEX) {
                continue;
            }

            if (!flowerByHarness.containsKey(fr.pixliteId)) {
                flowerByHarness.put(fr.pixliteId, new HashMap<>());
            }
            HashMap<Integer, List<FlowerModel>> harness = flowerByHarness.get(fr.pixliteId);
            if (!harness.containsKey(fr.harness)) {
                harness.put(fr.harness, emptyDataLine());
            }
            if (fr.harnessIndex != FlowerRecord.UNKNOWN_HARNESS_INDEX) {
                harness.get(fr.harness).set(fr.harnessIndex - 1, fm);
            }
        }
    }

    @Override
    public void onModelUpdated(LXModel model) {
        onDataUpdated();
        update(true, false);
    }

    public void storeRecords() {
        List<FlowerRecord> records = new ArrayList<>();
        for (FlowerModel fm : flowers) {
            records.add(fm.getFlowerData().record);
        }
        FileUtils.writeShowJson(FlowersModelLoader.RECORDS_FILENAME, records);
    }

    public void panelize() {
        List<FlowerData> data = new ArrayList<>();
        for (FlowerModel fm : flowers) {
            data.add(fm.getFlowerData());
        }
        Panelizer.panelize(data);
        Panelizer.harnessize(data);
    }

    private static class FlowerFixture extends LXAbstractFixture {
        public FlowerFixture(List<FlowerModel> models) {
            for (FlowerModel model : models) {
                points.addAll(model.getPoints());
            }
        }
    }

    static List<FlowerModel> emptyDataLine() {
        List<FlowerModel> fms = new ArrayList<>();
        for (int i = 0; i < FlowerRecord.MAX_HARNESS_SIZE; i++) {
            fms.add(null);
        }
        return fms;
    }
}
