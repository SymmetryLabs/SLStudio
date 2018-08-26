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

    static final float SP = 1.35f;


    static final TowerConfig[] TOWER_CONFIG = {

        /**
         * A --------------------------------------------------------------------------------------------------*
        */
        // A1 - STAGE***
        new TowerConfig(CubesModel.Cube.Type.LARGE,   1.9f,    JUMP*2,    51.2f+7, -45, new String[] {"341", "188", "326"}),

        // A2 - STAGE***
        new TowerConfig(CubesModel.Cube.Type.LARGE,   6.9f-SP,    JUMP*2,    53.1f-SP+7, -45, new String[] {"362", "189", "50", "61"}),
        new TowerConfig(CubesModel.Cube.Type.LARGE,   6.9f,  JUMP*2+JUMP*3, 53.1f+7, -45, new String[] {"71"}),

        // A3 - STAGE***
        new TowerConfig(CubesModel.Cube.Type.LARGE,   9.4f,    JUMP*2,    51.6f+7, -45, new String[] {"121", "365", "397", "128"}), //81->128

        // A4 - STAGE***
        new TowerConfig(CubesModel.Cube.Type.LARGE,  14.0f-SP*0.5f,    JUMP*2,     52.8f-SP+7, -45, new String[] {"21", "419"}),
        new TowerConfig(CubesModel.Cube.Type.LARGE,  14.0f+SP*0.5f,   JUMP*2,  52.8f+7, -45, new String[] {"38", "367", "55"}),

        // A5 - STAGE***
        new TowerConfig(CubesModel.Cube.Type.HD,     15.5f,          0,    47.0f+7, 45, new String[] {"1118", "1119", "1228", "1229"}),
        new TowerConfig(CubesModel.Cube.Type.HD,     15.5f+SP,  JUMP*1,    47.0f+SP+7, 45, new String[] {"1226", "1227"}),

        // A6 - STAGE***
        new TowerConfig(CubesModel.Cube.Type.HD,     15.6f,         0, 42.9f+7, 45, new String[] {"1086", "1088", "1051", "1058"}), // 1086???
        new TowerConfig(CubesModel.Cube.Type.HD,     15.6f+SP, JUMP*1, 42.9f+SP+7, 45, new String[] {"1069", "1068"}),

        // A7 - STAGE***
        new TowerConfig(CubesModel.Cube.Type.HD,     14.6f,         0, 38.2f+7, 45, new String[] {"1056", "1085", "1081", "1101"}),

        // A8***
        new TowerConfig(CubesModel.Cube.Type.HD,     16.0f,         0, 31.6f, 45, new String[] {"1130", "1131", "1132", "1133"}),

        // A9***
        new TowerConfig(CubesModel.Cube.Type.HD,      9.8f,         0, 31.3f, 45, new String[] {"1100", "1102", "1055", "1054", "900", "901"}),

        // A10***
        new TowerConfig(CubesModel.Cube.Type.HD,      6.7f,         0,  23.5f, 45, new String[] {"37", "1050", "1191", "1190"}),

        // A11***
        new TowerConfig(CubesModel.Cube.Type.HD,     13.1f,         0,  22.8f, 45, new String[] {"1113", "1114"}),

        /**
         * B --------------------------------------------------------------------------------------------------*
        */
        // B1***
        new TowerConfig(CubesModel.Cube.Type.HD,      9.3f,      0,    19.3f, 45, new String[] {"1124", "1125", "1196", "1197", "1193", "1192"}),
        new TowerConfig(CubesModel.Cube.Type.HD,      9.3f-SP,   0,    19.3f-SP, 45, new String[] {"1195", "1194"}),

        // B2***
        new TowerConfig(CubesModel.Cube.Type.HD,     10.9f,      0,    13.4f, 45, new String[] {"1128", "1129"}),

        // B3***
        new TowerConfig(CubesModel.Cube.Type.HD,      8.0f,      0,    8.7f, 45, new String[] {"143", "150"}),
        new TowerConfig(CubesModel.Cube.Type.HD,      8.0f+SP,      0, 8.7f-SP, 45, new String[] {"521", "331"}),

        // B4***
        new TowerConfig(CubesModel.Cube.Type.HD,      4.0f,      0,    6.6f,  0, new String[] {"1246", "1245"}),

        // B5***
        new TowerConfig(CubesModel.Cube.Type.HD,      3.7f,  JUMP*1.5f, 3.4f,  0, new String[] {"1241", "1240"}),

        // B6???
        new TowerConfig(CubesModel.Cube.Type.HD,      6.4f,      0,    2.2f, 45, new String[] {"?", "?", "?", "?"}),
        new TowerConfig(CubesModel.Cube.Type.HD,      6.4f+SP, JUMP*0.5f,    2.2f+SP, 45, new String[] {"?", "?"}),

        // B7*** just double check
        new TowerConfig(CubesModel.Cube.Type.HD,      9.8f,      0,    0.0f, 45, new String[] {"575", "1189"}),
        new TowerConfig(CubesModel.Cube.Type.HD,      9.8f-SP*0.5f,   JUMP,    0.0f-SP*0.5f, 45, new String[] {"1231", "594"}),

        // B8***
        new TowerConfig(CubesModel.Cube.Type.HD,      7.2f,      0,   -4.8f, 45, new String[] {"1244", "1243", "204", "1084"}),

        // B9***
        new TowerConfig(CubesModel.Cube.Type.HD,      3.6f,  JUMP*1.5f, -7.2f,  0, new String[] {"1236", "1235"}),

        // B10***
        new TowerConfig(CubesModel.Cube.Type.HD,     8.0f,      0,   10.3f, 45, new String[] {"1126", "1127"}),
        new TowerConfig(CubesModel.Cube.Type.HD,  8.0f-SP,      0,   -10.3f+SP, 45, new String[] {"1250", "501"}),
        new TowerConfig(CubesModel.Cube.Type.HD,  8.0f-SP*0.5f,   JUMP,   -10.3f+SP*0.5f, 45, new String[] {"312", "135"}),

        // B11***
        new TowerConfig(CubesModel.Cube.Type.HD,     4.0f,      0,   -12.7f, 0, new String[] {"1234", "1233"}),

        /**
         * C --------------------------------------------------------------------------------------------------*
        */
        // C1***
        new TowerConfig(CubesModel.Cube.Type.HD,    -7.8f,          0,    17.5f, 45, new String[] {"1212", "1222"}),

        // C2***
        new TowerConfig(CubesModel.Cube.Type.HD,   -10.5f,          0,    11.6f, 45, new String[] {"308", "159"}),

        // C3??? need to get ids for half-off cube
        new TowerConfig(CubesModel.Cube.Type.HD,    -8.8f,          0,    5.7f, 45, new String[] {"1218", "1219"}),
        new TowerConfig(CubesModel.Cube.Type.HD,    -8.8f+SP,       0,    5.7f-SP, 45, new String[] {"1206", "1207"}),
        //new TowerConfig(CubesModel.Cube.Type.HD,    -8.8f+SP*1.5f,   JUMP,    5.7f-SP*0.5f, 45, new String[] {"?", "?"}),

        // C4***
        new TowerConfig(CubesModel.Cube.Type.HD,    -3.8f,             0,    6.8f, 90, new String[] {"361", "1007"}), // 360 or 361?

        // C5***
        new TowerConfig(CubesModel.Cube.Type.HD,    -3.5f,      JUMP*1.3f,    2.9f,  90, new String[] {"1224", "1225"}),

        // C6***
        new TowerConfig(CubesModel.Cube.Type.HD,    -6.7f,          0,    -0.8f, 45, new String[] {"1117", "1005", "1200", "1201"}),
        new TowerConfig(CubesModel.Cube.Type.HD,    -6.7f+SP*0.4f, JUMP,    -0.8f+SP*1.5f, 45, new String[] {"1242", "1239"}), // CHECK THESE

        // C7***
        new TowerConfig(CubesModel.Cube.Type.HD,    -7.0f,          0,   -4.7f, 45, new String[] {"1120", "1121", "1214", "1215"}),
        new TowerConfig(CubesModel.Cube.Type.HD,    -7.0f-SP,       0,  -4.7f-SP, 45, new String[] {"199", "889"}),

        // C8***
        new TowerConfig(CubesModel.Cube.Type.HD,    -3.6f,      JUMP*1.3f,   -9.0f,  90, new String[] {"797", "1230"}),

        // C9***
        new TowerConfig(CubesModel.Cube.Type.HD,    -8.2f,          0,   -9.9f, 45, new String[] {"1238", "1237"}),
        new TowerConfig(CubesModel.Cube.Type.HD,    -8.2f+SP,       0,   -9.9f+SP, 45, new String[] {"1248", "1247", "1250", "?"}),

        // C10??? need ids
        new TowerConfig(CubesModel.Cube.Type.HD,    -3.8f,          0,   -12.7f, 90, new String[] {"1221", "1220"}),

        /**
         * D --------------------------------------------------------------------------------------------------*
        */
        // D1 - STAGE***
        new TowerConfig(CubesModel.Cube.Type.LARGE, -1.6f,       JUMP*2,    51.5f+7, -45, new String[] {"186", "36", "5", "122"}),

        // D2 - STAGE***
        new TowerConfig(CubesModel.Cube.Type.LARGE, -5.8f,       JUMP*2,    52.1f+7, -45, new String[] {"79", "320", "185"}),
        new TowerConfig(CubesModel.Cube.Type.LARGE, -5.8f+SP,    JUMP*2,    52.1f+SP+7, -45, new String[] {"321", "138"}),

        // D3 - STAGE***
        new TowerConfig(CubesModel.Cube.Type.LARGE, -9.2f,       JUMP*2,    52.3f+7, -45, new String[] {"372", "87", "181", "126"}),

        // D4 - STAGE***
        new TowerConfig(CubesModel.Cube.Type.LARGE, -12.5f,      JUMP*2,   52.0f+7, -45, new String[] {"120", "14", "351"}),

        // D5 - STAGE***
        new TowerConfig(CubesModel.Cube.Type.LARGE, -16.2f,      JUMP*2,    52.4f+7, -45, new String[] {"69", "184", "211"}),
        new TowerConfig(CubesModel.Cube.Type.LARGE, -16.2f+SP,  JUMP*2+JUMP*2,    52.4f+SP+7, -45, new String[] {"203"}),

        // D6 - STAGE***
        new TowerConfig(CubesModel.Cube.Type.HD,    -17.9f,          0,    47.4f+7, 45, new String[] {"1002", "683", "1209", "1208"}),

        // D7 - STAGE***
        new TowerConfig(CubesModel.Cube.Type.HD,    -18.6f,          0,    42.5f+7, 45, new String[] {"1211", "1210", "895", "983"}),
        new TowerConfig(CubesModel.Cube.Type.HD,    -18.6f+SP,  JUMP*1,    42.5f+SP+7, 45, new String[] {"572", "806"}),

        // D8 - STAGE***
        new TowerConfig(CubesModel.Cube.Type.HD,    -18.6f,          0,    38.0f+7, 45, new String[] {"1216", "1217", "1053", "1062"}),
        new TowerConfig(CubesModel.Cube.Type.HD,    -18.6f+SP,       0, 38.0f+SP+7, 45, new String[] {"990", "822"}),

        // D9***
        new TowerConfig(CubesModel.Cube.Type.HD,    -17.0f,          0,    31.8f, 45, new String[] {"447", "793", "1203", "1202"}),

        // D10***
        new TowerConfig(CubesModel.Cube.Type.HD,    -11.9f,          0,    32.7f, 45, new String[] {"614", "636", "1205", "1204", "438", "459"}),
        new TowerConfig(CubesModel.Cube.Type.HD,    -11.9f+SP,       0, 32.7f-SP, 45, new String[] {"1014", "891"}),

        // D11***
        new TowerConfig(CubesModel.Cube.Type.HD,    -10.0f,          0,     26.3f, 45, new String[] {"966", "509"}),

        // D12???
        new TowerConfig(CubesModel.Cube.Type.HD,    -12.9f,          0,     22.8f, 45, new String[] {"1135", "1232"}),
        new TowerConfig(CubesModel.Cube.Type.HD,    -12.9f+SP,          0,     22.8f-SP, 45, new String[] {"140", "1134"}),

        /**
         * U --------------------------------------------------------------------------------------------------*
        */
        // U1 ??? these right?
        new TowerConfig(CubesModel.Cube.Type.HD,    -12.9f,          0,     -22.8f, 45, new String[] {"165", "196"}),
        new TowerConfig(CubesModel.Cube.Type.HD,    -12.9f-SP,          5,     -22.8f+SP, 45, new String[] {"1049", "1105"}),

        // U2 ???? these right?
        new TowerConfig(CubesModel.Cube.Type.HD,    12.9f,          0,      -22.8f, 45, new String[] {"1019", "908"}),
        new TowerConfig(CubesModel.Cube.Type.HD,    12.9f+SP,          5,      -22.8f+SP, 45, new String[] {"590", "640"}),
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
            float x = config.x*12f;
            float z = config.z*12f;
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
