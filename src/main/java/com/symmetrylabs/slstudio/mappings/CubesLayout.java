package com.symmetrylabs.slstudio.mappings;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.CubesModel;
import com.symmetrylabs.slstudio.output.SLController;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.util.NetworkUtils;
import com.symmetrylabs.slstudio.util.dispatch.Dispatcher;
import com.symmetrylabs.slstudio.util.listenable.ListenableList;
import com.symmetrylabs.slstudio.util.listenable.ListListener;
import com.symmetrylabs.slstudio.objimporter.ObjImporter;

/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class CubesLayout {

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
    static final float CUBE_SPACING = 3.5f;

    static final float TOWER_VERTICAL_SPACING = 2.5f;
    static final float TOWER_RISER = 14;
    static final float SP = 24;
    static final float JUMP = TOWER_HEIGHT+TOWER_VERTICAL_SPACING;

    static final float INCHES_PER_METER = 39.3701f;

    // static final BulbConfig[] BULB_CONFIG = {
    //     // new BulbConfig("lifx-1", -50, 50, -30),
    //     // new BulbConfig("lifx-2", 0, 50, 0),
    //     // new BulbConfig("lifx-3", -65, 20, -100),
    //     // new BulbConfig("lifx-4", 0, 0, 0),
    //     // new BulbConfig("lifx-5", 0, 0, 0),
    // };

    static final TowerConfig[] TOWER_CONFIG = {

        // left
        new TowerConfig(0, 0, 0, new String[] {"0", "0", "0", "0", "0"}),
        new TowerConfig(30, 0, -30, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(60, 0, 0, new String[] {"0", "0", "0"}),

        new TowerConfig(0, 137, 15, new String[] {"0", "0", "0"}),
        new TowerConfig(30, 109, 15, new String[] {"0", "0", "0", "0", "0", "0", "0"}),
        new TowerConfig(60, 109, 15, new String[] {"0", "0", "0", "0", "0", "0", "0"}),

        new TowerConfig(12, 150, -15, new String[] {"0"}),
        new TowerConfig(39, 150, -15, new String[] {"0"}),

        // right
        new TowerConfig(200, 0, 0, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(230, 0, 0, new String[] {"0", "0", "0", "0", "0"}),
        new TowerConfig(260, 29, 0, new String[] {"0", "0", "0", "0"}),

        new TowerConfig(200, 143, 0, new String[] {"0", "0"}),
        new TowerConfig(200, 228, 0, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(230, 143, 0, new String[] {"0", "0", "0", "0", "0", "0"}),
        new TowerConfig(260, 143, 0, new String[] {"0", "0", "0", "0"}),
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

    public static CubesModel buildModel() {

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
        globalTransform.rotateY(globalRotationY * Math.PI / 180.);
        globalTransform.rotateX(globalRotationX * Math.PI / 180.);
        globalTransform.rotateZ(globalRotationZ * Math.PI / 180.);

        /* Cubes ----------------------------------------------------------*/
        List<CubesModel.Tower> towers = new ArrayList<>();
        List<CubesModel.Cube> allCubes = new ArrayList<>();

        int stripId = 0;
        for (TowerConfig config : TOWER_CONFIG) {
            List<CubesModel.Cube> cubes = new ArrayList<>();
            float x = config.x;
            float z = config.z;
            float xRot = config.xRot;
            float yRot = config.yRot;
            float zRot = config.zRot;
            CubesModel.Cube.Type type = config.type;

            for (int i = 0; i < config.ids.length; i++) {
                float y = config.yValues[i];
                CubesModel.Cube cube = new CubesModel.Cube(config.ids[i], x, y, z, xRot, yRot, zRot, globalTransform, type);
                cubes.add(cube);
                allCubes.add(cube);
            }
            towers.add(new CubesModel.Tower("", cubes));
        }
        /*-----------------------------------------------------------------*/

        /* Strips ----------------------------------------------------------*/
        List<CubesModel.CubesStrip> strips = new ArrayList<>();

        for (StripConfig stripConfig : STRIP_CONFIG) {
            CubesModel.CubesStrip.Metrics metrics = new CubesModel.CubesStrip.Metrics(stripConfig.numPoints, stripConfig.spacing);

            globalTransform.push();
            globalTransform.translate(stripConfig.x, stripConfig.y, stripConfig.z);
            globalTransform.rotateY(stripConfig.xRot * Math.PI / 180.);
            globalTransform.rotateX(stripConfig.yRot * Math.PI / 180.);
            globalTransform.rotateZ(stripConfig.zRot * Math.PI / 180.);

            strips.add(new CubesModel.CubesStrip(stripId+"", metrics, globalTransform));

            globalTransform.pop();

            ++stripId;
        }
        /*-----------------------------------------------------------------*/

        CubesModel.Cube[] allCubesArr = new CubesModel.Cube[allCubes.size()];
        for (int i = 0; i < allCubesArr.length; i++) {
            allCubesArr[i] = allCubes.get(i);
        }

        return new CubesModel(towers, allCubesArr, strips);
    }

    /*
    public static LXModel importObjModel() {
        return new LXModel(new ObjImporter("data", globalTransform).getModels().toArray(new LXModel[0]));
    }
    */

    public static ListenableList<SLController> setupCubesOutputs(LX lx) {

        ListenableList<SLController> controllers = new ListenableList<>();

        if (!(lx.model instanceof CubesModel))
            return controllers;

        final NetworkMonitor networkMonitor = NetworkMonitor.getInstance(lx);
        final Dispatcher dispatcher = Dispatcher.getInstance(lx);

        networkMonitor.networkDevices.addListener(new ListListener<NetworkDevice>() {
            public void itemAdded(int index, NetworkDevice device) {
                String macAddr = NetworkUtils.macAddrToString(device.macAddress);
                String physid = macToPhysid.get(macAddr);
                if (physid == null) {
                    physid = macAddr;
                    System.err.println("WARNING: MAC address not in physid_to_mac.json: " + macAddr);
                }
                final SLController controller = new SLController(lx, device, physid);
                controllers.add(index, controller);
                dispatcher.dispatchEngine(new Runnable() {
                    public void run() {
                        lx.addOutput(controller);
                    }
                });
                //controller.enabled.setValue(false);
            }
            public void itemRemoved(int index, NetworkDevice device) {
                final SLController controller = controllers.remove(index);
                dispatcher.dispatchEngine(new Runnable() {
                    public void run() {
                        //lx.removeOutput(controller);
                    }
                });
            }
        });

        //lx.addOutput(new SLController(lx, "10.200.1.255"));
        //lx.addOutput(new LIFXOutput());

        return controllers;
    }
}
