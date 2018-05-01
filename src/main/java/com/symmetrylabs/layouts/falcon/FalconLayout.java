package com.symmetrylabs.layouts.falcon;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.layouts.cubes.*;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.util.dispatch.Dispatcher;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;
import com.symmetrylabs.slstudio.model.Strip;
import heronarts.p3lx.ui.UI2dScrollContext;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.layouts.falcon.FalconStrip;


import static com.symmetrylabs.util.DistanceConstants.INCHES_PER_METER;
import static com.symmetrylabs.util.MathUtils.floor;
import static processing.core.PConstants.HALF_PI;


public class FalconLayout implements Layout {

    static final float INCHES_PER_METER = 39.3701f;

    static final float SPACING = 6f * 12f;

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 90;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;
    static final float PIXEL_PITCH_INCHES = 0.65616f;
    static final int NUM_STRIPS = 10;


//    static StripConfig[] STRIPS_CONFIG = {


//        new StripConfig("206", 5, 5, 5, 0, 0, 0, new FalconStrip.Metrics(100, 0.25f)),
//        new StripConfig("207", 10, 10, 10, 0, 0, 0,new FalconStrip.Metrics(100, 0.25f)),
    //    new StripConfig("208", 15, 15, 15, 0, 0, 0, new FalconStrip.Metrics(100, 0.25f)),

//  };
//
//    static class StripConfig {
//        String id;
//        //int numPoints;
//        //float spacing;
//        float x;
//        float y;
//        float z;
//        float xRot;
//        float yRot;
//        float zRot;
//        final FalconStrip.Metrics metrics;
//
//        StripConfig(String id, float x, float y, float z, float xRot, float yRot, float zRot, FalconStrip.Metrics metrics) {
//            this.id = id;
//        this.metrics = metrics;
//            this.x = x;
//            this.y = y;
//            this.z = z;
//            this.xRot = xRot;
//            this.yRot = yRot;
//            this.zRot = zRot;
//        }
//    }

    static Map<String, String> macToPhysid = new HashMap<>();
    static Map<String, String> physidToMac = new HashMap<>();

    public SLModel buildModel() {

        byte[] bytes = SLStudio.applet.loadBytes("physid_to_mac.json");
        if (bytes != null) {
            try {
                JsonObject json = new Gson().fromJson(new String(bytes), JsonObject.class);
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    macToPhysid.put(entry.getValue().getAsString(), entry.getKey());
                    physidToMac.put(entry.getKey(), entry.getValue().getAsString());
                }
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }

        // Any global transforms
        LXTransform globalTransform = new LXTransform();
        globalTransform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
        globalTransform.rotateY(globalRotationY * Math.PI / 180.);
        globalTransform.rotateX(globalRotationX * Math.PI / 180.);
        globalTransform.rotateZ(globalRotationZ * Math.PI / 180.);


        //List<CubesModel.Tower> towers = new ArrayList<>();
        //List<CubesModel.Cube> allCubes = new ArrayList<>();
//        int stripId = 0;

//        Strip[] allStripsArr = new Strip[STRIPS_CONFIG.length];

//    List<FalconStrip> falconstrips = new ArrayList<>();
//        for (StripConfig config : STRIPS_CONFIG) {
//
//            //List<Strip> strips = new ArrayList<>();
//            String id = config.id;
//            float x = config.x;
//            float z = config.z;
//            float y = config.y;
//
//            float xRot = config.xRot;
//            float yRot = config.yRot;
//            float zRot = config.zRot;
//            FalconStrip.Metrics metrics = config.metrics;
//            //int numPoints = config.numPoints;
//            //float spacing = config.spacing;
//
//            FalconStrip falconstrip = new FalconStrip(id, x, y, z, xRot, yRot, zRot, globalTransform, metrics);
//            falconstrips.add(falconstrip);
//
//            }

        return new MillenniumFalconModel("1", 0, 0, 0, 0, 0, 0, new FalconStrip.Metrics(100, PIXEL_PITCH_INCHES, NUM_STRIPS), globalTransform); // what is argument?
        //new StripConfig(),
    }


            // strip and transform.. ...

//            allStripsArr[stripId++] = strip;

