package com.symmetrylabs.layouts.composite;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.lang.ref.WeakReference;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.FadecandyOutput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transform.LXTransform;
import heronarts.p3lx.ui.UI2dScrollContext;
import heronarts.lx.output.LXDatagramOutput;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.slstudio.output.SLController;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.layouts.cubes.CubesModel;
import com.symmetrylabs.layouts.oslo.TreeModel;
import com.symmetrylabs.layouts.icicles.Icicle;
import com.symmetrylabs.layouts.butterflies.ButterfliesModel;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.util.NetworkUtils;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.listenable.ListenableList;
import com.symmetrylabs.util.listenable.ListListener;
import com.symmetrylabs.slstudio.output.TenereDatagram;
import com.symmetrylabs.slstudio.objimporter.ObjImporter;

public class ObjLayout implements Layout {
    ListenableList<SLController> controllers = new ListenableList<>();

    List<CubesModel.Cube> cubes = new ArrayList<>();
    List<TreeModel.Branch> branches = new ArrayList<>();
    List<ButterfliesModel.Butterfly> butterflies = new ArrayList<>();

    // for SLController mac address lookup
    static Map<String, String> macToPhysid = new HashMap<>();
    static Map<String, String> physidToMac = new HashMap<>();

    static final float INCHES_PER_METER = 39.3701f;

    static final float globalOffsetX = 1315;
    static final float globalOffsetY = 598;
    static final float globalOffsetZ = 466;

    static final float globalRotationX = 0;
    static final float globalRotationY = 180;
    static final float globalRotationZ = 0;

    static final float CUBES_SPACING = 24f+9f;;
    static final float CUBES_Y_JUMP = 24f+12f;

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
        LXTransform transform = new LXTransform();
        transform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
        transform.rotateX(globalRotationX * Math.PI / 180.);
        transform.rotateY(globalRotationY * Math.PI / 180.);
        transform.rotateZ(globalRotationZ * Math.PI / 180.);

        /* Obj Importer ----------------------------------------------------*/
        List<LXModel> objModels = new ObjImporter("data", transform).getModels();

        List<Strip> strips = new ArrayList<>();
        LXPoint[] points = objModels.get(0).points;
        int stripLength = 100;

        for (int i = 0; i < points.length; i+=stripLength) {
            List<LXPoint> stripPoints = new ArrayList<LXPoint>();

            for (int i1 = 0; i1 < stripLength; i1++) {
                int index = i+i1;
                if (index < points.length) {
                    stripPoints.add(points[i+i1]);
                }
            }
            strips.add(new Strip(new Strip.Metrics(stripPoints.size()), stripPoints));
        }

        return new CompositeModel(strips);
    }

    private static Map<LX, WeakReference<ObjLayout>> instanceByLX = new WeakHashMap<>();

    public static ObjLayout getInstance(LX lx) {
        WeakReference<ObjLayout> weakRef = instanceByLX.get(lx);
        return weakRef == null ? null : weakRef.get();
    }

    public void setupLx(SLStudioLX lx) {
        instanceByLX.put(lx, new WeakReference<>(this));
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
    }
}