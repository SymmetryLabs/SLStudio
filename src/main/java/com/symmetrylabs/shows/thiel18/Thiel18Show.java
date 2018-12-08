package com.symmetrylabs.shows.thiel18;

import java.util.*;


import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.shows.cubes.UICubesMappingPanel;
import com.symmetrylabs.shows.cubes.UICubesOutputs;
import com.symmetrylabs.slstudio.model.SLModel;

import heronarts.lx.transform.LXTransform;
import heronarts.p3lx.ui.UI2dScrollContext;

import com.symmetrylabs.slstudio.SLStudioLX;

public class Thiel18Show extends CubesShow {
    public static final String SHOW_NAME = "thiel18";

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;

    static final float CUBE_WIDTH = 24;
    static final float CUBE_HEIGHT = 24;
    static final float TOWER_WIDTH = 24;
    static final float TOWER_HEIGHT = 24;
    static final float CUBE_SPACING = 1.5f;

    static final float TOWER_VERTICAL_SPACING = 0;
    static final float TOWER_RISER = 14;
    static final float SP = 25.5f;
    static final float JUMP = TOWER_HEIGHT+TOWER_VERTICAL_SPACING;

    static final float INCHES_PER_METER = 39.3701f;

    static final ClusterConfig[] clusters = new ClusterConfig[] {
        new ClusterConfig("US", 0, 0, 0, new TowerConfig[] {
                new TowerConfig(-4*SP, SP, 0, new String[][] {
                        new String[] {"", ""},
                        new String[] {"", ""},
                    }),
                new TowerConfig(-3*SP, 0, 0, new String[][] {
                        new String[] {"", ""},
                        new String[] {"", ""},
                        new String[] {"", ""},
                        new String[] {"", ""},
                    }),
                new TowerConfig(-2*SP, 0, 0, new String[][] {
                        new String[] {"", ""},
                        new String[] {"", ""},
                        new String[] {"", ""},
                        new String[] {"", ""},
                        new String[] {"", ""},
                    }),
                new TowerConfig(-1*SP, 0, 0, new String[][] {
                        new String[] {"", ""},
                        new String[] {"", ""},
                        new String[] {"", ""},
                        new String[] {"", ""},
                        new String[] {"", ""},
                    }),
                new TowerConfig(0*SP, 0, 0, new String[][] {
                        new String[] {"", ""},
                        new String[] {"", ""},
                        new String[] {"", ""},
                        new String[] {"", ""},
                        new String[] {"", ""},
                    }),
                new TowerConfig(1*SP, 0, 0, new String[][] {
                        new String[] {"", ""},
                        new String[] {"", ""},
                        new String[] {"", ""},
                        new String[] {"", ""},
                        new String[] {"", ""},
                    }),
                new TowerConfig(2*SP, 0, 0, new String[][] {
                        new String[] {"", ""},
                        new String[] {"", ""},
                        new String[] {"", ""},
                        new String[] {"", ""},
                    }),
                new TowerConfig(3*SP, 0, 0, new String[][] {
                        new String[] {"", ""},
                        new String[] {"", ""},
                        new String[] {"", ""},
                    }),
                new TowerConfig(-3.5f*SP, 0, -SP, new String[][] {
                        new String[] {"", ""},
                        new String[] {"", ""},
                        new String[] {"", ""},
                    }),
                new TowerConfig(-3.5f*SP, 0, -2*SP, new String[][] {
                        new String[] {"", ""},
                        new String[] {"", ""},
                    }),
                new TowerConfig(2.5f*SP, 0, -SP, new String[][] {
                        new String[] {"", ""},
                        new String[] {"", ""},
                        new String[] {"", ""},
                    }),
                new TowerConfig(2.5f*SP, 0, -2*SP, new String[][] {
                        new String[] {"", ""},
                        new String[] {"", ""},
                    }),
            }),
    };

    static class ClusterConfig {
        final String id;
        final float x;
        final float y;
        final float z;
        final float ry;
        final TowerConfig[] configs;

        ClusterConfig(String id, float x, float y, float z, float ry, TowerConfig[] configs) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.z = z;
            this.ry = ry;
            this.configs = configs;
        }

        ClusterConfig(String id, float x, float y, float z, TowerConfig[] configs) {
            this(id, x, y, z, 0, configs);
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
        final String[][] ids;
        final float[] yValues;

        TowerConfig(float x, float y, float z, String[][] ids) {
            this(CubesModel.Cube.Type.LARGE, x, y, z, ids);
        }

        TowerConfig(float x, float y, float z, float yRot, String[][] ids) {
            this(x, y, z, 0, yRot, 0, ids);
        }

        TowerConfig(CubesModel.Cube.Type type, float x, float y, float z, String[][] ids) {
            this(type, x, y, z, 0, 0, 0, ids);
        }

        TowerConfig(CubesModel.Cube.Type type, float x, float y, float z, float yRot, String[][] ids) {
            this(type, x, y, z, 0, yRot, 0, ids);
        }

        TowerConfig(float x, float y, float z, float xRot, float yRot, float zRot, String[][] ids) {
            this(CubesModel.Cube.Type.LARGE, x, y, z, xRot, yRot, zRot, ids);
        }

        TowerConfig(CubesModel.Cube.Type type, float x, float y, float z, float xRot, float yRot, float zRot, String[][] ids) {
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

        List<CubesModel.Tower> towers = new ArrayList<>();
        List<CubesModel.Cube> allCubes = new ArrayList<>();

        for (ClusterConfig cluster : clusters) {
            List<CubesModel.Cube> cubes = new ArrayList<>();

            globalTransform.push();
            globalTransform.translate(cluster.x, cluster.y, cluster.z);
            globalTransform.rotateY(cluster.ry);

            for (TowerConfig config : cluster.configs) {
                float x = config.x;
                float z = config.z;

                float rX = config.xRot;
                float rY = config.yRot;
                float rZ = config.zRot;

                for (int i = 0; i < config.ids.length; i++) {
                    String idA = config.ids[i][0];
                    String idB = config.ids[i][1];
                    float y = config.yValues[i];
                    CubesModel.DoubleControllerCube cube =
                        new CubesModel.DoubleControllerCube(
                            idA, idB, x, y, z, rX, rY, rZ, globalTransform);
                    cubes.add(cube);
                    allCubes.add(cube);
                }
            }
            globalTransform.pop();
            towers.add(new CubesModel.Tower(cluster.id, cubes));
        }

        CubesModel.Cube[] allCubesArr = new CubesModel.Cube[allCubes.size()];
        for (int i = 0; i < allCubesArr.length; i++) {
            allCubesArr[i] = allCubes.get(i);
        }

        CubesModel model = new CubesModel(towers, allCubesArr);
        model.setTopologyTolerances(6, 6, 8);
        return model;
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        UI2dScrollContext utility = ui.rightPane.utility;
        new UICubesOutputs(lx, ui, this, 0, 0, utility.getContentWidth()).addToContainer(utility);
        new UICubesMappingPanel(lx, ui, 0, 0, utility.getContentWidth()).addToContainer(utility);
    }
}
