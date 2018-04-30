//package com.symmetrylabs.slstudio.model;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.google.gson.annotations.Expose;
//import com.google.gson.stream.JsonWriter;
//import com.symmetrylabs.util.DebugPointCloud.FixtureViewer;
//import heronarts.lx.model.LXFixture;
//import heronarts.lx.model.LXPoint;
//import org.junit.jupiter.api.Test;
//import sun.java2d.pipe.SpanShapeRenderer;
//
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.PrintStream;
//import java.util.Arrays;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class StripTease {
//    Gson gson = new GsonBuilder()
//        .setPrettyPrinting()
//        .excludeFieldsWithoutExposeAnnotation()
//        .create();
//
//    class SimplePoint {
//        @Expose
//        public double[] point = new double[3];
//        public SimplePoint(double x, double y, double z){
//            point[0] = x;
//            point[1] = y;
//            point[2] = z;
//        }
//    }
//
//    @Test
//    public void testFixtureViewer() throws IOException {
//        StripForm strip = new StripForm("testStrip", new StripForm.Metrics(20, .2));
//        StripForm strip2 = new StripForm("testStrip", new StripForm.Metrics(50, .1));
//        LXFixture[] yeee =  {strip, strip2};
//        SLModel model = new SLModel(yeee);
////        JsonWriter writer = new JsonWriter(new FileWriter("data/test.json"));
//
//        FixtureViewer viewer = new FixtureViewer(model);
//        viewer.viewFixture();
////        System.out.println(gson.toJson(strip));
//    }
//
//    @Test
//    public void RenderObject() throws IOException {
//        System.out.println("Something that renders");
//
//        SimplePoint[] pts = new SimplePoint[10];
//        for (int i = 0; i < 10; i++){
//            pts[i] = new SimplePoint(0 + i % 4,0 + i,1.2);
//        }
//
////        JsonWriter writer = new JsonWriter(new FileWriter("data/test.json"));
//
//        String text = gson.toJson(pts);
//        try (PrintStream out = new PrintStream(new FileOutputStream("dynamic.json"))) {
//            out.print(text);
//        }
//    }
//}
