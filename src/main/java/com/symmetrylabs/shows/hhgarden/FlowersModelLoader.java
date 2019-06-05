package com.symmetrylabs.shows.hhgarden;

import com.symmetrylabs.util.FileUtils;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ReadableObj;
import heronarts.lx.transform.LXVector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FlowersModelLoader {
    private static final float RECORD_MATCH_SQDIST_INCHES = 3.f * 3.f;

    static final String GEOMETRY_FILENAME = "locations.obj";
    static final String RECORDS_FILENAME = "records.json";
    static final String PANEL_FILENAME = "panels.txt";
    static final String PIXLITE_FILENAME = "pixlites.txt";

    public static FlowersModel load(String showName) {
        ReadableObj model = FileUtils.readShowObj(GEOMETRY_FILENAME);
        if (model == null) return null;

        List<FlowerData> flowerData = new ArrayList<>();
        for (int vi = 0; vi < model.getNumVertices(); vi++) {
            FloatTuple v = model.getVertex(vi);
            /* Change coordinate system to Z-up and LHS */
            flowerData.add(new FlowerData(new LXVector(v.getX(), v.getZ(), v.getY())));
        }

        FlowerRecord[] records = FileUtils.readShowJson(RECORDS_FILENAME, FlowerRecord[].class);
        if (records != null) matchRecords(flowerData, records);
        addMissingRecords(flowerData);
        updateHeights(flowerData);

        ArrayList<FlowerModel> children = new ArrayList<>();
        for (FlowerData fd : flowerData) {
            children.add(FlowerModel.create(showName, fd));
        }
        return new FlowersModel(showName, children, model);
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
            fd.record = new FlowerRecord(nextId++, fd.location.x, fd.location.y);
        }
    }

    private static void updateHeights(List<FlowerData> data) {
        for (FlowerData fd : data) {
            fd.recalculateLocation();
        }
    }

}
