package com.symmetrylabs.shows.composite;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.lang.ref.WeakReference;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.util.CubePhysicalIdMap;
import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transform.LXTransform;
import heronarts.lx.output.LXDatagramOutput;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.slstudio.output.SLController;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.oslo.TreeModel;
import com.symmetrylabs.shows.icicles.Icicle;
import com.symmetrylabs.shows.butterflies.ButterfliesModel;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.listenable.ListenableSet;
import com.symmetrylabs.util.listenable.SetListener;
import com.symmetrylabs.slstudio.output.TenereDatagram;

public class CompositeShow implements Show {
    ListenableSet<SLController> controllers = new ListenableSet<>();
    CubePhysicalIdMap cubePhysicalIdMap = CubePhysicalIdMap.loadFromDisk();

    List<CubesModel.Cube> cubes = new ArrayList<>();
    List<TreeModel.Branch> branches = new ArrayList<>();
    List<ButterfliesModel.Butterfly> butterflies = new ArrayList<>();

    static final float INCHES_PER_METER = 39.3701f;

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;

    static final float CUBES_SPACING = 24f+9f;;
    static final float CUBES_Y_JUMP = 24f+12f;

    /**
     * Cubes
     *--------------------------------------------------------------------------------------*/
    static final TowerConfig[] TOWER_CONFIG = {
        new TowerConfig(0, 0, 0, -45, new String[] { "326", "198", "6","50" }),
        new TowerConfig(CUBES_SPACING * 1, 0, 0, -45, new String[] { "418", "203", "54" }),
        new TowerConfig(CUBES_SPACING * 2, 0, 0, -45, new String[] { "150", "312", "129" }),
        new TowerConfig(CUBES_SPACING * 3, 0, 0, -45, new String[] { "172", "79", "111", "177" }),
        new TowerConfig(CUBES_SPACING * 1.5f, CUBES_Y_JUMP * 3, 0, -45, new String[] {"87"}),
        new TowerConfig(CUBES_SPACING * 0.5f, 0, -24*2, -45, new String[] {"340", "135", "391", "390"}),
        new TowerConfig(CUBES_SPACING * 1.5f, 0, -24*2, -45, new String[] {"182", "398", "94" }),
        new TowerConfig(CUBES_SPACING * 2.5f, 0, -24*2, -45, new String[] {"29", "30", "199", "27"}),
        new TowerConfig(CubesModel.Cube.Type.MEDIUM, CUBES_SPACING * 2.5f + 6, 0, -24*2, -45, new String[] { "143"}),
        new TowerConfig(CubesModel.Cube.Type.MEDIUM, CUBES_SPACING * .5f + 6, 0, -24*2, -45, new String[] { "393"}),
        new TowerConfig(CUBES_SPACING * 1, 0, -24*4, -45, new String[] { "383", "211", "d8:80:39:9b:23:ad"}),
        new TowerConfig(CUBES_SPACING * 2, 0, -24*4, -45, new String[] { "196", "18", "361"}),
        new TowerConfig(CubesModel.Cube.Type.MEDIUM, CUBES_SPACING * 1+6, 0, -24*4, -45, new String[] { "210"}),
        new TowerConfig(CubesModel.Cube.Type.MEDIUM, CUBES_SPACING * 2+6, 0, -24*4, -45, new String[] { "345"}),
        new TowerConfig(CubesModel.Cube.Type.SMALL, CUBES_SPACING * 1+12, 0, -24*4, -45, new String[] { "82"}),
        new TowerConfig(CubesModel.Cube.Type.SMALL, CUBES_SPACING * 2+12, 0, -24*4, -45, new String[] { ""}),
        new TowerConfig(43.5f, 0, -24*6, -45, new String[] { "74", "63", "33"}),
        new TowerConfig(CubesModel.Cube.Type.MEDIUM, CUBES_SPACING * 1.5f, 0, -24*6, -45, new String[] { "334"}),
        new TowerConfig(CubesModel.Cube.Type.SMALL, CUBES_SPACING * 1.75f, 0, -24*6, -45, new String[] { "384"})
    };

    /**
     * Leaf Assemblages
     *--------------------------------------------------------------------------------------*/
    static final LeafAssemblageConfig[] LEAF_ASSEMBLAGE_CONFIG = {
        //new LeafAssemblageConfig("0", new float[] {100, 0, 0}, new float[] {0, 0, 0})
    };

    /**
     * Branches
     *--------------------------------------------------------------------------------------*/
    static final BranchConfig[] BRANCH_CONFIG = {
        new BranchConfig("branch1", new float[] {300, 50, 0}, new float[] {-30, 0, 40}),
        new BranchConfig("branch2", new float[] {300, 45, -5}, new float[] {-45, 0, 0})
    };

