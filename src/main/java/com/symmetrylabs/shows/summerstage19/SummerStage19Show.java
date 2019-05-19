package com.symmetrylabs.shows.summerstage19;

import com.symmetrylabs.slstudio.showplugins.FaderLimiter;
import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.shows.cubes.UICubesMappingPanel;
import com.symmetrylabs.shows.cubes.UICubesOutputs;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.transform.LXTransform;
import heronarts.p3lx.ui.UI2dScrollContext;
import heronarts.lx.LX;

import java.util.ArrayList;
import java.util.List;

/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class SummerStage19Show extends CubesShow {
    public static final String SHOW_NAME = "summerstage19";

    static final float CUBE_WIDTH = 24;
    static final float CUBE_HEIGHT = 24;
    static final float TOWER_WIDTH = 24;
    static final float TOWER_HEIGHT = 24;
    static final float CUBE_SPACING = 1.5f;

    static final float TOWER_VERTICAL_SPACING = 0;
    static final float TOWER_RISER = 14;
    static final float SP = CUBE_WIDTH + CUBE_SPACING;
    static final float JUMP = TOWER_HEIGHT+TOWER_VERTICAL_SPACING;

    static final float INCHES_PER_METER = 39.3701f;

    private static final ClusterConfig[] clusters = new ClusterConfig[] {
        new ClusterConfig("L1", 0, 4, 6,
            new CubeConfig("A1.5", 0, 0.5, 0), //1027
            new CubeConfig("B1", 1, 0, 0), // 944
            new CubeConfig("B2", 1, 1, 0), // 587
            new CubeConfig("B3", 1, 2, 0)), // 997
        new ClusterConfig("L2", 2, 3.5, 7,
            new CubeConfig("A1", 0, 0, 0), // 924 
            new CubeConfig("A2", 0, 1, 0)), // 954
        new ClusterConfig("L3", 3, 5, 7,
            new CubeConfig("A1", 0, 0, 0), // 669
            new CubeConfig("A2", 0, 1, 0), // 1248
            new CubeConfig("A3", 0, 2, 0)), // 875
        new ClusterConfig("L4", 4, 5, 7,
            new CubeConfig("A1", 0, 0, 0), // 204
            new CubeConfig("A2", 0, 1, 0), // 
            new CubeConfig("A3", 0, 2, 0), // 438
            new CubeConfig("A4", 0, 3, 0)), // 495 
        new ClusterConfig("L5", 4, 1, 7,
            new CubeConfig("A1", 0, 0, 0), // 935
            new CubeConfig("A2", 0, 1, 0), // 481
            new CubeConfig("A3", 0, 2, 0), // 559
            new CubeConfig("A4", 0, 3, 0)), // 679
        new ClusterConfig("L6", 5, 4, 7,
            new CubeConfig("A1", 0, 0, 0), // 467
            new CubeConfig("A2", 0, 1, 0), // 1244
            new CubeConfig("A3", 0, 2, 0), // 1041
            new CubeConfig("A4", 0, 3, 0), // 906
            new CubeConfig("A5", 0, 4, 0)), // 751
        new ClusterConfig("L7", 5, 0, 7,
            new CubeConfig("A1", 0, 0, 0), // 1206
            new CubeConfig("A2", 0, 1, 0), // 876
            new CubeConfig("A3", 0, 2, 0), // 700
            new CubeConfig("A4", 0, 3, 0)), // 601
        new ClusterConfig("L8", 6, 5, 7,
            new CubeConfig("A1", 0, 0, 0), // 741
            new CubeConfig("A2", 0, 1, 0), // 591
            new CubeConfig("A3", 0, 2, 0)), // 813
        new ClusterConfig("L9", 6, 1, 8,
            new CubeConfig("A1", 0, 0, 0), // 1107
            new CubeConfig("A2", 0, 1, 0), // 823
            new CubeConfig("A3", 0, 2, 0), // 833
            new CubeConfig("A4", 0, 3, 0)), // 1009
        new ClusterConfig("LC1", 6, 6.5, 5,
            new CubeConfig("A1A", 0, 0, 0), // 991
            new CubeConfig("A2A", 0, 1, 0), // 
            new CubeConfig("A3A", 0, 2, 0), // 509
            new CubeConfig("A1.5B", 0, 0.5, 1), // 1234
            new CubeConfig("A2.5B", 0, 1.5, 1)), // 443
        new ClusterConfig("LC2", 7, 5.5, 1,
            new CubeConfig("A2A", 0, 1, 0), // 
            new CubeConfig("A1B", 0, 0, 1), // 1212
            new CubeConfig("A2B", 0, 1, 1), // 1231
            new CubeConfig("A3B+", 0, 2, 1.5), // 1241
            new CubeConfig("A1C", 0, 0, 2), // 1049
            new CubeConfig("A2C", 0, 1, 2), // 
            new CubeConfig("A2D", -0.25, 0, 3)), // 1368 
        new ClusterConfig("CS1", 7, 5, 1,
            new CubeConfig("A1.5", 0, 0.5, 0), // 767
            new CubeConfig("B1", 1, 0, 0), // 667
            new CubeConfig("B2", 1, 1, 0), // 1069
            new CubeConfig("B3", 1, 2, 0), // 1226
            new CubeConfig("C1", 2, 0, 0), // 1011
            new CubeConfig("C2", 2, 1, 0)), // 644
        new ClusterConfig("CS2", 10, 4, 1,
            new CubeConfig("A3A", 0, 2, 0), // 1246
            new CubeConfig("A4A", 0, 3, 0), // 1363
            // new CubeConfig("A3B", 0, 2, 1), // 896 ----------------------------------------------------
            new CubeConfig("A3B", 0, 4, 1, 0, 0, 180), // 896 ----------------------------------------------------
            new CubeConfig("B1A", 1, 0, 0), // 967
            new CubeConfig("B2A", 1, 1, 0), // 1093
            new CubeConfig("B3A", 1, 2, 0), // 757
            new CubeConfig("B3B", 1, 2, 1), // 1239
            new CubeConfig("B4B", 1, 3, 1), // 1341
            new CubeConfig("B5B", 1, 4, 1), // 1096
            new CubeConfig("B6B", 1, 5, 1), // 1351
            new CubeConfig("C3A+", 2, 2, 0.5), // 975
            new CubeConfig("C4A+", 2, 3, 0.5)), // 949
        new ClusterConfig("CS3", 13, 4, 1,
            new CubeConfig("A3B", 0, 2, 1), // 1335
            new CubeConfig("A4B", 0, 3, 1), // 1333
            new CubeConfig("A5B", 0, 4, 1), // 1327
            new CubeConfig("B1A", 1, 0, 0), // 1325
            new CubeConfig("B2A", 1, 1, 0), // 361 (controller 361 currently out)
            new CubeConfig("B3A", 1, 2, 0), // 945
            new CubeConfig("B3B", 1, 2, 1), // 1337
            new CubeConfig("C3A", 2, 2, 0), // 1329
            new CubeConfig("C3B", 2, 2, 1), // 353
            new CubeConfig("C4B", 2, 3, 1)), // 1331
        new ClusterConfig("CS4", 16, 5, 1,
            new CubeConfig("A3A", 0, 2, 0), // 765
            new CubeConfig("A4A", 0, 3, 0), // 1339
            new CubeConfig("A5A", 0, 4, 0), // 1238
            new CubeConfig("A3B", 0, 2, 1), // 1362
            new CubeConfig("B1A", 1, 0, 0), // 475
            new CubeConfig("B2A", 1, 1, 0), // 1221
            new CubeConfig("B3A", 1, 2, 0), // 572
            new CubeConfig("C1A", 2, 0, 0), // 1097
            new CubeConfig("C2A", 2, 1, 0), // 685
            new CubeConfig("C3A", 2, 2, 0), // 366
            new CubeConfig("C4A", 2, 3, 0), // 517
            new CubeConfig("C5A", 2, 4, 0), // 781
            new CubeConfig("C2B", 2, 1, 1), // 1365
            new CubeConfig("C3B", 2, 2, 1)), // 908
        new ClusterConfig("CS5", 19, 6, 1,
            new CubeConfig("A3A", 0, 2, 0), // 516
            new CubeConfig("A4A", 0, 3, 0), // 926
            new CubeConfig("A1B", 0, 0, 1), // 507
            new CubeConfig("A2B", 0, 1, 1), // 551
            new CubeConfig("A3B", 0, 2, 1), // 916
            new CubeConfig("B1B", 1, 0, 1), // 499
            new CubeConfig("B2B", 1, 1, 1), // 942
            new CubeConfig("B3B", 1, 2, 1), // 1081
            new CubeConfig("B4B", 1, 3, 1), // 1030
            new CubeConfig("B5B", 1, 4, 1)), // 553
        new ClusterConfig("CS6", 21, 4, 0,

            new CubeConfig("A3C", 0, 2, 2), // 1012
            new CubeConfig("A4C", 0, 3, 2), // 449
            new CubeConfig("A3B", 0, 2, 1), // 1354
            new CubeConfig("A4B", 0, 3, 1), // 1072
            new CubeConfig("B1A+", 1, 0, 0.5), // 707
            new CubeConfig("B2A+", 1, 1, 0.5), // 447
            new CubeConfig("B3A+", 1, 2, 0.5), // 818
            new CubeConfig("B4A+", 1, 3, 0.5), // 1358
            new CubeConfig("B5A+", 1, 4, 0.5), // 1348
            new CubeConfig("B6A+", 1, 5, 0.5), // 1130
            new CubeConfig("C2B", 2, 1, 1), // 931
            new CubeConfig("C3B", 2, 2, 1), // 1360
            new CubeConfig("C2C", 2, 1, 2), // 1126
            new CubeConfig("C3C", 2, 2, 2), // 1343
            new CubeConfig("C4C", 2, 3, 2)), // 1122
        new ClusterConfig("CS7", 24, 5, 1,
            new CubeConfig("A1A", 0, 0, 0), // 900
            new CubeConfig("A2A", 0, 1, 0), // 1046
            new CubeConfig("A3A", 0, 2, 0), // 1224
            new CubeConfig("A4A", 0, 3, 0), // 641
            new CubeConfig("A5A", 0, 4, 0), // 1196
            new CubeConfig("A6A", 0, 5, 0), // 1120
            new CubeConfig("B2A", 1, 1, 0), // 852
            new CubeConfig("B3A", 1, 2, 0), // 1214
            new CubeConfig("B2B", 1, 1, 1), // 953
            new CubeConfig("B3B", 1, 2, 1), // 140
            new CubeConfig("B4B", 1, 3, 1), // 609
            new CubeConfig("B5B", 1, 4, 1)), // 1249
        new ClusterConfig("CS8", 26, 4, 1,
            new CubeConfig("A3B", 0, 2, 1), // 518
            new CubeConfig("A4B", 0, 3, 1), // 647
            new CubeConfig("A5B", 0, 4, 1), // 1113
            new CubeConfig("B4B", 1, 3, 1), // 966
            new CubeConfig("B5B", 1, 4, 1), // 643
            new CubeConfig("B6B", 1, 5, 1), // 615
            new CubeConfig("C2A", 2, 1, 0), // 1209
            new CubeConfig("C3A", 2, 2, 0), // 1323
            new CubeConfig("C4A", 2, 3, 0), // 727
            new CubeConfig("C5A", 2, 4, 0), // 308
            new CubeConfig("C4B", 2, 3, 1), // 1192
            new CubeConfig("C5B", 2, 4, 1)), // 575
        new ClusterConfig("CS9", 29, 5, 1,
            new CubeConfig("A1A", 0, 0, 0), // 1104
            new CubeConfig("A2A", 0, 1, 0), // 713
            new CubeConfig("A3A", 0, 2, 0), // 759
            new CubeConfig("A4A", 0, 3, 0), // 603
            new CubeConfig("A5A", 0, 4, 0), // 761
            new CubeConfig("B2A", 1, 1, 0), // 441
            new CubeConfig("B3A", 1, 2, 0), // 442
            new CubeConfig("B2B", 1, 1, 1), // 455
            new CubeConfig("B3B", 1, 2, 1), // 691
            new CubeConfig("B4B", 1, 3, 1)), // 1306
        new ClusterConfig("CS10", 31, 5, 1,
            new CubeConfig("A3A", 0, 2, 0), // 1371
            new CubeConfig("A2B", 0, 1, 1), // 897
            new CubeConfig("A3B", 0, 2, 1), // 844
            new CubeConfig("A4B", 0, 3, 1), // 940
            new CubeConfig("A5B", 0, 4, 1), // 1216
            new CubeConfig("A6B", 0, 5, 1), // 778
            new CubeConfig("B1B", 1, 0, 1), // 873
            new CubeConfig("B2B", 1, 1, 1), // 827
            new CubeConfig("B3B", 1, 2, 1), // 143
            new CubeConfig("B4B", 1, 3, 1), // 1037
            new CubeConfig("C4A", 2, 3, 0), // 604
            new CubeConfig("C4B", 2, 3, 1), // 
            new CubeConfig("C5B", 2, 4, 1)), // 929
        new ClusterConfig("CS11", 34, 4, 1,
            new CubeConfig("A2A", 0, 1, 0), // 911
            new CubeConfig("A3A", 0, 2, 0), // 747
            new CubeConfig("A4A", 0, 3, 0), // 546
            new CubeConfig("A5A", 0, 4, 0), // 971
            new CubeConfig("A6A", 0, 5, 0), // 531
            new CubeConfig("B1A", 1, 0, 0), // 775
            new CubeConfig("B2A", 1, 1, 0), // 849
            new CubeConfig("B3A", 1, 2, 0), // 1100
            new CubeConfig("B4A", 1, 3, 0), // 1304
            new CubeConfig("B3B", 1, 2, 1), // 865
            new CubeConfig("B4B", 1, 3, 1), // 725
            new CubeConfig("B5B", 1, 4, 1), // 1017
            new CubeConfig("C2A", 2, 1, 0), // 415hp
            new CubeConfig("C3A", 2, 2, 0), // 723
            new CubeConfig("C3B", 2, 2, 1), // 451
            new CubeConfig("C4B", 2, 3, 1)), // 589
        new ClusterConfig("CS12", 37, 5, 1,
            new CubeConfig("A1", 0, 0, 0), // 1300
            new CubeConfig("A2", 0, 1, 0), // 527
            new CubeConfig("A3", 0, 2, 0), // 1302
            new CubeConfig("A4", 0, 3, 0), // 1053
            new CubeConfig("A5", 0, 4, 0), // 794
            new CubeConfig("B2", 1, 1, 0), // 968
            new CubeConfig("B3", 1, 2, 0)), // 549
        new ClusterConfig("RC1", 39.5, 5.5, 2,
            new CubeConfig("A1.5A", 0, 0.5, 0), // 199
            new CubeConfig("A1B", 0, 0, 1), // 1124
            new CubeConfig("A2B", 0, 1, 1), // 1118
            new CubeConfig("A1.5C", 0, 0.5, 2), // 614
            new CubeConfig("A2.5C", 0, 1.5, 2)), // 567
        new ClusterConfig("RC2", 39.5, 6, 5,
            new CubeConfig("A1.5A", 0, 0.5, 0), // 1051
            new CubeConfig("A2.5A", 0, 1.5, 0), // 446
            new CubeConfig("A1B", 0, 0, 1), // 1236
            new CubeConfig("A2B", 0, 1, 1), // 1242
            new CubeConfig("A3B", 0, 2, 1)), // 1309
        new ClusterConfig("R1", 40, 1, 8,
            new CubeConfig("A1", 0, 0, 0), // 733
            new CubeConfig("A2", 0, 1, 0), // 773
            new CubeConfig("A3", 0, 2, 0), // 498
            new CubeConfig("A4", 0, 3, 0)), // 977
        new ClusterConfig("R2", 41, 5, 7,
            new CubeConfig("A1", 0, 0, 0), // 962
            new CubeConfig("A2", 0, 1, 0), // 919
            new CubeConfig("A3", 0, 2, 0), // 637
            new CubeConfig("A4", 0, 3, 0)), // 957
        new ClusterConfig("R3", 41, 0, 7,
            new CubeConfig("A4A", 0, 3, 0), // 711
            new CubeConfig("A5A", 0, 4, 0), // 893
            new CubeConfig("A1B", 0, 0, 1), // 571
            new CubeConfig("A2B", 0, 1, 1), // 1067
            new CubeConfig("A3B", 0, 2, 1), // 583
            new CubeConfig("A4B", 0, 3, 1), // 1211
            new CubeConfig("A5B", 0, 4, 1)), // 928
        new ClusterConfig("R4", 42, 3, 7,
            new CubeConfig("A1", 0, 0, 0), // 649
            new CubeConfig("A2", 0, 1, 0), // 825
            new CubeConfig("A3", 0, 2, 0), // 1077
            new CubeConfig("A4", 0, 3, 0), // 797
            new CubeConfig("A5", 0, 4, 0), // 987
            new CubeConfig("A6", 0, 5, 0)), // 1032
        new ClusterConfig("R5", 43, 4, 7,
            new CubeConfig("A1", 0, 0, 0), // 654
            new CubeConfig("A2", 0, 1, 0), // 529
            new CubeConfig("A3", 0, 2, 0), // 771
            new CubeConfig("A4", 0, 3, 0), // 737
            new CubeConfig("A5", 0, 4, 0), // 859
            new CubeConfig("A6", 0, 5, 0)), // 657
        new ClusterConfig("R6", 44, 3.5, 7,
            new CubeConfig("A1", 0, 0, 0), // 809
            new CubeConfig("A2", 0, 1, 0)), // 1047
        new ClusterConfig("R7", 45, 4, 6,
            new CubeConfig("A1", 0, 0, 0), 
            new CubeConfig("A2", 0, 1, 0), // 1078
            new CubeConfig("A3", 0, 2, 0), // 745
            new CubeConfig("A4", 0, 3, 0), // 787
            new CubeConfig("A5", 0, 4, 0)), // 595
        new ClusterConfig("R8", 46, 5.5, 6,
            new CubeConfig("A1", 0, 0, 0), // 739 
            new CubeConfig("A2", 0, 1, 0)), // 905
    };

    static class ClusterConfig {
        final String id;
        final float x;
        final float y;
        final float z;
        final CubeConfig[] configs;

        // these are doubles so we can use numeric literals without the f
        ClusterConfig(String id, double x, double y, double z, CubeConfig... configs) {
            this.id = id;
            this.x = (float) x;
            this.y = (float) y;
            this.z = (float) z;
            this.configs = configs;
        }
    }

    static class CubeConfig {
        final String baseModelId;
        final float x;
        final float y;
        final float z;
        final float rx;
        final float ry;
        final float rz;

        CubeConfig(String baseModelId, double x, double y, double z) {
            this.baseModelId = baseModelId;
            this.x = (float) x;
            this.y = (float) y;
            this.z = (float) z;
            this.rx = 0;
            this.ry = 0;
            this.rz = 0;
        }

        CubeConfig(String baseModelId, double x, double y, double z, double rx, double ry, double rz) {
            this.baseModelId = baseModelId;
            this.x = (float) x;
            this.y = (float) y;
            this.z = (float) z;
            this.rx = (float) rx;
            this.ry = (float) ry;
            this.rz = (float) rz;
        }
    }

    public SLModel buildModel() {
        LXTransform globalTransform = new LXTransform();
        // we're abusing tower to mean cluster, oh well
        List<CubesModel.Tower> towers = new ArrayList<>();
        List<CubesModel.Cube> allCubes = new ArrayList<>();

        for (ClusterConfig cluster : clusters) {
            List<CubesModel.Cube> clusterCubes = new ArrayList<>();

            globalTransform.push();
            globalTransform.translate(SP * cluster.x, SP * cluster.y, SP * cluster.z);

            for (CubeConfig config : cluster.configs) {
                float x = SP * config.x;
                float y = SP * config.y;
                float z = SP * config.z;
                float rx = config.rx;
                float ry = config.ry;
                float rz = config.rz;
                CubesModel.DoubleControllerCube cube = new CubesModel.DoubleControllerCube(
                    cluster.id + "-" + config.baseModelId,
                    x, y, z, rx, ry, rz, globalTransform);
                clusterCubes.add(cube);
                allCubes.add(cube);
            }

            globalTransform.pop();
            towers.add(new CubesModel.Tower(cluster.id, clusterCubes));
        }

        CubesModel.Cube[] allCubesArr = new CubesModel.Cube[allCubes.size()];
        allCubes.toArray(allCubesArr);

        CubesModel model = new CubesModel(towers, allCubesArr, cubeInventory, mapping);
        model.setTopologyTolerances(6, 6, 6);
        return model;
    }


    @Override
    public void setupLx(LX lx) {
        super.setupLx(lx);
        FaderLimiter.attach(lx);
        /* TODO(jake): set actual brightness cap here! */
        outputScaler.setTargetLinearScale(1.0);
        outputScaler.setUseMonoGamma(false);
    }

    @Override
    public String getShowName() {
        return SHOW_NAME;
    }
}
