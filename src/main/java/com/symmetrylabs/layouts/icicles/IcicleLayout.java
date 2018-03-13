package com.symmetrylabs.layouts.icicles;

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

import heronarts.lx.LX;
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

import com.symmetrylabs.layouts.cubes.CubesController;

import com.symmetrylabs.layouts.icicles.UIOutputs;
import com.symmetrylabs.layouts.cubes.UIMappingPanel;

/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class IcicleLayout implements Layout {
    ListenableList<CubesController> controllers = new ListenableList<>();

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;

    static final float INCHES_PER_METER = 39.3701f;

    static final float SPACING = 10f;

    static final IcicleConfig[] ICICLES_CONFIG = {

        // left
        new IcicleConfig("0", SPACING*0, 0, 0, 0, 0, 90),
        new IcicleConfig("0", SPACING*1, 0, 0, 0, 0, 90),
        new IcicleConfig("0", SPACING*2, 0, 0, 0, 0, 90),
        new IcicleConfig("0", SPACING*3, 0, 0, 0, 0, 90),
        new IcicleConfig("0", SPACING*4, 0, 0, 0, 0, 90),
        new IcicleConfig("0", SPACING*5, 0, 0, 0, 0, 90),

    };

    static class IcicleConfig {

        final String id;
        final float x;
        final float y;
        final float z;
        final float xRot;
        final float yRot;
        final float zRot;

        IcicleConfig(String id, float x, float y, float z, float xRot, float yRot, float zRot) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.z = z;
            this.xRot = xRot;
            this.yRot = yRot;
            this.zRot = zRot;
        }

    }

    static Map<String, String> macToPhysid = new HashMap<>();
    static Map<String, String> physidToMac = new HashMap<>();

    public IcicleModel buildModel() {

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
        List<IcicleModel.Icicle> icicles = new ArrayList<>();

        for (IcicleConfig config : ICICLES_CONFIG) {
            String id = config.id;
            float x = config.x;
            float y = config.y;
            float z = config.z;
            float xRot = config.xRot;
            float yRot = config.yRot;
            float zRot = config.zRot;

            icicles.add(new IcicleModel.Icicle(id, x, y, z, xRot, yRot, zRot, globalTransform));
        }
        /*-----------------------------------------------------------------*/

        IcicleModel.Icicle[] iciclesArr = new IcicleModel.Icicle[icicles.size()];
        for (int i = 0; i < iciclesArr.length; i++) {
            iciclesArr[i] = icicles.get(i);
        }

        return new IcicleModel(icicles, iciclesArr);
    }

    /*
    public static LXModel importObjModel() {
        return new LXModel(new ObjImporter("data", globalTransform).getModels().toArray(new LXModel[0]));
    }
    */

    private static Map<LX, WeakReference<IcicleLayout>> instanceByLX = new WeakHashMap<>();

    public static IcicleLayout getInstance(LX lx) {
        WeakReference<IcicleLayout> weakRef = instanceByLX.get(lx);
        return weakRef == null ? null : weakRef.get();
    }

    public void setupLx(SLStudioLX lx) {
        instanceByLX.put(lx, new WeakReference<>(this));

        final NetworkMonitor networkMonitor = NetworkMonitor.getInstance(lx).start();
        final Dispatcher dispatcher = Dispatcher.getInstance(lx);

        networkMonitor.networkDevices.addListener(new ListListener<NetworkDevice>() {
            public void itemAdded(int index, NetworkDevice device) {
                System.out.println("controller added");
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

        //lx.addOutput(new CubesController(lx, "10.200.1.255"));
        //lx.addOutput(new LIFXOutput());

        System.out.println("network listener");


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
