package com.symmetrylabs.layouts.cubes;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.lang.ref.WeakReference;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.LX;
import heronarts.lx.output.FadecandyOutput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.util.NetworkUtils;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.listenable.ListenableList;
import com.symmetrylabs.util.listenable.ListListener;
import heronarts.p3lx.ui.UI2dScrollContext;

/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class CubesLayout implements Layout {
    ListenableList<CubesController> controllers = new ListenableList<>();

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;

    static final float objOffsetX = 0;
    static final float objOffsetY = 0;
    static final float objOffsetZ = 0;

    static final float objRotationX = 0;
    static final float objRotationY = 0;
    static final float objRotationZ = 0;

    static final float CUBE_WIDTH = 24;
    static final float CUBE_HEIGHT = 24;
    static final float TOWER_WIDTH = 24;
    static final float TOWER_HEIGHT = 24;
    static final float CUBE_SPACING = 2.5f;

    static final float TOWER_VERTICAL_SPACING = 2.5f;
    static final float TOWER_RISER = 14;
    static final float SP = 24; //24+9;
    static final float SPV = 24+2; //24+9;
    static final float JUMP = TOWER_HEIGHT+TOWER_VERTICAL_SPACING;

    static final float INCHES_PER_METER = 39.3701f;

    // static final BulbConfig[] BULB_CONFIG = {
    //     // new BulbConfig("lifx-1", -50, 50, -30),
    //     // new BulbConfig("lifx-2", 0, 50, 0),
    //     // new BulbConfig("lifx-3", -65, 20, -100),
    //     // new BulbConfig("lifx-4", 0, 0, 0),
    //     // new BulbConfig("lifx-5", 0, 0, 0),
    // };

    static final float djBoothOffsetX = 0;
    static final float djBoothOffsetY = 0;
    static final float djBoothOffsetZ = 0;

    static final float djBoothRotationX = 0;
    static final float djBoothRotationY = -45;
    static final float djBoothRotationZ = 0;

    static final TowerConfig[] DJ_BOOTH_CUBES = {
        new TowerConfig(SP*0.0f, SPV*0.0f,  SP*0.0f, 0f, new String[] { "43", "362" }),
        new TowerConfig(SP*1.0f, SPV*1.5f,  SP*0.5f, 0f, new String[] { "138" }),
        new TowerConfig(SP*0.6f, SPV*2.0f,  SP*1.5f, 0f, new String[] { "157" }),
        new TowerConfig(SP*1.6f, SPV*2.5f,  SP*1.0f, 0f, new String[] { "94" }),
        new TowerConfig(SP*2.0f, SPV*0.0f,  SP*0.0f, 0f, new String[] { "199", "361", "6", "155" }),
        new TowerConfig(SP*1.5f, SPV*0.5f, -SP*1.0f, 0f, new String[] { "81" }),
        new TowerConfig(SP*3.0f, SPV*1.5f, -SP*0.5f, 0f, new String[] { "37" }),
        new TowerConfig(SP*3.0f, SPV*3.5f, -SP*0.5f, 0f, new String[] { "314" }),
        new TowerConfig(SP*2.5f, SPV*2.5f, -SP*1.0f, 0f, new String[] { "44" }),
        new TowerConfig(SP*3.5f, SPV*0.0f, -SP*1.5f, 0f, new String[] { "17", "56", "85", "391", "182" }),
        new TowerConfig(SP*4.0f, SPV*1.5f, -SP*2.5f, 0f, new String[] { "131" }),
        new TowerConfig(SP*4.5f, SPV*2.5f, -SP*2.0f, 0f, new String[] { "120" }),
        new TowerConfig(SP*4.0f, SPV*3.5f, -SP*2.5f, 0f, new String[] { "365" }),
        new TowerConfig(SP*5.0f, SPV*0.0f, -SP*3.0f, 0f, new String[] { "420", "126", "54", "548", "129" }),
        new TowerConfig(SP*4.5f, SPV*0.5f, -SP*4.0f, 0f, new String[] { "15" }),
        new TowerConfig(SP*6.0f, SPV*1.5f, -SP*3.5f, 0f, new String[] { "326" }),
        new TowerConfig(SP*6.0f, SPV*3.5f, -SP*3.5f, 0f, new String[] { "196" }),
        new TowerConfig(SP*5.6f, SPV*2.5f, -SP*4.0f, 0f, new String[] { "51" }),
        new TowerConfig(SP*4.5f, SPV*2.0f, -SP*4.5f, 0f, new String[] { "22" }),
        new TowerConfig(SP*6.5f, SPV*0.0f, -SP*4.5f, 0f, new String[] { "372", "406hp", "412", "111" }),
        new TowerConfig(SP*5.5f, SPV*1.5f, -SP*5.0f, 0f, new String[] { "397" }),
        new TowerConfig(SP*7.0f, SPV*2.5f, -SP*5.5f, 0f, new String[] { "135" }),
        new TowerConfig(SP*5.0f, SPV*1.0f, -SP*6.0f, 0f, new String[] { "356", "14" }),
        new TowerConfig(SP*5.5f, SPV*1.5f, -SP*7.0f, 0f, new String[] { "39" }),

    };

    static final float windowOffsetX = 350;
    static final float windowOffsetY = 0;
    static final float windowOffsetZ = 0;

    static final float windowRotationX = 0;
    static final float windowRotationY = -45;
    static final float windowRotationZ = 0;

    static final TowerConfig[] WINDOW_CUBES = {
        // new TowerConfig(SPACING*0.0, SPACING*0.0, -SPACING*0.0, -45, new String[] { "" }),

        new TowerConfig(SP*0.0f, SPV*1.5f, -SP*0.0f, 0f, new String[] { "128" }),
        new TowerConfig(SP*0.5f, SPV*0.5f, -SP*0.5f, 0f, new String[] { "159" }),
        new TowerConfig(SP*0.5f, SPV*2.5f, -SP*0.5f, 0f, new String[] { "61" }),
        new TowerConfig(SP*1.0f, SPV*0.0f, -SP*0.5f, 0f, new String[] { "91", "357", "367", "71" }),
        new TowerConfig(SP*2.0f, SPV*0.5f, -SP*1.0f, 0f, new String[] { "186" }),
        new TowerConfig(SP*2.0f, SPV*2.5f, -SP*1.0f, 0f, new String[] { "191" }),
        new TowerConfig(SP*3.0f, SPV*0.0f, -SP*1.5f, 0f, new String[] { "320", "46", "137" }),
        new TowerConfig(SP*2.5f, SPV*1.5f, -SP*0.5f, 0f, new String[] { "337" }),
        new TowerConfig(SP*3.5f, SPV*0.5f, -SP*2.5f, 0f, new String[] { "184" }),
        new TowerConfig(SP*4.0f, SPV*1.5f, -SP*2.0f, 0f, new String[] { "23" }),
    };

    static final float cornerRocksOffsetX = -180;
    static final float cornerRocksOffsetY = 0;
    static final float cornerRocksOffsetZ = -50;

    static final float cornerRocksRotationX = 0;
    static final float cornerRocksRotationY = 75+180;
    static final float cornerRocksRotationZ = 0;

    static final TowerConfig[] CORNER_ROCKS_CUBES = {
        // new TowerConfig(SPACING*0.0f, SPACING*0.0f, -SPACING*0.0f, 0f, new String[] { "" }),

        new TowerConfig(SP*0.0f, SPV*0.0f, -SP*0.0f, 0f, new String[] { "384", "172", "29", "5" }),
        new TowerConfig(-SP*1.0f, SPV*1.5f, SP*0.5f, 0f, new String[] { "132" }),
        new TowerConfig(-SP*0.5f, SPV*2.5f, SP*1.0f, 0f, new String[] { "d8:80:39:9b:23:ad" }), // right?
        new TowerConfig(SP*1.0f, SPV*2.5f, -SP*0.5f, 0f, new String[] { "55" }),
        new TowerConfig(SP*0.5f, SPV*1.5f, -SP*1.0f, 0f, new String[] { "?" }), // need to figure out
        new TowerConfig(SP*1.0f, SPV*0.0f, -SP*2.0f, 0f, new String[] { "82", "9", "70" }),
        new TowerConfig(SP*0.0f, SPV*0.5f, -SP*1.5f, 0f, new String[] { "177" }),
        new TowerConfig(SP*1.5f, SPV*1.5f, -SP*3.0f, 0f, new String[] { "163" }),
    };

    static final float rocksOffsetX = -260;
    static final float rocksOffsetY = 0;
    static final float rocksOffsetZ = -250;

    static final float rocksRotationX = 0;
    static final float rocksRotationY = 90;
    static final float rocksRotationZ = 0;

    static final TowerConfig[] ROCKS_CUBES = {
        // new TowerConfig(SPACING*0.0f, SPACING*0.0f, -SPACING*0.0f, 0f, new String[] { "" }),

        new TowerConfig(SP*0.0f, SPV*0.0f, -SP*0.0f, 45f, new String[] { "336", "79", "203" }),
        new TowerConfig(CubesModel.Cube.Type.MEDIUM, SP*0.0f, SPV*2.0f+3f, -SP*0.0f, 45f, new String[] { "393" }), // medium

        new TowerConfig(SP*3.0f, SPV*0.0f, -SP*0.0f, 45f, new String[] { "62", "187", "390" }),
        //new TowerConfig(CubesModel.Cube.Type.MEDIUM, SP*3.0f, SPV*2.0f+3f, -SP*0.0f, 45f, new String[] { "" }), // medium

        new TowerConfig(SP*6.0f, SPV*0.0f, -SP*0.0f, 45f, new String[] { "419", "106", "408" }),
        new TowerConfig(CubesModel.Cube.Type.MEDIUM, SP*6.0f, SPV*2.0f+3f, -SP*0.0f, 45f, new String[] { "100" }), // medium
    };

    static final TowerConfig[] SINGLE_TOWERS_CUBES = {
        // new TowerConfig(SPACING*0.0f, SPACING*0.0f, -SPACING*0.0f, 0f, new String[] { "" }),

        // with trees
        // closer to dj
        new TowerConfig(SP*0.0f, SPV*0.0f, -SP*8.0f, 180f, new String[] { "50", "35" }),
        // further from dj
        new TowerConfig(SP*0.0f, SPV*0.0f, -SP*17.5f, 90f, new String[] { "1", "108" }),

        // doorway
        new TowerConfig(SP*15.0f, SPV*0.0f, -SP*8.0f, 0f, new String[] { "312", "141", "57", "30" }),
        new TowerConfig(CubesModel.Cube.Type.MEDIUM, SP*15.0f, SPV*3.0f+3f, -SP*8.0f, 0f, new String[] { "334" }),

        new TowerConfig(SP*14.0f, SPV*0.0f, -SP*17.5f, 90f, new String[] { "68", "16", "398", "65" }),
        new TowerConfig(CubesModel.Cube.Type.MEDIUM, SP*14.0f, SPV*3.0f+3f, -SP*17.5f, 90f, new String[] { "327" }), // ??

        new TowerConfig(SP*15.0f, SPV*0.0f, -SP*22f, 90f, new String[] { "63", "87", "76" }),

        // bar
        // right
        new TowerConfig(SP*2.0f, SPV*0.0f, -SP*24.0f, 0f, new String[] { "21", "18", "383" }),
        new TowerConfig(CubesModel.Cube.Type.MEDIUM, SP*2.0f, SPV*2.0f+3f, -SP*24.0f, 0f, new String[] { "143" }),

        // left
        new TowerConfig(SP*9.0f, SPV*0.0f, -SP*24.0f, 0f, new String[] { "351", "174", "31" }),
        new TowerConfig(CubesModel.Cube.Type.MEDIUM, SP*9.0f, SPV*2.0f+3f, -SP*24.0f, 0f, new String[] { "210" }),

        // roof
        new TowerConfig(SP*11.0f, SPV*5.0f, -SP*19f, 180f, new String[] { "343", "66" }),
        new TowerConfig(SP*11.5f, SPV*5.0f, -SP*20.4f, 90f, new String[] { "113" }),
        new TowerConfig(SP*7.5f, SPV*5.0f, -SP*20.4f, 90f, new String[] { "340" }),
    };

    static final StripConfig[] STRIP_CONFIG = {
        // controller-id x y z x-rot y-rot z-rot num-leds pitch-in-inches
        //new StripConfig("206", 0, 0, 0, 0, 0, 0, 10, 0.25),
    };

    static class StripConfig {
        String id;
        int numPoints;
        float spacing;
        float x;
        float y;
        float z;
        float xRot;
        float yRot;
        float zRot;

        StripConfig(String id, float x, float y, float z, float xRot, float yRot, float zRot, int numPoints, float spacing) {
            this.id = id;
            this.numPoints = numPoints;
            this.spacing = spacing;
            this.x = x;
            this.y = y;
            this.z = z;
            this.xRot = xRot;
            this.yRot = yRot;
            this.zRot = zRot;
        }
    }

    static class TowerConfig {

        final CubesModel.Cube.Type type;
        final float x;
        final float y;
        final float z;
        final float xRot;
        final float yRot;
        final float zRot;
        final String[] ids;
        final float[] yValues;

        TowerConfig(float x, float y, float z, String[] ids) {
            this(CubesModel.Cube.Type.LARGE, x, y, z, ids);
        }

        TowerConfig(float x, float y, float z, float yRot, String[] ids) {
            this(x, y, z, 0, yRot, 0, ids);
        }

        TowerConfig(CubesModel.Cube.Type type, float x, float y, float z, String[] ids) {
            this(type, x, y, z, 0, 0, 0, ids);
        }

        TowerConfig(CubesModel.Cube.Type type, float x, float y, float z, float yRot, String[] ids) {
            this(type, x, y, z, 0, yRot, 0, ids);
        }

        TowerConfig(float x, float y, float z, float xRot, float yRot, float zRot, String[] ids) {
            this(CubesModel.Cube.Type.LARGE, x, y, z, xRot, yRot, zRot, ids);
        }

        TowerConfig(CubesModel.Cube.Type type, float x, float y, float z, float xRot, float yRot, float zRot, String[] ids) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.z = z;
            this.xRot = xRot;
            this.yRot = yRot;
            this.zRot = zRot;
            this.ids = ids;

            this.yValues = new float[ids.length];
            for (int i = 0; i < ids.length; i++) {
                yValues[i] = y + i * (CUBE_HEIGHT + CUBE_SPACING);
            }
        }

    }

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
            }  catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }

        // Any global transforms
        LXTransform globalTransform = new LXTransform();
        globalTransform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
        globalTransform.rotateX(globalRotationX * Math.PI / 180.);
        globalTransform.rotateY(globalRotationY * Math.PI / 180.);
        globalTransform.rotateZ(globalRotationZ * Math.PI / 180.);

        List<CubesModel.Tower> towers = new ArrayList<>();
        List<CubesModel.Cube> allCubes = new ArrayList<>();

        /* DJ BOOTH ----------------------------------------------------------*/
        globalTransform.push();
        globalTransform.translate(djBoothOffsetX, djBoothOffsetY, djBoothOffsetZ);
        globalTransform.rotateX(djBoothRotationX * Math.PI / 180.);
        globalTransform.rotateY(djBoothRotationY * Math.PI / 180.);
        globalTransform.rotateZ(djBoothRotationZ * Math.PI / 180.);

        for (TowerConfig config : DJ_BOOTH_CUBES) {
            List<CubesModel.Cube> cubes = new ArrayList<>();
            float x = config.x;
            float z = config.z;
            float xRot = config.xRot;
            float yRot = config.yRot;
            float zRot = config.zRot;
            CubesModel.Cube.Type type = config.type;

            for (int i = 0; i < config.ids.length; i++) {
                float y = config.yValues[i];
                CubesModel.Cube cube = new CubesModel.Cube(config.ids[i], x, y, z, xRot+180f, yRot-90f, zRot, globalTransform, type);
                cubes.add(cube);
                allCubes.add(cube);
            }
            towers.add(new CubesModel.Tower("", cubes));
        }
        globalTransform.pop();
        /*-----------------------------------------------------------------*/

        /* WINDOW ----------------------------------------------------------*/
        globalTransform.push();
        globalTransform.translate(windowOffsetX, windowOffsetY, windowOffsetZ);
        globalTransform.rotateX(windowRotationX * Math.PI / 180.);
        globalTransform.rotateY(windowRotationY * Math.PI / 180.);
        globalTransform.rotateZ(windowRotationZ * Math.PI / 180.);

        for (TowerConfig config : WINDOW_CUBES) {
            List<CubesModel.Cube> cubes = new ArrayList<>();
            float x = config.x;
            float z = config.z;
            float xRot = config.xRot;
            float yRot = config.yRot;
            float zRot = config.zRot;
            CubesModel.Cube.Type type = config.type;

            for (int i = 0; i < config.ids.length; i++) {
                float y = config.yValues[i];
                CubesModel.Cube cube = new CubesModel.Cube(config.ids[i], x, y, z, xRot+180f, yRot-90f, zRot, globalTransform, type);
                cubes.add(cube);
                allCubes.add(cube);
            }
            towers.add(new CubesModel.Tower("", cubes));
        }
        globalTransform.pop();
        /*-----------------------------------------------------------------*/

        /* CORNER ROCKS ----------------------------------------------------------*/
        globalTransform.push();
        globalTransform.translate(cornerRocksOffsetX, cornerRocksOffsetY, cornerRocksOffsetZ);
        globalTransform.rotateX(cornerRocksRotationX * Math.PI / 180.);
        globalTransform.rotateY(cornerRocksRotationY * Math.PI / 180.);
        globalTransform.rotateZ(cornerRocksRotationZ * Math.PI / 180.);

        for (TowerConfig config : CORNER_ROCKS_CUBES) {
            List<CubesModel.Cube> cubes = new ArrayList<>();
            float x = config.x;
            float z = config.z;
            float xRot = config.xRot;
            float yRot = config.yRot;
            float zRot = config.zRot;
            CubesModel.Cube.Type type = config.type;

            for (int i = 0; i < config.ids.length; i++) {
                float y = config.yValues[i];
                CubesModel.Cube cube = new CubesModel.Cube(config.ids[i], x, y, z, xRot+180f, yRot-90f, zRot, globalTransform, type);
                cubes.add(cube);
                allCubes.add(cube);
            }
            towers.add(new CubesModel.Tower("", cubes));
        }
        globalTransform.pop();
        /*-----------------------------------------------------------------*/

        /* ROCKS ----------------------------------------------------------*/
        globalTransform.push();
        globalTransform.translate(rocksOffsetX, rocksOffsetY, rocksOffsetZ);
        globalTransform.rotateX(rocksRotationX * Math.PI / 180.);
        globalTransform.rotateY(rocksRotationY * Math.PI / 180.);
        globalTransform.rotateZ(rocksRotationZ * Math.PI / 180.);

        for (TowerConfig config : ROCKS_CUBES) {
            List<CubesModel.Cube> cubes = new ArrayList<>();
            float x = config.x;
            float z = config.z;
            float xRot = config.xRot;
            float yRot = config.yRot;
            float zRot = config.zRot;
            CubesModel.Cube.Type type = config.type;

            for (int i = 0; i < config.ids.length; i++) {
                float y = config.yValues[i];
                CubesModel.Cube cube = new CubesModel.Cube(config.ids[i], x, y, z, xRot+180f, yRot-90f, zRot, globalTransform, type);
                cubes.add(cube);
                allCubes.add(cube);
            }
            towers.add(new CubesModel.Tower("", cubes));
        }
        globalTransform.pop();
        /*-----------------------------------------------------------------*/

        /* ROCKS ----------------------------------------------------------*/
        globalTransform.push();

        for (TowerConfig config : SINGLE_TOWERS_CUBES) {
            List<CubesModel.Cube> cubes = new ArrayList<>();
            float x = config.x;
            float z = config.z;
            float xRot = config.xRot;
            float yRot = config.yRot;
            float zRot = config.zRot;
            CubesModel.Cube.Type type = config.type;

            for (int i = 0; i < config.ids.length; i++) {
                float y = config.yValues[i];
                CubesModel.Cube cube = new CubesModel.Cube(config.ids[i], x, y, z, xRot+180f, yRot-90f, zRot, globalTransform, type);
                cubes.add(cube);
                allCubes.add(cube);
            }
            towers.add(new CubesModel.Tower("", cubes));
        }
        globalTransform.pop();
        /*-----------------------------------------------------------------*/

        CubesModel.Cube[] allCubesArr = new CubesModel.Cube[allCubes.size()];
        for (int i = 0; i < allCubesArr.length; i++) {
            allCubesArr[i] = allCubes.get(i);
        }

        return new CubesModel(towers, allCubesArr);
    }

    /*
    public static LXModel importObjModel() {
        return new LXModel(new ObjImporter("data", globalTransform).getModels().toArray(new LXModel[0]));
    }
    */

    private static Map<LX, WeakReference<CubesLayout>> instanceByLX = new WeakHashMap<>();

    public static CubesLayout getInstance(LX lx) {
        WeakReference<CubesLayout> weakRef = instanceByLX.get(lx);
        return weakRef == null ? null : weakRef.get();
    }

    public static void addFadecandyOutput(LX lx) throws Exception {
        lx.engine.addOutput(new FadecandyOutput(lx, "localhost", 7890, lx.model));
//        lx.engine.addOutput(new FadecandyOutput(lx, "192.168.0.113", 1234, lx.model));
    }
    public void setupLx(SLStudioLX lx) {
        instanceByLX.put(lx, new WeakReference<>(this));

        final NetworkMonitor networkMonitor = NetworkMonitor.getInstance(lx).start();
        final Dispatcher dispatcher = Dispatcher.getInstance(lx);


        CubesController local_debug = new CubesController(lx, "localhost", "localdebug");
        controllers.add(local_debug);
        lx.addOutput(local_debug);


        TowerConfig config = new TowerConfig(SP*3.0f, (JUMP*2)+3          , -SP*5.5f, new String[] {"188"});
        List<CubesModel.Cube> cubes = new ArrayList<>();
        float x = config.x;
        float z = config.z;
        float xRot = config.xRot;
        float yRot = config.yRot;
        float zRot = config.zRot;
        CubesModel.Cube.Type type = config.type;

        LXTransform globalTransform = new LXTransform();
        globalTransform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
        globalTransform.rotateY(globalRotationY * Math.PI / 180.);
        globalTransform.rotateX(globalRotationX * Math.PI / 180.);
        globalTransform.rotateZ(globalRotationZ * Math.PI / 180.);
        for (int i = 0; i < config.ids.length; i++) {
            float y = config.yValues[i];
            CubesModel.Cube cube = new CubesModel.Cube(config.ids[i], x, y, z, xRot, yRot, zRot, globalTransform, type);
        }

        networkMonitor.networkDevices.addListener(new ListListener<NetworkDevice>() {
            public void itemAdded(int index, NetworkDevice device) {
                String macAddr = NetworkUtils.macAddrToString(device.macAddress);
                String physid = macToPhysid.get(macAddr);
                if (physid == null) {
                    physid = macAddr;
                    System.err.println("WARNING: MAC address not in physid_to_mac.json: " + macAddr);
                }
                final CubesController controller = new CubesController(lx, device, physid);
                controllers.add(index, controller);
                dispatcher.dispatchNetwork(() -> lx.addOutput(controller));
                //controller.enabled.setValue(false);
            }

            public void itemRemoved(int index, NetworkDevice device) {
                final CubesController controller = controllers.remove(index);
                dispatcher.dispatchNetwork(() -> {
                    //lx.removeOutput(controller);
                });
            }
        });

//        lx.addOutput(new CubesController(lx, "10.200.1.255"));
        //lx.addOutput(new LIFXOutput());

        lx.engine.output.enabled.addListener(param -> {
            boolean isEnabled = ((BooleanParameter) param).isOn();
            for (CubesController controller : controllers) {
                controller.enabled.setValue(isEnabled);
            }
        });
    }

    public List<CubesController> getSortedControllers() {
        List<CubesController> sorted = new ArrayList<CubesController>(controllers);
        sorted.sort(new Comparator<CubesController>() {
            public int compare(CubesController o1, CubesController o2) {
                try {
                    return Integer.parseInt(o1.id) - Integer.parseInt(o2.id);
                } catch (NumberFormatException e) {
                    return o1.id.compareTo(o2.id);
                }
            }
        });
        return sorted;
    }

    public void addControllerListListener(ListListener<CubesController> listener) {
        controllers.addListener(listener);
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        UI2dScrollContext utility = ui.rightPane.utility;
        new UIOutputs(lx, ui, this, 0, 0, utility.getContentWidth()).addToContainer(utility);
        new UIMappingPanel(lx, ui, 0, 0, utility.getContentWidth()).addToContainer(utility);
    }
}
