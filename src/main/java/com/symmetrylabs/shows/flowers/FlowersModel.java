package com.symmetrylabs.shows.flowers;

import com.symmetrylabs.slstudio.model.SLModel;
import de.javagl.obj.FloatTuple;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjFace;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ReadableObj;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import processing.core.PGraphics;

public class FlowersModel extends SLModel {
    private static final String GEOMETRY_FILE = "shows/flowers/locations.obj";
    private static final String ADDRESS_FILE = "shows/flowers/addressing.json";

    private ReadableObj model;
    private List<FlowerModel> flowers;

    protected FlowersModel(List<FlowerModel> flowers, ReadableObj model) {
        super(new FlowerFixture(flowers));
        this.model = model;
    }

    public static FlowersModel load() {
        ReadableObj model;
        try {
            InputStream in = new FileInputStream(SOURCE_FILE);
            model = ObjReader.read(in);
        } catch (IOException e) {
            System.err.println("could not read flowers point file:");
            e.printStackTrace();
            return null;
        }

        ArrayList<FlowerModel> children = new ArrayList<>();
        for (int vi = 0; vi < model.getNumVertices(); vi++) {
            FloatTuple v = model.getVertex(vi);
            children.add(FlowerModel.create(new LXVector(v.getX(), v.getY(), v.getZ())));
        }
        return new FlowersModel(children, model);
    }

    private static class FlowerFixture extends LXAbstractFixture {
        public FlowerFixture(List<FlowerModel> models) {
            for (FlowerModel model : models) {
                points.addAll(model.getPoints());
            }
        }
    }
}
