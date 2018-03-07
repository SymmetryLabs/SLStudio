package com.symmetrylabs.slstudio.mappings;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import com.symmetrylabs.slstudio.model.CandyBar;
import com.symmetrylabs.slstudio.model.LocatedForm;
import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.LX;
import heronarts.lx.model.LXFixture;
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
    static final float globalRotationY = -45;
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

    static final float group0_x = 20;
    static final float group0_y = 0;
    static final float group0_z = 0;

    static final TowerConfig[] TOWER_CONFIG = {

        // left
        new TowerConfig(group0_x-SP*6.5f, 0, -SP*2.5f, new String[] { "1", "29" }),

        new TowerConfig(group0_x-SP*6.5f, (JUMP*0)+TOWER_RISER, -SP*3.0f, new String[] { "38" }),
        new TowerConfig(group0_x-SP*8.5f, (JUMP*0)+0, -SP*3.5f, new String[] { "50", "56" }),

        new TowerConfig(group0_x-SP*8.0f, 0, -SP*1.0f, new String[] { "82", "120", "135" }),

        new TowerConfig(group0_x-SP*8.5f, (JUMP*0)+TOWER_RISER, -SP*2.0f, new String[] { "150" }),
        new TowerConfig(group0_x-SP*9.0f, (JUMP*0)+TOWER_RISER, -SP*0.5f, new String[] { "171" }),
        new TowerConfig(group0_x-SP*11.0f, (JUMP*2)+TOWER_RISER, -SP*0.5f, new String[] { "182" }),

        // left tower of 5
        new TowerConfig(group0_x-SP*5, 0, 0, new String[] {
            "196", "198", "203", "340", "351"
        }),

        new TowerConfig(group0_x-SP*11.0f, (JUMP*0)+TOWER_RISER, -SP*10.5f, new String[] {"360"}),
        new TowerConfig(group0_x-SP*11.0f, (JUMP*2)+TOWER_RISER, -SP*10.5f, new String[] {"361"}),
        new TowerConfig(group0_x-SP*11.0f, (JUMP*1)+TOWER_RISER, -SP*10.5f, new String[] {"397"}),
        new TowerConfig(group0_x-SP*10.5f, (JUMP*3)+TOWER_RISER, -SP*11.0f, new String[] {"4"}),
        new TowerConfig(group0_x-SP*11.5f, (JUMP*2)+0          , -SP*11.5f, new String[] {"126"}),

        new TowerConfig(SP*12.0f, 0, 0, new String[] {
            "6", "132", "61", "54"
        }),

        new TowerConfig(SP*12.5f, (JUMP*1)+TOWER_RISER, -SP*11.0f, new String[] {"4"}),
        new TowerConfig(SP*12.5f, (JUMP*3)+TOWER_RISER, -SP*11.0f, new String[] {"151"}),

        // middle tower of 5
        new TowerConfig(SP*13.5f, 0, -SP*1.5f, new String[] {
            "111", "166", "187", "158", "101"
        }),

        new TowerConfig(SP*14.5f, (JUMP*3)+TOWER_RISER, -SP*2.0f, new String[] {"11"}),
        new TowerConfig(SP*13.0f, (JUMP*2)+TOWER_RISER, -SP*2.5f, new String[] {"163"}),
        new TowerConfig(SP*12.0f, (JUMP*3)+0          , -SP*2.0f, new String[] {"34"}),
        new TowerConfig(SP*14.0f, (JUMP*0)+TOWER_RISER, -SP*2.5f, new String[] {"17", "44"}),

        new TowerConfig(SP*14.5f, 0, -SP*3.5f, new String[] {
            "102", "156", "13", "82"
        }),

        new TowerConfig(-SP*15.5f, (JUMP*2)+TOWER_RISER, -SP*3.5f, new String[] {"412"}),
        new TowerConfig(-SP*15.0f, (JUMP*0)+TOWER_RISER, -SP*4.0f, new String[] {"73"}),
        new TowerConfig(-SP*14.0f, (JUMP*1)+TOWER_RISER, -SP*4.0f, new String[] {"47"}),
        new TowerConfig(-SP*14.0f, (JUMP*3)+TOWER_RISER, -SP*4.0f, new String[] {"32"}),
        new TowerConfig(-SP*13.0f, (JUMP*3)+0          , -SP*3.5f, new String[] {"175"}),

        // right tower of 5
        new TowerConfig(SP*14.5f, 0, -SP*5.0f, new String[] {
            "183", "180", "57", "51", "108"
        }),

        new TowerConfig(SP*13.5f, (JUMP*0)+TOWER_RISER, -SP*5.5f, new String[] {"104"}),
        new TowerConfig(SP*14.0f, (JUMP*2)+TOWER_RISER, -SP*6.0f, new String[] {"168"}),
        new TowerConfig(SP*13.0f, (JUMP*2)+3          , -SP*5.5f, new String[] {"188"}),

        new TowerConfig(SP*13.0f - 10, 0, -SP*6.5f - 12, new String[] {
            "100", "85", "110zAQ  AZQ"
        }),

        new TowerConfig((SP*3.0f - 10)-(SP*0.5f), (JUMP*0)+TOWER_RISER, (-SP*6.5f - 12)-(SP*1.0f), new String[] {"87"}),
        new TowerConfig((SP*3.0f - 10)-(SP*0.0f), (JUMP*0)+0          , (-SP*6.5f - 12)-(SP*2.0f), new String[] {"33"}),

        // table cubes
        new TowerConfig(SP*-0.5f, 0, -SP*4.0f, new String[] {"74"}),
        new TowerConfig(0, 0, -SP*5.0f, new String[] {"171"}),
        new TowerConfig(SP*1.0f, 0, -SP*5.5f, new String[] {"9"}),
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

        CubesModel.Cube[] allCubesArr = new CubesModel.Cube[allCubes.size()];
        for (int i = 0; i < allCubesArr.length; i++) {
            allCubesArr[i] = allCubes.get(i);
        }

        List<LXFixture> dynamicAllBars = new ArrayList<>();
        CandyBar candy = new CandyBar();
        dynamicAllBars.add(candy);


        for (int i = 0; i < 32; i ++){
            LocatedForm located = new LocatedForm(globalTransform, candy);
            globalTransform.translate(20,0,0);
            dynamicAllBars.add(located);
        }

        LXFixture[] allFixtures =  new LXFixture[dynamicAllBars.size()];
        allFixtures = dynamicAllBars.toArray(allFixtures);



        return new CubesModel(towers, allCubesArr, allFixtures);
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
