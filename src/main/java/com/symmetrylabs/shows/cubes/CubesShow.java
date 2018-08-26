package com.symmetrylabs.shows.cubes;

import java.util.*;
import java.lang.ref.WeakReference;
import java.lang.RuntimeException;

import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.p3lx.ui.UI2dScrollContext;

import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.listenable.ListenableSet;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.util.CubePhysicalIdMap;
import com.symmetrylabs.util.listenable.SetListener;
import static com.symmetrylabs.util.MathUtils.*;


/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public abstract class CubesShow implements Show {
    public static final float CUBE_WIDTH = 24;
    public static final float CUBE_HEIGHT = 24;
    public static final float CUBE_SPACING = 1.5f;

    public static final float TOWER_VERTICAL_SPACING = 2.5f;
    public static final float TOWER_RISER = 14;
    public static final float SP = CUBE_HEIGHT+CUBE_SPACING;
    public static final float JUMP = CUBE_HEIGHT+TOWER_VERTICAL_SPACING;

    static final float INCHES_PER_METER = 39.3701f;

    ListenableSet<CubesController> controllers = new ListenableSet<>();
    CubePhysicalIdMap cubePhysicalIdMap = new CubePhysicalIdMap();

    private static Map<LX, WeakReference<CubesShow>> instanceByLX = new WeakHashMap<>();

    public static CubesShow getInstance(LX lx) {
        WeakReference<CubesShow> weakRef = instanceByLX.get(lx);
        return weakRef == null ? null : weakRef.get();
    }

    public static class TowerConfig {

        final public CubesModel.Cube.Type type;
        final public float x;
        final public float y;
        final public float z;
        final public float xRot;
        final public float yRot;
        final public float zRot;
        final public String[] ids;
        final public float[] yValues;

        public TowerConfig(float x, float y, float z, String[] ids) {
            this(CubesModel.Cube.Type.LARGE, x, y, z, ids);
        }

        public TowerConfig(float x, float y, float z, float yRot, String[] ids) {
            this(x, y, z, 0, yRot, 0, ids);
        }

        public TowerConfig(CubesModel.Cube.Type type, float x, float y, float z, String[] ids) {
            this(type, x, y, z, 0, 0, 0, ids);
        }

        public TowerConfig(CubesModel.Cube.Type type, float x, float y, float z, float yRot, String[] ids) {
            this(type, x, y, z, 0.f, yRot, 0.f, ids);
        }

        public TowerConfig(float x, float y, float z, float xRot, float yRot, float zRot, String[] ids) {
            this(CubesModel.Cube.Type.LARGE, x, y, z, xRot, yRot, zRot, ids);
        }

        public TowerConfig(CubesModel.Cube.Type type, float x, float y, float z, float xRot, float yRot, float zRot, String[] ids) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.z = z;
            this.xRot = xRot;
            this.yRot = yRot;
            this.zRot = zRot;
            this.ids = ids;

            if (type != CubesModel.Cube.Type.HD) {
                yValues = new float[ids.length];
            } else if (type == CubesModel.Cube.Type.HD){
                if (ids.length % 2 != 0) {
                    throw new RuntimeException("DoubleControllerCube with id (" + ids[0] + ") requires 2 ids per cubes!");
                }
                yValues = new float[floor(ids.length/2)];
            } else {
                yValues = null;
            }

            for (int i = 0; i < yValues.length; i++) {
                yValues[i] = y + i * (CUBE_HEIGHT + CUBE_SPACING);
            }
        }
    }

    public abstract SLModel buildModel();

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
                    controller.dispose();
                    lx.removeOutput(controller);
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
