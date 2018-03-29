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

import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.LX;
import heronarts.lx.output.FadecandyOutput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.layouts.icicles.IciclesModel;
import com.symmetrylabs.layouts.icicles.Icicle;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.util.NetworkUtils;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.listenable.ListenableList;
import com.symmetrylabs.util.listenable.ListListener;
import com.symmetrylabs.slstudio.output.pixelpusher.*;
import heronarts.p3lx.ui.UI2dScrollContext;

import com.symmetrylabs.slstudio.output.pixelpusher.SLPixelPusherManager;
import com.symmetrylabs.slstudio.output.PointsGrouping;

/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class KearnyStreetLayout implements Layout {

    static final float INCHES_PER_METER = 39.3701f;

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;

    static IcicleConfig[] ICICLE_CONFIG = {
        new IcicleConfig("0", 0, 0, 0, 0, 0, 0, new Icicle.Metrics(70, 0.5f)),
    };

    static class IcicleConfig {
        final String id;
        final float x;
        final float y;
        final float z;
        final float xRot;
        final float yRot;
        final float zRot;
        final Icicle.Metrics metrics;

        IcicleConfig(String id, float x, float y, float z, float xRot, float yRot, float zRot, Icicle.Metrics metrics) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.z = z;
            this.xRot = xRot;
            this.yRot = yRot;
            this.zRot = zRot;
            this.metrics = metrics;
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
        List<Icicle> icicles = new ArrayList<>();

        for (IcicleConfig config : ICICLE_CONFIG) {
            String id = config.id;
            float x = config.x;
            float y = config.y;
            float z = config.z;
            float xRot = config.xRot;
            float yRot = config.yRot;
            float zRot = config.zRot;
            Icicle.Metrics metrics = config.metrics;

            Icicle icicle = new Icicle(id, x, y, z, xRot, yRot, zRot, globalTransform, metrics);
            icicles.add(icicle);
        }
        /*-----------------------------------------------------------------*/

        return new IciclesModel(icicles);
    }

    private static Map<LX, WeakReference<KearnyStreetLayout>> instanceByLX = new WeakHashMap<>();

    public static KearnyStreetLayout getInstance(LX lx) {
        WeakReference<KearnyStreetLayout> weakRef = instanceByLX.get(lx);
        return weakRef == null ? null : weakRef.get();
    }

    public void setupLx(SLStudioLX lx) {
        SLPixelPusherManager manager = new SLPixelPusherManager(lx);
        manager.addDataline(new PointsGrouping("1")
            .addPoints(((IciclesModel)lx.model).getIcicleByRawIndex(0).points)
        );
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
    }


}
