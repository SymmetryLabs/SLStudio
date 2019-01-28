package com.symmetrylabs.shows.thiel18;

import java.util.*;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.shows.cubes.UICubesMappingPanel;
import com.symmetrylabs.shows.cubes.UICubesOutputs;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.showplugins.FaderLimiter;
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
                /* back layer */
                new TowerConfig(-5*SP, SP - 4, 0, new String[][] {
                        new String[] {"550", "551"},
                    }),
                new TowerConfig(-4*SP, SP, 0, new String[][] {
                        new String[] {"702", "366"},
                        new String[] {"808", "809"},
                    }),
                new TowerConfig(-3*SP, 0, 0, new String[][] {
                        new String[] {"750", "751"},
                        new String[] {"526", "527"},
                        new String[] {"466", "467"},
                        new String[] {"723", "442"},
                    }),
                new TowerConfig(-2*SP, 0.5f*SP, 0, new String[][] {
                        new String[] {"970", "778"},
                        new String[] {"994", "971"},
                        new String[] {"1070", "1097"},
                        new String[] {"902", "1078"},
                    }),
                new TowerConfig(-1*SP, 0.5f*SP, 0, new String[][] {
                        new String[] {"1020", "991"},
                        new String[] {"1038", "1037"},
                        new String[] {"558", "559"},
                        new String[] {"710", "711"},
                        new String[] {"746", "747"},
                    }),
                new TowerConfig(-0.5f*SP, 5.5f*SP, 0, new String[][] {
                        new String[] {"874", "875"},
                    }),
                new TowerConfig(0*SP, 0.5f*SP, 0, new String[][] {
                        new String[] {"1039", "1012"},
                        new String[] {"1024", "977"},
                        new String[] {"646", "647"},
                        new String[] {"766", "767"},
                        new String[] {"774", "775"},
                    }),
                new TowerConfig(1*SP, 0.5f*SP, 0, new String[][] {
                        new String[] {"1063", "911"},
                        new String[] {"792", "700"},
                        new String[] {"462", "962"},
                        new String[] {"474", "475"},
                    }),
                new TowerConfig(2*SP, 0, 0, new String[][] {
                        new String[] {"1110", "953"},
                        new String[] {"812", "813"},
                        new String[] {"648", "649"},
                        new String[] {"1106", "1107"},
                    }),
                new TowerConfig(3*SP, 0, 0, new String[][] {
                        new String[] {"894", "893"},
                        new String[] {"885", "897"},
                        new String[] {"821", "443"},
                    }),
                new TowerConfig(4*SP, 0, 0, new String[][] {
                        new String[] {"1073", "1072"},
                        new String[] {"602", "603"},
                    }),

                /* layer 2 */
                new TowerConfig(-3.5f*SP, 0, -1*SP, new String[][] {
                        new String[] {"738", "739"},
                        new String[] {"552", "553"},
                        new String[] {"853", "852"},
                    }),
                new TowerConfig(-2.5f*SP, 0.5f*SP, -1*SP, new String[][] {
                        new String[] {"983", "531"},
                        new String[] {"831", "498"},
                        new String[] {"795", "794"},
                        new String[] {"864", "865"},
                    }),
                new TowerConfig(1.5f*SP, 0.5f*SP, -1*SP, new String[][] {
                        new String[] {"588", "589"},
                        new String[] {"724", "725"},
                        new String[] {"1016", "1017"},
                        new String[] {"582", "583"},
                    }),
                new TowerConfig(2.5f*SP, 0, -1*SP, new String[][] {
                        new String[] {"832", "833"},
                        new String[] {"586", "587"},
                        new String[] {"760", "761"},
                    }),

                /* layer 3 */
                new TowerConfig(-3.5f*SP, 0, -2*SP, new String[][] {
                        new String[] {"484", "485"},
                        new String[] {"698", "699"},
                    }),
                new TowerConfig(2.5f*SP, 0, -2*SP, new String[][] {
                        new String[] {"913", "546"},
                        new String[] {"532", "518"},
                    }),

                /* layer 4 */
                new TowerConfig(-3.5f*SP, 0, -3*SP, new String[][] {
                        new String[] {"668", "669"},
                    }),
            }),
        new ClusterConfig("door", 20*12, 0, -30*12, new TowerConfig[] {
                new TowerConfig(0, 0, -SP, new String[][] {
                        new String[] {"566", "567"},
                        new String[] {"872", "873"},
                        new String[] {"848", "849"},
                    }),
                new TowerConfig(0, 6, 0, new String[][] {
                        new String[] {"964", "967"},
                        new String[] {"976", "968"},
                        new String[] {"596", "446"},
                        new String[] {"1022", "1032"},
                    }),
                new TowerConfig(0, 6, 144, new String[][] {
                        new String[] {"600", "601"},
                        new String[] {"489", "975"},
                        new String[] {"574", "549"},
                        new String[] {"1040", "1030"},
                    }),
                new TowerConfig(0, 0, 144+SP, new String[][] {
                        new String[] {"1045", "1046"},
                        new String[] {"959", "954"},
                        new String[] {"972", "1081"},
                    }),
            }),
        new ClusterConfig("pool-towers", -15*12, 0, -10*12 -4*SP, new TowerConfig[] {
                new TowerConfig(0, 0, 0, 90, new String[][] {
                        new String[] {"664", "665"},
                        new String[] {"1056", "591"},
                        new String[] {"732", "733"},
                    }),
                new TowerConfig(0, 0, -20*12, 90, new String[][] {
                        new String[] {"764", "765"},
                        new String[] {"608", "609"},
                        new String[] {"780", "781"},
                    }),
                new TowerConfig(-25*12, 0, 24, 90, new String[][] {
                        new String[] {"820", "1060"},
                        new String[] {"736", "737"},
                        new String[] {"858", "859"},
                    }),
                new TowerConfig(-26*12, 0, -9*12, 90, new String[][] {
                        new String[] {"450", "451"},
                        new String[] {"802", "1011"},
                        new String[] {"448", "449"},
                    }),
                new TowerConfig(-25*12, 0, -20*12, 90, new String[][] {
                        new String[] {"845", "844"},
                        new String[] {"816", "499"},
                        new String[] {"506", "507"},
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
                    String idA = config.ids[i][1];
                    String idB = config.ids[i][0];
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
        FaderLimiter.attach(lx, 0.72f);
    }
}
