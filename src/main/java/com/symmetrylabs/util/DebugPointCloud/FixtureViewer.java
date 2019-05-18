package com.symmetrylabs.util.DebugPointCloud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FixtureViewer {

    Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .excludeFieldsWithoutExposeAnnotation()
        .create();

    class SimplePoint {
        @Expose
        public double[] point = new double[3];
        public SimplePoint(double x, double y, double z){
            point[0] = x;
            point[1] = y;
            point[2] = z;
        }
    }

    private List<SimplePoint> pointcloud = new ArrayList<SimplePoint>();

    public FixtureViewer(LXFixture cloud){
        for (LXPoint pt : cloud.getPoints()){
            pointcloud.add(new SimplePoint(pt.x, pt.y, pt.z));
        }
    }

    public void viewFixture() throws IOException {
        SimplePoint[] staticArray =  new SimplePoint[pointcloud.size()];
        staticArray = pointcloud.toArray(staticArray);

        String text = gson.toJson(staticArray);
        System.out.println("WRITING TO FILE:");
        System.out.println(text);
        try (PrintStream out = new PrintStream(new FileOutputStream("dynamic.json"))) {
            out.print(text);
        }
        Runtime rt = Runtime.getRuntime();
        //Process pr = rt.exec("cmd /c dir");

        String basePath = new File("").getAbsolutePath();
        System.out.println(basePath);
        String command = basePath+"/../openpixelcontrol/bin/gl_server -l " + basePath + "/dynamic.json";
        System.out.println(command);
        Process pr = rt.exec(command);
    }
}
