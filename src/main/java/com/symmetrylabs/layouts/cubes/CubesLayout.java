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
    static final float SP = 24+9;
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
// OFFICE MAPPINGS APRIL
    
//         // left
//         // new TowerConfig(-SP*5.5f, (JUMP*0)+0, -SP*3.5f, new String[] { "localdebug" }),
                
                //backrow left to right
        new TowerConfig(0, 0,0, -45, new String[] { "326", "198", "6","50" }),
        new TowerConfig((SP) * 1, 0,0, -45, new String[] { "418", "203", "54" }),
        new TowerConfig((SP) * 2, 0,0, -45, new String[] { "150", "312", "129" }),
        new TowerConfig((SP) * 3, 0,0, -45, new String[] { "172", "79", "111", "177" }),
        
        // //Back Row Top Center
        new TowerConfig((SP) * 1.5f, JUMP * 3,0, -45, new String[] {"87"}),        




        //middle 3
        new TowerConfig (SP*.5f, 0, -24*2, -45, new String[] {"340", "135", "391", "390"}),
        new TowerConfig (SP * 1.5f, 0, -24*2, -45, new String[] {"182", "398", "94" }),
        new TowerConfig (SP * 2.5f, 0, -24*2, -45, new String[] {"29", "30", "199", "27"}),
        new TowerConfig (CubesModel.Cube.Type.MEDIUM, SP * 2.5f + 6, 0, -24*2, -45, new String[] { "143"}),
        new TowerConfig (CubesModel.Cube.Type.MEDIUM, SP * .5f + 6, 0, -24*2, -45, new String[] { "393"}),
        //medium 393
        //medium (actual) 345

        //middle 2
        new TowerConfig (SP * 1, 0, -24*4, -45, new String[] { "383", "211", "d8:80:39:9b:23:ad"}),
        new TowerConfig (SP * 2, 0, -24*4, -45, new String[] { "196", "18", "361"}),
        new TowerConfig (CubesModel.Cube.Type.MEDIUM, SP * 1+6, 0, -24*4, -45, new String[] { "210"}),
        new TowerConfig (CubesModel.Cube.Type.MEDIUM, SP * 2+6, 0, -24*4, -45, new String[] { "345"}),
        new TowerConfig (CubesModel.Cube.Type.SMALL, SP * 1+12, 0, -24*4, -45, new String[] { "82"}),
        new TowerConfig (CubesModel.Cube.Type.SMALL, SP * 2+12, 0, -24*4, -45, new String[] { ""}),
        // front 1
        new TowerConfig (43.5f, 0, -24*6, -45, new String[] { "74", "63", "33"}),
        new TowerConfig (CubesModel.Cube.Type.MEDIUM, SP * 1.5f, 0, -24*6, -45, new String[] { "334"}),
        new TowerConfig (CubesModel.Cube.Type.SMALL, SP * 1.75f, 0, -24*6, -45, new String[] { "384"}),

        


