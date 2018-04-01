package com.symmetrylabs.layouts.composite;

import java.util.List;
import java.util.Arrays;
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
import heronarts.lx.output.FadecandyOutput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transform.LXTransform;
import heronarts.p3lx.ui.UI2dScrollContext;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.layouts.cubes.CubesModel;
import com.symmetrylabs.layouts.oslo.TreeModel;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.util.NetworkUtils;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.listenable.ListenableList;
import com.symmetrylabs.util.listenable.ListListener;

public class CompositeLayout implements Layout {
    //ListenableList<CubesController> controllers = new ListenableList<>();

    // for SLController mac address lookup
    static Map<String, String> macToPhysid = new HashMap<>();
    static Map<String, String> physidToMac = new HashMap<>();

    static final float INCHES_PER_METER = 39.3701f;

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;

    /**
     * Cubes
     *--------------------------------------------------------------------------------------*/
    static final TowerConfig[] TOWER_CONFIG = {
        new TowerConfig(0, 0, 0, new String[] {"0"}),
    };

    /**
     * Leaf Assemblages
     *--------------------------------------------------------------------------------------*/
    static final LeafAssemblageConfig[] LEAF_ASSEMBLAGE_CONFIG = {
        new LeafAssemblageConfig("0", new float[] {0, 0, 0}, new float[] {0, 0, 0})
    };

    /**
     * Branches
     *--------------------------------------------------------------------------------------*/
    static final BranchConfig[] BRANCH_CONFIG = {
        new BranchConfig("0", new float[] {0, 0, 0}, new float[] {0, 0, 0})
    };

    // /**
    //  * Icicles
    //  *--------------------------------------------------------------------------------------*/
    // static final IcicleConfig[] ICICLE_CONFIG = {
    //   new IcicleConfig(0, 0, 0, new String[] {"0"}),
    // };

    // /**
    //  * Bars
    //  *--------------------------------------------------------------------------------------*/
    // static final BarConfig[] BAR_CONFIG = {
    //   new BarConfig(0, 0, 0, new String[] {"0"}),
    // };

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
        transform.rotateY(globalRotationX * Math.PI / 180.);
        transform.rotateX(globalRotationY * Math.PI / 180.);
        transform.rotateZ(globalRotationZ * Math.PI / 180.);

        List<Strip> strips = new ArrayList<>();

        /**
         * Cubes
         *--------------------------------------------------------------------------------------*/
        List<CubesModel.Cube> cubes = new ArrayList<>();

        for (TowerConfig config : TOWER_CONFIG) {
            float x = config.x;
            float z = config.z;
            float xRot = config.xRot;
            float yRot = config.yRot;
            float zRot = config.zRot;
            CubesModel.Cube.Type type = config.type;

            for (int i = 0; i < config.ids.length; i++) {
                float y = config.yValues[i];
                CubesModel.Cube cube = new CubesModel.Cube(config.ids[i], x, y, z, xRot, yRot, zRot, transform, type);
                cubes.add(cube);
            }
        }

        for (CubesModel.Cube cube : cubes) {
            for (CubesModel.CubesStrip strip : cube.getStrips()) {
                strips.add((Strip)strip);
            }
        }

        /**
         * Leaf Assemblages
         *--------------------------------------------------------------------------------------*/
        for (LeafAssemblageConfig config : LEAF_ASSEMBLAGE_CONFIG) {
            transform.push();
            transform.translate(config.x, config.y, config.z);
            transform.rotateX(config.rx * Math.PI / 180f);
            transform.rotateY(config.ry * Math.PI / 180f);
            transform.rotateZ(config.rz * Math.PI / 180f);

            TreeModel.LeafAssemblage leafAssemblage = new TreeModel.LeafAssemblage(config.channel, transform);
            for (int i = 0; i < leafAssemblage.leaves.size(); i++) {
                strips.add(new Strip(
                    config.id+"_strip"+i,
                    new Strip.Metrics(leafAssemblage.leaves.size()),
                    new ArrayList<>(Arrays.asList(leafAssemblage.leaves.get(i).points))
                ));
            }
            transform.pop();
        }

        /**
         * Branches
         *--------------------------------------------------------------------------------------*/
        List<TreeModel.Branch> branches = new ArrayList<>();

        for (BranchConfig config : BRANCH_CONFIG) {
            transform.push();
            transform.translate(config.x, config.y, config.z);
            transform.rotateX(config.rx * Math.PI / 180f);
            transform.rotateY(config.ry * Math.PI / 180f);
            transform.rotateZ(config.rz * Math.PI / 180f);

            TreeModel.Branch branch = new TreeModel.Branch(transform);
            for (int i = 0; i < branch.leaves.size(); i++) {
                strips.add(new Strip(
                    config.id+"_strip"+i,
                    new Strip.Metrics(branch.leaves.size()),
                    new ArrayList<>(Arrays.asList(branch.leaves.get(i).points))
                ));
            }
            transform.pop();
        }
        /**
         * TO FINISH....
         */

        return new CompositeModel(); //new CompositeModel(strips);
    }

    /*
    public static LXModel importObjModel() {
        return new LXModel(new ObjImporter("data", globalTransform).getModels().toArray(new LXModel[0]));
    }
    */

    private static Map<LX, WeakReference<CompositeLayout>> instanceByLX = new WeakHashMap<>();

    public static CompositeLayout getInstance(LX lx) {
        WeakReference<CompositeLayout> weakRef = instanceByLX.get(lx);
        return weakRef == null ? null : weakRef.get();
    }

    public void setupLx(SLStudioLX lx) {
        instanceByLX.put(lx, new WeakReference<>(this));
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
    }

    static class TowerConfig {

        static final float CUBE_WIDTH = 24;
        static final float CUBE_HEIGHT = 24;
        static final float TOWER_WIDTH = 24;
        static final float TOWER_HEIGHT = 24;
        static final float CUBE_SPACING = 2.5f;

        static final float TOWER_VERTICAL_SPACING = 2.5f;
        static final float TOWER_RISER = 14;
        static final float SP = 24;
        static final float JUMP = TOWER_HEIGHT+TOWER_VERTICAL_SPACING;

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

    static class LeafAssemblageConfig {
        final String id;
        final float x;
        final float y;
        final float z;
        final float rx;
        final float ry;
        final float rz;
        final int channel;

        LeafAssemblageConfig(String id, float[] coordinates, float[] rotations) {
            this(id, coordinates, rotations, 0);
        }

        LeafAssemblageConfig(String id, float[] coordinates, float[] rotations, int channel) {
            this.id = id;
            this.x = coordinates[0];
            this.y = coordinates[1];
            this.z = coordinates[2];
            this.rx = rotations[0];
            this.ry = rotations[1];
            this.rz = rotations[2];
            this.channel = channel;
        }
    }

    static class BranchConfig {
        final String id;
        final float x;
        final float y;
        final float z;
        final float rx;
        final float ry;
        final float rz;

     BranchConfig(String id, float[] coordinates, float[] rotations) {
            this.id = id;
            this.x = coordinates[0];
            this.y = coordinates[1];
            this.z = coordinates[2];
            this.rx = rotations[0];
            this.ry = rotations[1];
            this.rz = rotations[2];
        }
    }
}