            //Fixture fixture = (Fixture) this.fixtures.get(0);
            //this.strip.addAll(strips);

//            globalTransform.push();
//
//            globalTransform.translate(0, 1, 0);
//            globalTransform.rotateZ(HALF_PI);
//
//            List<LXPoint> points = new ArrayList<>();
//
//            for (int i = 0; i < config.numPoints; i++) {
//                LXPoint point = new LXPoint(globalTransform.x(), globalTransform.y(), globalTransform.z());
//                globalTransform.translate(config.spacing, 0 ,0);
//
//            }



//            for (LXPoint p : strip.points) {
//
//                points.add(p);
//            }

//            globalTransform.pop();

            //StripsModel.Strip.Type type = config.type;
            //for (int i = 0; i < config.id.length; i++){
                //float y = config.yValues[i];
//              Strip.Metrics metrics = new Strip.Metrics(config.numPoints, config.spacing);

                //allStrips.add(strip);
//            }
            //towers


//        private static class Fixture extends LXAbstractFixture {
//
//            private final List<Strip> strips = new ArrayList<>();
//
//            private Fixture(Strip.Metrics metrics, LXTransform transform) {
//
//            }
//        }
/*
        public static class Falcon extends StripsModel<Strip> {

            public final static int STRIPS_PER_FALCON = 10;

            public static class Metrics {
            final Strip.Metrics = numPoints;
            final Strip.Metrics = pixelPitch;
                public Metrics(Strip.Metrics numPoints, Strip.Metrics pixelPitch) {
                    this.numPoints = numPoints;
                    this.pixelPitch = pixelPitch;

            }
        }

            public Falcon(LXTransform transform){
                super(new Fixture(transform));

                Fixture fixture = (Fixture) this.fixtures.get(0);
                this.strips.addAll(fixture.strips);
            }


        private static class Fixture extends LXAbstractFixture {

            private final List<Strip> strips = new ArrayList<>();

            private Fixture(Metrics metrics, LXTransform transform) {
                transform.push();
                for (int i = 0; i < STRIPS_PER_FALCON; i++) {
                    //boolean isHorizontal = (i % 2 == 0);
//                    CubesStrip.Metrics stripMetrics = isHorizontal ? metrics.horizontal : metrics.vertical;
                    Strip strip = new Strip(i+"", stripMetrics, transform);
                    //CubesStrip strip = new CubesStrip(i+"", stripMetrics, transform);
                    this.strips.add(strip);
                    //transform.translate(isHorizontal ? metrics.horizontal.length : metrics.vertical.length, 0, 0);
                    transform.rotateZ(HALF_PI);
                    for (LXPoint p : strip.points) {
                        this.points.add(p);
                    }
                }
                transform.pop();
            }
        }
        */

        //StripsModel.Strip[] allStripsArr = new StripsModel.Strip;



    public void setupLx(SLStudioLX lx) {
//        instanceByLX.put(lx, new WeakReference<>(this));
//
//        final NetworkMonitor networkMonitor = NetworkMonitor.getInstance(lx).start();
//        final Dispatcher dispatcher = Dispatcher.getInstance(lx);
//
//
////        CubesController local_debug = new CubesController(lx, "localhost", "localdebug");
////        controllers.add(local_debug);
////        lx.addOutput(local_debug);
//
//
//        StripConfig config = new StripConfig(SP * 3.0f, (JUMP * 2) + 3, -SP * 5.5f, new String[]{"188"});
//        //List<CubesModel.Cube> cubes = new ArrayList<>();
//        float x = config.x;
//        float z = config.z;
//        float xRot = config.xRot;
//        float yRot = config.yRot;
//        float zRot = config.zRot;
//
//        //CubesModel.Cube.Type type = config.type;
//
//        LXTransform globalTransform = new LXTransform();
//        globalTransform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
//        globalTransform.rotateY(globalRotationY * Math.PI / 180.);
//        globalTransform.rotateX(globalRotationX * Math.PI / 180.);
//        globalTransform.rotateZ(globalRotationZ * Math.PI / 180.);
//        for (int i = 0; i < config.ids.length; i++) {
//            float y = config.yValues[i];
//            //CubesModel.Cube cube = new CubesModel.Cube(config.ids[i], x, y, z, xRot, yRot, zRot, globalTransform, type);
//            StripsModel.Strip strip = new StripsModel.strip(config.ids[i], x, y, z, xRot, yRot, zRot, globalTransform, type);
//        }
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
//        UI2dScrollContext utility = ui.rightPane.utility;
//        new UIOutputs(lx, ui, this, 0, 0, utility.getContentWidth()).addToContainer(utility);
//        new UIMappingPanel(lx, ui, 0, 0, utility.getContentWidth()).addToContainer(utility);
    }


}