// STOCK CONFIG
    /*    // left

        new TowerConfig(-SP*5.5f, (JUMP*0)+0, -SP*3.5f, new String[] { "localdebug" }),

        new TowerConfig(-SP*3.5f, 0, -SP*2.5f, new String[] { "22", "204" }),
        new TowerConfig(-SP*3.5f, 0, -SP*2.5f, new String[] { "static0", "static1", "static2", "static3" }),

        new TowerConfig(-SP*4.5f, (JUMP*0)+TOWER_RISER, -SP*3.0f, new String[] { "86" }),

        new TowerConfig(-SP*2.0f, 0, -SP*1.0f, new String[] { "25", "199", "177" }),

        new TowerConfig(-SP*1.5f, (JUMP*0)+TOWER_RISER, -SP*2.0f, new String[] { "94" }),
        new TowerConfig(-SP*1.0f, (JUMP*0)+TOWER_RISER, -SP*0.5f, new String[] { "90" }),
        new TowerConfig(-SP*1.0f, (JUMP*2)+TOWER_RISER, -SP*0.5f, new String[] { "64" }),

        // left tower of 5
        new TowerConfig(0, 0, 0, new String[] {
            "19", "190", "121", "1", "103"
        }),

        new TowerConfig(SP*1.0f, (JUMP*0)+TOWER_RISER, -SP*0.5f, new String[] {"76"}),
        new TowerConfig(SP*1.0f, (JUMP*2)+TOWER_RISER, -SP*0.5f, new String[] {"18"}),
        new TowerConfig(SP*1.0f, (JUMP*1)+TOWER_RISER, +SP*0.5f, new String[] {"157"}),
        new TowerConfig(SP*0.5f, (JUMP*3)+TOWER_RISER, -SP*1.0f, new String[] {"4"}),
        new TowerConfig(SP*1.5f, (JUMP*2)+0          , -SP*1.5f, new String[] {"126"}),

        new TowerConfig(SP*2.0f, 0, 0, new String[] {
            "6", "132", "61", "54"
        }),

        new TowerConfig(SP*2.5f, (JUMP*1)+TOWER_RISER, -SP*1.0f, new String[] {"4"}),
        new TowerConfig(SP*2.5f, (JUMP*3)+TOWER_RISER, -SP*1.0f, new String[] {"151"}),

        // middle tower of 5
        new TowerConfig(SP*3.5f, 0, -SP*1.5f, new String[] {
            "111", "166", "187", "158", "101"
        }),

        new TowerConfig(SP*4.5f, (JUMP*3)+TOWER_RISER, -SP*2.0f, new String[] {"11"}),
        new TowerConfig(SP*3.0f, (JUMP*2)+TOWER_RISER, -SP*2.5f, new String[] {"163"}),
        new TowerConfig(SP*2.0f, (JUMP*3)+0          , -SP*2.0f, new String[] {"34"}),
        new TowerConfig(SP*4.0f, (JUMP*0)+TOWER_RISER, -SP*2.5f, new String[] {"17", "44"}),

        new TowerConfig(SP*4.5f, 0, -SP*3.5f, new String[] {
            "102", "156", "13", "82"
        }),

        new TowerConfig(SP*5.5f, (JUMP*2)+TOWER_RISER, -SP*3.5f, new String[] {"412"}),
        new TowerConfig(SP*5.0f, (JUMP*0)+TOWER_RISER, -SP*4.0f, new String[] {"73"}),
        new TowerConfig(SP*4.0f, (JUMP*1)+TOWER_RISER, -SP*4.0f, new String[] {"47"}),
        new TowerConfig(SP*4.0f, (JUMP*3)+TOWER_RISER, -SP*4.0f, new String[] {"32"}),
        new TowerConfig(SP*3.0f, (JUMP*3)+0          , -SP*3.5f, new String[] {"175"}),

        // right tower of 5
        new TowerConfig(SP*4.5f, 0, -SP*5.0f, new String[] {
            "183", "180", "57", "51", "108"
        }),

        new TowerConfig(SP*3.5f, (JUMP*0)+TOWER_RISER, -SP*5.5f, new String[] {"104"}),
        new TowerConfig(SP*4.0f, (JUMP*2)+TOWER_RISER, -SP*6.0f, new String[] {"168"}),
        new TowerConfig(SP*3.0f, (JUMP*2)+3          , -SP*5.5f, new String[] {"188"}),

        new TowerConfig(SP*3.0f - 10, 0, -SP*6.5f - 12, new String[] {
            "100", "85", "110zAQ  AZQ"
        }),

        new TowerConfig((SP*3.0f - 10)-(SP*0.5f), (JUMP*0)+TOWER_RISER, (-SP*6.5f - 12)-(SP*1.0f), new String[] {"87"}),
        new TowerConfig((SP*3.0f - 10)-(SP*0.0f), (JUMP*0)+0          , (-SP*6.5f - 12)-(SP*2.0f), new String[] {"33"}),

        // table cubes
        new TowerConfig(SP*-0.5f, 0, -SP*4.0f, new String[] {"74"}),
        new TowerConfig(0, 0, -SP*5.0f, new String[] {"171"}),
        new TowerConfig(SP*1.0f, 0, -SP*5.5f, new String[] {"9"}),

        */
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
