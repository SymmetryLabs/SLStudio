package com.symmetrylabs.layouts.cubes;

import java.util.*;
import java.lang.ref.WeakReference;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.output.SLController;
import com.symmetrylabs.util.CubePhysicalIdMap;
import com.symmetrylabs.util.listenable.SetListener;
import heronarts.lx.LX;
import heronarts.lx.output.FadecandyOutput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.listenable.ListenableSet;
import heronarts.p3lx.ui.UI2dScrollContext;

/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class CubesLayout implements Layout {
    ListenableSet<CubesController> controllers = new ListenableSet<>();
    CubePhysicalIdMap cubePhysicalIdMap = new CubePhysicalIdMap();

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = -45;
    static final float globalRotationZ = 0;

    static final float CUBE_WIDTH = 24;
    static final float CUBE_HEIGHT = 24;
    static final float TOWER_WIDTH = 24;
    static final float TOWER_HEIGHT = 24;
    static final float CUBE_SPACING = 2.5f;

    static final float TOWER_VERTICAL_SPACING = 2.5f;
    static final float TOWER_RISER = 14;
    static final float SP = 24+2;
    static final float JUMP = TOWER_HEIGHT+TOWER_VERTICAL_SPACING;

    static final float INCHES_PER_METER = 39.3701f;

    static final TowerConfig[] TOWER_CONFIG = {

        // left
        new TowerConfig(-SP*3.5f, 0, -SP*2.5f, new String[] { "22", "204" }),

        new TowerConfig(-SP*4.5f, (JUMP*0)+TOWER_RISER, -SP*3.0f, new String[] { "86" }),
        new TowerConfig(-SP*5.5f, (JUMP*0)+0, -SP*3.5f, new String[] { "5" }),

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
    };

    static final TowerConfig[] TOWER_CONFIG_SMALL = {
//        new TowerConfig(SP*0.f, 0, -SP*0.f, new String[] {"0", "0", "0", "0", "0"}),
//        new TowerConfig(SP*1.f, 0, -SP*1.f, new String[] {"0", "0", "0", "0", "0"}),
//        new TowerConfig(SP*2.f, 0, -SP*2.f, new String[] {"0", "0", "0", "0", "0"}),
//        new TowerConfig(SP*3.f, 0, -SP*3.f, new String[] {"0", "0", "0", "0", "0"}),
//        new TowerConfig(SP*4.f, 0, -SP*4.f, new String[] {"0", "0", "0", "0", "0"}),
//        new TowerConfig(SP*5.f, 0, -SP*5.f, new String[] {"0", "0", "0", "0", "0"}),

// back row right to left

new TowerConfig(SP*0, 0, -SP*0, 0, 0, 0, new String[] {"55", "001ec0f4beb1", "353", "61"}),
    new TowerConfig(SP*0, 0, -SP*1, 0, 0, 0, new String[] {"308", "412", "38"}),
new TowerConfig(SP*1, 0, -SP*1, 0, 0, 0, new String[] {"d880399b2b0a", "001ec0f56df8", "001ec0f4f636"}),
new TowerConfig(SP*2, 0, -SP*2, 0, 0, 0, new String[] {"d880399ad507", "71", "d8803963052f"}),
    new TowerConfig(SP*2, 0, -SP*3, 0, 0, 0, new String[] {"46", "5", "001ec0f56ee3"}),
new TowerConfig(SP*3, 0, -SP*3, 0, 0, 0, new String[] {"d880399b2000", "398", "001ec0f543f4", "d880396305af"})
};

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

    public SLModel buildModel() {
        // Any global transforms
        LXTransform globalTransform = new LXTransform();
        globalTransform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
        globalTransform.rotateX(globalRotationX * Math.PI / 180.);
        globalTransform.rotateY(globalRotationY * Math.PI / 180.);
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

        /*
        CubesController local_debug = new CubesController(lx, "localhost", "localdebug");
        controllers.add(local_debug);
        lx.addOutput(local_debug);
        */

        networkMonitor.deviceList.addListener(new SetListener<NetworkDevice>() {
            public void onItemAdded(NetworkDevice device) {
                String physicalId = cubePhysicalIdMap.getPhysicalId(device.deviceId);
                final CubesController controller = new CubesController(lx, device, physicalId);
                controller.set16BitColorEnabled(device.featureIds.contains("rgb16"));
                controllers.add(controller);
                dispatcher.dispatchNetwork(() -> lx.addOutput(controller));
                //controller.enabled.setValue(false);
            }

            public void onItemRemoved(NetworkDevice device) {
                final CubesController controller = getControllerByDevice(device);
                controllers.remove(controller);
                dispatcher.dispatchNetwork(() -> {
                    //lx.removeOutput(controller);
                });
            }
        });

        //lx.addOutput(new CubesController(lx, "10.200.1.255"));

        lx.engine.output.enabled.addListener(param -> {
            boolean isEnabled = ((BooleanParameter) param).isOn();
            for (CubesController controller : controllers) {
                controller.enabled.setValue(isEnabled);
            }
        });
    }

    public CubesController getControllerByDevice(NetworkDevice device) {
        for (CubesController controller : controllers) {
            if (controller.networkDevice == device) {
                return controller;
            }
        }
        return null;
    }

    public Collection<CubesController> getSortedControllers() {
        return new TreeSet<CubesController>(controllers);
    }

    public void addControllerSetListener(SetListener<CubesController> listener) {
        controllers.addListener(listener);
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        UI2dScrollContext utility = ui.rightPane.utility;
        new UIOutputs(lx, ui, this, 0, 0, utility.getContentWidth()).addToContainer(utility);
        new UIMappingPanel(lx, ui, 0, 0, utility.getContentWidth()).addToContainer(utility);
    }
}
