package com.symmetrylabs.shows.hhgarden;

import com.google.gson.Gson;
import de.javagl.obj.FloatTuple;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ReadableObj;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FlowersModelLoader {
    private static final float RECORD_MATCH_SQDIST_INCHES = 3.f * 3.f;

    static final String GEOMETRY_FILE = "shows/hhgarden/locations.obj";
    static final File RECORD_FILE = new File("shows/hhgarden/records.json");
    static final File PANEL_FILE = new File("shows/hhgarden/panels.txt");

    public static FlowersModel load() {
        ReadableObj model;
        try {
            InputStream in = new FileInputStream(GEOMETRY_FILE);
            model = ObjReader.read(in);
        } catch (IOException e) {
            System.err.println("could not read flowers point file:");
            e.printStackTrace();
            return null;
        }

        List<FlowerData> flowerData = new ArrayList<>();
        for (int vi = 0; vi < model.getNumVertices(); vi++) {
            FloatTuple v = model.getVertex(vi);
            /* Change coordinate system to Z-up and LHS */
            flowerData.add(new FlowerData(new LXVector(v.getX(), v.getZ(), v.getY())));
        }

        if (RECORD_FILE.exists()) {
            try {
                FlowerRecord[] records = new Gson().fromJson(
                    new BufferedReader(new FileReader(RECORD_FILE)), FlowerRecord[].class);
                matchRecords(flowerData, records);
            } catch (IOException e) {
                System.err.println("could not read flower data file:");
                e.printStackTrace();
            }
        }
        addMissingRecords(flowerData);
        updateHeights(flowerData);

        ArrayList<FlowerModel> children = new ArrayList<>();
        for (FlowerData fd : flowerData) {
            children.add(FlowerModel.create(fd));
        }
        return new FlowersModel(children, model);
    }

    private static void matchRecords(List<FlowerData> data, FlowerRecord[] records) {
        for (FlowerData fd : data) {
            for (FlowerRecord r : records) {
                float sqdist = (float) (
                    Math.pow(fd.location.x - r.x, 2) +
                    /* compare Y against Z to compensate for the coordinate change on import */
                    Math.pow(fd.location.y - r.z, 2));
                if (sqdist < RECORD_MATCH_SQDIST_INCHES) {
                    fd.record = r;
                    break;
                }
            }
        }
    }

    private static void addMissingRecords(List<FlowerData> data) {
        int nextId = 0;
        for (FlowerData fd : data) {
            if (fd.record != null) {
                nextId = Integer.max(nextId, fd.record.id + 1);
            }
        }
        for (FlowerData fd : data) {
            if (fd.record != null) {
                continue;
            }
            fd.record = new FlowerRecord(nextId++, fd.location.x, fd.location.z);
        }
    }

    private static void updateHeights(List<FlowerData> data) {
        for (FlowerData fd : data) {
            fd.recalculateLocation();
        }
    }

}
