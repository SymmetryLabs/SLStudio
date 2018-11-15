package com.symmetrylabs.shows.flowers;

import com.google.gson.Gson;
import com.symmetrylabs.slstudio.model.SLModel;
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
import java.util.List;
import processing.core.PGraphics;
import java.util.HashSet;

public class FlowersModel extends SLModel implements LXModel.Listener {
    private ReadableObj model;
    private List<FlowerModel> flowers;
    private List<PanelRecord> panels;

    protected FlowersModel(List<FlowerModel> flowers, ReadableObj model) {
        super(new FlowerFixture(flowers));
        this.flowers = flowers;
        for (FlowerModel m : flowers) {
            m.addListener(this);
        }
        this.model = model;
        panels = new ArrayList<>();
        recalculatePanels();
    }

    public List<FlowerModel> getFlowers() {
        return flowers;
    }

    public List<PanelRecord> getPanels() {
        return panels;
    }

    public void recalculatePanels() {
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
    }

    @Override
    public void onModelUpdated(LXModel model) {
        recalculatePanels();
        update(true, false);
    }

    public void storeRecords() {
        List<FlowerRecord> records = new ArrayList<>();
        for (FlowerModel fm : flowers) {
            records.add(fm.getFlowerData().record);
        }
        try {
            FileWriter writer = new FileWriter(FlowersModelLoader.RECORD_FILE);
            new Gson().toJson(records, writer);
            writer.close();
            System.out.println("wrote records to " + FlowersModelLoader.RECORD_FILE.toString());
        } catch (IOException e) {
            System.err.println("couldn't write record file:");
            e.printStackTrace();
        }
    }

    public void panelize() {
        List<FlowerData> data = new ArrayList<>();
        for (FlowerModel fm : flowers) {
            data.add(fm.getFlowerData());
        }
        Panelizer.panelize(data);
    }

    private static class FlowerFixture extends LXAbstractFixture {
        public FlowerFixture(List<FlowerModel> models) {
            for (FlowerModel model : models) {
                points.addAll(model.getPoints());
            }
        }
    }
}
