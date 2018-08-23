package com.symmetrylabs.shows.usopen;

import java.util.*;
import java.lang.ref.WeakReference;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.Float;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.output.SLController;
import com.symmetrylabs.util.CubePhysicalIdMap;
import com.symmetrylabs.util.listenable.SetListener;
import heronarts.lx.LX;
import heronarts.lx.output.FadecandyOutput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transform.LXTransform;
import heronarts.p3lx.ui.UI2dScrollContext;

import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.listenable.ListenableSet;
import com.symmetrylabs.util.Utils;
import static com.symmetrylabs.util.DistanceUtils.*;
import static com.symmetrylabs.util.DistanceConstants.*;

/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class USOpenShow extends CubesShow {

    static final float globalOffsetX = 220;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 103;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;

    static final float SP = 0.3f; //inchesToMeters(25);


    static final TowerConfig[] TOWER_CONFIG = {

        /**
         * A --------------------------------------------------------------------------------------------------*
        */
        // A1
        new TowerConfig(CubesModel.Cube.Type.LARGE,  0.45f,   JUMP*2, 16.91f, -45, new String[] {"341", "188", "326"}),

        // A2
        new TowerConfig(CubesModel.Cube.Type.LARGE,  2.00f,   JUMP*2, 17.48f, -45, new String[] {"362", "189", "50", "61"}),
        new TowerConfig(CubesModel.Cube.Type.LARGE,  2.00f+SP, JUMP*5, 17.48f, -45, new String[] {"?", "?"}),

        // A3
        new TowerConfig(CubesModel.Cube.Type.LARGE,  2.77f,   JUMP*2, 17.07f, -45, new String[] {"121", "365", "397", "81"}),

        // A4
        new TowerConfig(CubesModel.Cube.Type.LARGE,  4.17f,   JUMP*2, 17.47f, -45, new String[] {"21", "419"}),
        new TowerConfig(CubesModel.Cube.Type.LARGE,  4.17f+SP,JUMP*2, 17.47f, -45, new String[] {"367", "25"}),

        // A5
        new TowerConfig(CubesModel.Cube.Type.HD,     4.64f,         0, 16.30f, 45, new String[] {"1118", "1119", "1228", "1229"}),
        new TowerConfig(CubesModel.Cube.Type.HD,     4.64f+SP,      0, 16.30f, 45, new String[] {"1226", "1227"}),

        // A6
        new TowerConfig(CubesModel.Cube.Type.HD,     4.65f,         0, 15.14f, 45, new String[] {"1086", "?", "1051", "?"}),
        new TowerConfig(CubesModel.Cube.Type.HD,     4.65f+SP, JUMP*1, 15.14f, 45, new String[] {"1069", "1068"}),

        // A7
        new TowerConfig(CubesModel.Cube.Type.HD,     4.31f,         0, 13.78f, 45, new String[] {"1056", "1085", "1081", "1101"}),

        // A8
        new TowerConfig(CubesModel.Cube.Type.HD,     4.63f,         0, 11.00f, 45, new String[] {"1130", "1131", "1132", "1133"}),

        // A9
        new TowerConfig(CubesModel.Cube.Type.HD,     3.17f,         0, 10.91f, 45, new String[] {"1100", "1102", "1055", "1054", "900", "901"}),

        // A10
        new TowerConfig(CubesModel.Cube.Type.HD,     1.94f,         0,  8.52f, 45, new String[] {"1189", "1050", "1191", "1190"}),

        // A11
        new TowerConfig(CubesModel.Cube.Type.HD,     3.89f,         0,  8.32f, 45, new String[] {"1113", "1114"}),

        /**
         * B --------------------------------------------------------------------------------------------------*
        */
        // B1
        new TowerConfig(CubesModel.Cube.Type.HD,     2.76f,      0,    7.25f, 45, new String[] {"1124", "1125", "1196", "1197", "1193", "1192"}),
        new TowerConfig(CubesModel.Cube.Type.HD,     2.76f-SP,   0,    7.25f, 45, new String[] {"1195", "1194"}),

        // B2
        new TowerConfig(CubesModel.Cube.Type.HD,     3.58f,      0,    5.46f, 45, new String[] {"1128", "1129"}),

        // B3
        new TowerConfig(CubesModel.Cube.Type.HD,     2.43f,      0,    4.05f, 45, new String[] {"143", "150"}),
        new TowerConfig(CubesModel.Cube.Type.HD,     2.43f,      0, 4.05f-SP, 45, new String[] {"521", "1129"}),

        // B4
        new TowerConfig(CubesModel.Cube.Type.HD,     1.09f,      0,    3.41f, 45, new String[] {"1246", "1245"}),

        // B5
        new TowerConfig(CubesModel.Cube.Type.HD,     1.03f,  JUMP*1.3f,    2.44f,  0, new String[] {"0", "0"}),

        // B6
        new TowerConfig(CubesModel.Cube.Type.HD,     2.36f,      0,    2.22f, 45, new String[] {"0", "0"}),

        // B7
        new TowerConfig(CubesModel.Cube.Type.HD,     2.88f,      0,    0.96f, 45, new String[] {"575", "1189", "1231", "?"}),

        // B8
        new TowerConfig(CubesModel.Cube.Type.HD,     1.78f,      0,   -0.11f, 45, new String[] {"1244", "1243", "1242", "1239"}),

        // B9
        new TowerConfig(CubesModel.Cube.Type.HD,     1.04f,  JUMP*1.3f,   -1.40f,  0, new String[] {"1236", "1235"}),

        // B10
        new TowerConfig(CubesModel.Cube.Type.HD,     2.74f,      0,   -1.35f, 45, new String[] {"1238", "1237"}),
        new TowerConfig(CubesModel.Cube.Type.HD,  2.74f+SP,      0,   -1.35f, 45, new String[] {"1248", "1247"}),

        // B11
        new TowerConfig(CubesModel.Cube.Type.HD,     1.14f,      0,   -2.51f, 45, new String[] {"0", "0"}),

        /**
         * C --------------------------------------------------------------------------------------------------*
        */
        // C1
        new TowerConfig(CubesModel.Cube.Type.HD,    -2.44f,          0,    6.66f, 45, new String[] {"1212", "1222"}),

        // C2
        new TowerConfig(CubesModel.Cube.Type.HD,    -3.89f,          0,    5.25f, 45, new String[] {"?", "?"}),

        // C3
        new TowerConfig(CubesModel.Cube.Type.HD,    -3.48f,          0,    3.97f, 45, new String[] {"1218", "1219"}),

        // C4
        new TowerConfig(CubesModel.Cube.Type.HD,    -0.74f,          0,    3.42f, 45, new String[] {"360", "1007"}),

        // C5
        new TowerConfig(CubesModel.Cube.Type.HD,    -0.65f,      JUMP*1.3f,    2.44f,  0, new String[] {"1224", "1225"}),

        // C6
        new TowerConfig(CubesModel.Cube.Type.HD,    -2.75f,          0,    1.59f, 45, new String[] {"1117", "1005", "120", "1201", "1122", "1123"}),

        // C7
        new TowerConfig(CubesModel.Cube.Type.HD,    -2.20f,          0,   -0.04f, 45, new String[] {"1120", "1121", "1214", "1215", "1198", "1199"}),
        new TowerConfig(CubesModel.Cube.Type.HD,    -2.20f,          0,-0.04f+SP, 45, new String[] {"199", "889"}),

        // C8
        new TowerConfig(CubesModel.Cube.Type.HD,    -0.67f,      JUMP*1.3f,   -1.39f,  0, new String[] {"797", "1230"}),

        // C9
        new TowerConfig(CubesModel.Cube.Type.HD,    -3.12f,          0,   -1.44f, 45, new String[] {"1126", "1127"}),
        //new TowerConfig(CubesModel.Cube.Type.HD,    -3.12f,          0    -1.44f+SP, 45, new String[] {"1134", "1135"}), // weird position thing

        // C10
        new TowerConfig(CubesModel.Cube.Type.HD,    -0.72f,          0,   -2.52f, 45, new String[] {"0", "0"}),

        /**
         * D --------------------------------------------------------------------------------------------------*
        */
        // D1
        new TowerConfig(CubesModel.Cube.Type.LARGE, -0.57f,     JUMP*2,    16.96f, -45, new String[] {"186", "36", "5", "122"}),

        // D2
        new TowerConfig(CubesModel.Cube.Type.LARGE, -1.50f,     JUMP*2,    17.48f, -45, new String[] {"79", "320", "185"}),
        new TowerConfig(CubesModel.Cube.Type.LARGE, -1.50f+SP,  JUMP*2,    17.48f, -45, new String[] {"321", "138"}),

        // D3
        new TowerConfig(CubesModel.Cube.Type.LARGE, -2.86f,     JUMP*2,    17.20f, -45, new String[] {"372", "87", "181", "126"}),

        // D4
        new TowerConfig(CubesModel.Cube.Type.LARGE, -3.77f,     JUMP*2,    17.11f, -45, new String[] {"120", "14", "351"}),

        // D5
        new TowerConfig(CubesModel.Cube.Type.LARGE, -4.74f,     JUMP*2,    17.52f, -45, new String[] {"69", "184", "211"}),
        new TowerConfig(CubesModel.Cube.Type.LARGE, -4.74f+SP,  JUMP*4,    17.52f, -45, new String[] {"203"}),

        // D6
        new TowerConfig(CubesModel.Cube.Type.HD,    -5.49f,          0,    16.00f, 45, new String[] {"1002", "683", "1205", "1208"}),

        // D7
        new TowerConfig(CubesModel.Cube.Type.HD,    -5.32f,          0,    15.15f, 45, new String[] {"1211", "1210", "895", "983"}),
        new TowerConfig(CubesModel.Cube.Type.HD,    -5.32f+SP,  JUMP*1,    15.15f, 45, new String[] {"572", "806"}),

        // D8
        new TowerConfig(CubesModel.Cube.Type.HD,    -5.31f,          0,    14.00f, 45, new String[] {"1216", "1217", "1053", "1062"}),
        new TowerConfig(CubesModel.Cube.Type.HD,    -5.31f,          0, 14.00f-SP, 45, new String[] {"990", "822"}),

        // D9
        new TowerConfig(CubesModel.Cube.Type.HD,    -5.28f,          0,    11.00f, 45, new String[] {"447", "793", "1205", "1202"}),

        // D10
        new TowerConfig(CubesModel.Cube.Type.HD,    -3.69f,          0,    11.27f, 45, new String[] {"614", "636", "1205", "1204", "438", "459"}),
        new TowerConfig(CubesModel.Cube.Type.HD,    -3.69f,          0, 11.27f-SP, 45, new String[] {"1014", "891"}),

        // D11
        new TowerConfig(CubesModel.Cube.Type.HD,    -3.15f,          0,     9.40f, 45, new String[] {"966", "509"}),

        // D12
        new TowerConfig(CubesModel.Cube.Type.HD,    -4.02f,          0,     8.33f, 45, new String[] {"415HP", "1232"}),
    };

    public SLModel buildModel() {
        // Any global transforms
        LXTransform transform = new LXTransform();
        transform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
        transform.rotateX(globalRotationX * Math.PI / 180.);
        transform.rotateY(globalRotationY * Math.PI / 180.);
        transform.rotateZ(globalRotationZ * Math.PI / 180.);

        List<CubesModel.Tower> towers = new ArrayList<>();
        List<CubesModel.Cube> allCubes = new ArrayList<>();

        for (TowerConfig config : TOWER_CONFIG) {
            List<CubesModel.Cube> cubes = new ArrayList<>();
            float x = metersToInches(config.x);
            float z = metersToInches(config.z);
            float yRot = config.yRot;

            if (config.type != CubesModel.Cube.Type.HD) {
                for (int i = 0; i < config.yValues.length; i++) {
                    float y = config.yValues[i];
                    CubesModel.Cube cube = new CubesModel.Cube(config.ids[i], x, y, z, 0, yRot, 0, transform, config.type);
                    cubes.add(cube);
                    allCubes.add(cube);
                }
            } else {
                for (int i = 1; i < config.yValues.length+1; i++) {
                    float y = config.yValues[i-1];
                    CubesModel.Cube cube = new CubesModel.DoubleControllerCube(config.ids[i*2-2], config.ids[i*2-1],
                        x, y, z, 0, yRot, 0, transform);
                    cubes.add(cube);
                    allCubes.add(cube);
                }
            }
            towers.add(new CubesModel.Tower("", cubes));
        }

        CubesModel.Cube[] allCubesArr = new CubesModel.Cube[allCubes.size()];
        for (int i = 0; i < allCubesArr.length; i++) {
            allCubesArr[i] = allCubes.get(i);
        }

        return new CubesModel(towers, allCubesArr);
    }
}
