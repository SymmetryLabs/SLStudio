package com.symmetrylabs.layouts.butterflies;

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


/**
 * This file implements the mapping functions needed to lay out the butterflies.
 */
public class ButterflyLayout implements Layout {
    //ListenableList<CubesController> controllers = new ListenableList<>();

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;

    static final float INCHES_PER_METER = 39.3701f;

    static final ButterflyConfig[] BUTTERFLY_CONFIG = {

        new ButterflyConfig("butterfly_1", new float[] {0, 0, 0}, new float[] {0, 0, 0}),

    };

    static class ButterflyConfig {
        final String id;
        final float x;
        final float y;
        final float z;
        final float xRot;
        final float yRot;
        final float zRot;

        ButterflyConfig(String id, float[] coordinates, float[] rotations) {
            this.id = id;
            this.x = coordinates[0];
            this.y = coordinates[1];
            this.z = coordinates[2];
            this.xRot = rotations[0];
            this.yRot = rotations[1];
            this.zRot = rotations[2];
        }
    }

    static Map<String, String> macToPhysid = new HashMap<>();
    static Map<String, String> physidToMac = new HashMap<>();

    public ButterflyModel buildModel() {

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

        /* Butterflies ----------------------------------------------------*/
        List<ButterflyModel.Butterfly> butterflies = new ArrayList<>();

        for (ButterflyConfig config : BUTTERFLY_CONFIG) {
            String id = config.id;
            float x = config.x;
            float y = config.y;
            float z = config.z;
            float xRot = config.xRot;
            float yRot = config.yRot;
            float zRot = config.zRot;

            ButterflyModel.Butterfly butterfly = new ButterflyModel.Butterfly(id, x, y, z, xRot, yRot, zRot, globalTransform);
            butterflies.add(butterfly);
        }
        /*-----------------------------------------------------------------*/

        ButterflyModel.Butterfly[] butterfliesArr = new ButterflyModel.Butterfly[butterflies.size()];
        for (int i = 0; i < butterfliesArr.length; i++) {
            butterfliesArr[i] = butterflies.get(i);
        }

        return new ButterflyModel(butterflies, butterfliesArr);
    }

    private static Map<LX, WeakReference<ButterflyLayout>> instanceByLX = new WeakHashMap<>();

    public static ButterflyLayout getInstance(LX lx) {
        WeakReference<ButterflyLayout> weakRef = instanceByLX.get(lx);
        return weakRef == null ? null : weakRef.get();
    }

    public void setupLx(SLStudioLX lx) {
        instanceByLX.put(lx, new WeakReference<>(this));
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
    }
}