    /**
     * Icicles
     *--------------------------------------------------------------------------------------*/
    static final IcicleConfig[] ICICLE_CONFIG = {
        //new IcicleConfig("0", new float[] {300, 0, 0}, new float[] {0, 0, 0}, 72)
    };

    /**
     * Butterflies
     *--------------------------------------------------------------------------------------*/
    static final ButterflyConfig[] BUTTERFLY_CONFIG = {
        new ButterflyConfig("butterfly1", new float[] {270, 50, -25}, new float[] {0, 80, -40}, ButterfliesModel.Butterfly.Type.SHARP_CURVY),
        new ButterflyConfig("butterfly2", new float[] {300, 50, -30}, new float[] {0, 70, -5}, ButterfliesModel.Butterfly.Type.CURVY)
    };

    // /**
    //  * Bars
    //  *--------------------------------------------------------------------------------------*/
    // static final BarConfig[] BAR_CONFIG = {
    //   new BarConfig(0, 0, 0, new String[] {"0"}),
    // };

    public SLModel buildModel() {
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
        for (TowerConfig config : TOWER_CONFIG) {
            float x = config.x;
            float z = config.z;
            float xRot = config.xRot;
            float yRot = config.yRot;
            float zRot = config.zRot;
            CubesModel.Cube.Type type = config.type;

            for (int i = 0; i < config.ids.length; i++) {
                float y = config.yValues[i];
                cubes.add(new CubesModel.Cube(config.ids[i], x, y, z, xRot, yRot, zRot, transform, type));
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
        for (BranchConfig config : BRANCH_CONFIG) {
            transform.push();
            transform.translate(config.x, config.y, config.z);
            transform.rotateX(config.rx * Math.PI / 180f);
            transform.rotateY(config.ry * Math.PI / 180f);
            transform.rotateZ(config.rz * Math.PI / 180f);

            TreeModel.Branch branch = new TreeModel.Branch(transform);
            branches.add(branch);
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
         * Icicles
         *--------------------------------------------------------------------------------------*/
        for (IcicleConfig config : ICICLE_CONFIG) {
            transform.push();
            String id = config.id;
            float x = config.x;
            float y = config.y;
            float z = config.z;
            float rx = config.rx;
            float ry = config.ry;
            float rz = config.rz;
            Icicle.Metrics metrics = new Icicle.Metrics(config.numPoints, config.pixelPitch);

            Icicle icicle = new Icicle(id, x, y, z, rx, ry, rz, transform, metrics);
            strips.addAll(icicle.getStrips());
            transform.pop();
        }

        /**
         * Butterflies
         *--------------------------------------------------------------------------------------*/
        for (ButterflyConfig config : BUTTERFLY_CONFIG) {
            transform.push();
            String id = config.id;
            float x = config.x;
            float y = config.y;
            float z = config.z;
            float rx = config.rx;
            float ry = config.ry;
            float rz = config.rz;
            ButterfliesModel.Butterfly.Type type = config.type;

            ButterfliesModel.Butterfly butterfly = new ButterfliesModel.Butterfly(id, x, y, z, rx, ry, rz, type, transform);
            butterflies.add(butterfly);
            strips.addAll(butterfly.getStrips());
            transform.pop();
        }

        /**
         * TODO: add bars and butterflies
         */

        return new CompositeModel(strips);
    }

    private static Map<LX, WeakReference<CompositeShow>> instanceByLX = new WeakHashMap<>();

    public static CompositeShow getInstance(LX lx) {
        WeakReference<CompositeShow> weakRef = instanceByLX.get(lx);
        return weakRef == null ? null : weakRef.get();
    }

    public void setupLx(SLStudioLX lx) {
        instanceByLX.put(lx, new WeakReference<>(this));

        final NetworkMonitor networkMonitor = NetworkMonitor.getInstance(lx).start();
        final Dispatcher dispatcher = Dispatcher.getInstance(lx);

        /**
         * TODO: We need to workout a slick way of arbitrarily mapping points to controllers...
         */

        // Put cubes on SLControllers
        networkMonitor.opcDeviceList.addListener(new SetListener<NetworkDevice>() {
            public void onItemAdded(NetworkDevice device) {
                String physicalId = cubePhysicalIdMap.getPhysicalId(device.deviceId);
                final PointsGrouping points = new PointsGrouping(physicalId);

                for (CubesModel.Cube cube : cubes) {
                    if (cube.id.equals(physicalId)) {
                        // this should live somewhere
                        List<Strip> strips = ((StripsModel)cube).getStrips();

                        points.addPoints(strips.get(6).points)
                                    .addPoints(strips.get(7).points)
                                    .addPoints(strips.get(8).points)
                                    .addPoints(strips.get(9).points)
                                    .addPoints(strips.get(10).points)
                                    .addPoints(strips.get(11).points)
                                    .addPoints(strips.get(0).points)
                                    .addPoints(strips.get(1).points)
                                    .addPoints(strips.get(2).points)
                                    .addPoints(strips.get(3).points)
                                    .addPoints(strips.get(4).points)
                                    .addPoints(strips.get(5).points);
                    }
                }

                final SLController controller = new SLController(lx, device, points);
                controllers.add(controller);
                dispatcher.dispatchNetwork(() -> lx.addOutput(controller));
            }

            public void onItemRemoved(NetworkDevice device) {
                final SLController controller = getControllerByDevice(device);
                controllers.remove(controller);
                dispatcher.dispatchNetwork(() -> {
                    //lx.removeOutput(controller);
                });
            }
        });

        lx.engine.output.enabled.addListener(param -> {
            boolean isEnabled = ((BooleanParameter) param).isOn();
            for (SLController controller : controllers) {
                controller.enabled.setValue(isEnabled);
            }
        });

        // Put branches on TenereDatagrams
        try {
            addTenereDatagram(lx, new PointsGrouping(branches.get(0).points).getIndicesInRange(0, 420),   (byte) 0x00, "10.200.1.67");
            addTenereDatagram(lx, new PointsGrouping(branches.get(0).points).getIndicesInRange(420, 840), (byte) 0x04, "10.200.1.67");
            addTenereDatagram(lx, new PointsGrouping(branches.get(1).points).getIndicesInRange(0, 420),   (byte) 0x00, "10.200.1.81");
            addTenereDatagram(lx, new PointsGrouping(branches.get(1).points).getIndicesInRange(420, 840), (byte) 0x04, "10.200.1.81");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Put the butterflies on Pixlite
        lx.addOutput(new SimplePixlite(lx, "10.200.1.10")
            .addPixliteOutput(new PointsGrouping("1", butterflies.get(0).points))
            .addPixliteOutput(new PointsGrouping("2", butterflies.get(1).points))
        );
    }

    public SLController getControllerByDevice(NetworkDevice device) {
        for (SLController controller : controllers) {
            if (controller.networkDevice == device) {
                return controller;
            }
        }
        return null;
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
    }

    public static void addTenereDatagram(LX lx, int[] indices, byte channel, String ip) throws SocketException, UnknownHostException {
        lx.addOutput(
            new LXDatagramOutput(lx).addDatagram(new TenereDatagram(lx, indices, channel).setAddress(ip).setPort(1337))
        );
    }

    static class TowerConfig {

        static final float CUBE_WIDTH = 24;
        static final float CUBE_HEIGHT = 24;
        static final float TOWER_WIDTH = 24;
        static final float TOWER_HEIGHT = 24;
        static final float CUBE_SPACING = 2.5f;

        static final float TOWER_VERTICAL_SPACING = 2.5f;
        static final float TOWER_RISER = 14;

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

    static class IcicleConfig {
        final String id;
        final float x;
        final float y;
        final float z;
        final float rx;
        final float ry;
        final float rz;
        final int numPoints;
        final float pixelPitch;

        IcicleConfig(String id, float[] coordinates, float[] rotations, int numPoints) {
            this(id, coordinates, rotations, numPoints, 0.54f);
        }

        IcicleConfig(String id, float[] coordinates, float[] rotations, int numPoints, float pixelPitch) {
            this.id = id;
            this.x = coordinates[0];
            this.y = coordinates[1];
            this.z = coordinates[2];
            this.rx = rotations[0];
            this.ry = rotations[1];
            this.rz = rotations[2];
            this.numPoints = numPoints;
            this.pixelPitch = pixelPitch;
        }
    }

    static class ButterflyConfig {
        final String id;
        final float x;
        final float y;
        final float z;
        final float rx;
        final float ry;
        final float rz;
        final ButterfliesModel.Butterfly.Type type;

        ButterflyConfig(String id, float[] coordinates, float[] rotations, ButterfliesModel.Butterfly.Type type) {
            this.id = id;
            this.x = coordinates[0];
            this.y = coordinates[1];
            this.z = coordinates[2];
            this.rx = rotations[0];
            this.ry = rotations[1];
            this.rz = rotations[2];
            this.type = type;
        }
    }
}
