package com.symmetrylabs.layouts.cubes;

//package com.symmetrylabs.layouts.cubes;

import java.util.*;
import java.lang.ref.WeakReference;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.output.SLController;
import com.symmetrylabs.util.CubePhysicalIdMap;
import com.symmetrylabs.util.listenable.SetListener;
import heronarts.lx.LX;
import heronarts.lx.output.FadecandyOutput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.listenable.ListenableSet;
import heronarts.p3lx.ui.UI2dScrollContext;

/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class AstraLayout extends CubesLayout implements Layout {

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;


    static final float trussOffsetX = 0;
    static final float trussOffsetY = JUMP*3;
    static final float trussOffsetZ = 0;

    static final float trussRotationX = 0;
    static final float trussRotationY = -45;
    static final float trussRotationZ = 0;

    static final float CUBE_SPACING = 1;

    static final TowerConfig[] TRUSS_CONFIG = {
        new TowerConfig(0, 0, 0, new String[] {"71", "61", "1105"}),
        new TowerConfig(SP*1.0f, TOWER_RISER, -SP*0.5f, new String[] { "412", "352", "159"}),
        new TowerConfig(SP*1.5f, JUMP*2, -SP*1.5f, new String[] { "135", "171"}),
        new TowerConfig(SP*2.5f, JUMP+TOWER_RISER, -SP*2.0f, new String[] { "1049", "50"}),
        new TowerConfig(SP*3.0f, JUMP, -SP*3.0f, new String[] { "196", "85"}),
        new TowerConfig(SP*4.0f, TOWER_RISER, -SP*3.5f, new String[] { "81", "55", "38"}),
        new TowerConfig(SP*4.5f, 0, -SP*4.5f, new String[] { "16", "318", "36"}),
    };

    static final TowerConfig[] NORMAL_CONFIG = {
        // left tower of 5
        new TowerConfig(-50, 0, -40, 0, -45, 0, new String[] {"d880399acc51", "133", "398", "353", "134"}),
        new TowerConfig(0, 0, -40, 0, -90, 0, new String[] {"17", "128"}),

        new TowerConfig(150, 0, -40, 0, 0, 0, new String[] {"177", "5"}),
        new TowerConfig(190, 0, -40, 0, -45, 0, new String[] {"165", "186", "312", "189", "122"}),



        // left leaning tower
        new TowerConfig(20, 40, 20, 0, -45, 0, new String[] {"308", "362"}),

        // single leaning cube
        new TowerConfig(40, 105, 20, 0, -45, -20, new String[] {"27"}),

        // right leaning tower
        new TowerConfig(132, 40, 15, 0, -45, 0, new String[] {"397", "361"}),


        // Tunnel
        // -- right close
        new TowerConfig(115, 50, 60, 180, -45-180, 0, new String[] {"33"}),

        // -- right medium
        new TowerConfig(CubesModel.Cube.Type.MEDIUM, 85, 90, 100, 235-180, 8-180, 0, new String[] {"210"}),

        // -- right small
        new TowerConfig(CubesModel.Cube.Type.SMALL, 135, 50, 200, 0, -45, 0, new String[] {"345"}),

        // -- left first medium
        new TowerConfig(CubesModel.Cube.Type.MEDIUM, 30, 90, 80, 45, 0, 0, new String[] {"393"}),

        // -- left second medium
        new TowerConfig(CubesModel.Cube.Type.MEDIUM, 50, 90, 150, 0, -45, 0, new String[] {"334"}),

        // -- left small
        new TowerConfig(CubesModel.Cube.Type.SMALL, 15, 40, 200, 0, -45, 0, new String[] {"173"}),
    };

    public SLModel buildModel() {
        // Any global transforms
        LXTransform globalTransform = new LXTransform();
        globalTransform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
        globalTransform.rotateX(globalRotationX * Math.PI / 180.);
        globalTransform.rotateY(globalRotationY * Math.PI / 180.);
        globalTransform.rotateZ(globalRotationZ * Math.PI / 180.);

        /* Cubes ----------------------------------------------------------*/
        List<CubesModel.Tower> towers = new ArrayList<>();
        List<CubesModel.Cube> allCubes = new ArrayList<>();

        int stripId = 0;

        globalTransform.push();
        globalTransform.translate(trussOffsetX, trussOffsetY, trussOffsetZ);
        globalTransform.rotateX(trussRotationX * Math.PI / 180.);
        globalTransform.rotateY(trussRotationY * Math.PI / 180.);
        globalTransform.rotateZ(trussRotationZ * Math.PI / 180.);
        for (TowerConfig config : TRUSS_CONFIG) {
            List<CubesModel.Cube> cubes = new ArrayList<>();
            float x = config.x;
            float z = config.z;
            float xRot = config.xRot;
            float yRot = config.yRot;
            float zRot = config.zRot;
            CubesModel.Cube.Type type = config.type;

            for (int i = 0; i < config.ids.length; i++) {
                float y = config.yValues[i];
                CubesModel.Cube cube = new CubesModel.Cube(config.ids[i], x, y, z, xRot, yRot, zRot, globalTransform, type);
                cubes.add(cube);
                allCubes.add(cube);
            }
            towers.add(new CubesModel.Tower("", cubes));
        }
        globalTransform.pop();
        /*-----------------------------------------------------------------*/

        for (TowerConfig config : NORMAL_CONFIG) {
            List<CubesModel.Cube> cubes = new ArrayList<>();
            float x = config.x;
            float z = config.z;
            float xRot = config.xRot;
            float yRot = config.yRot;
            float zRot = config.zRot;
            CubesModel.Cube.Type type = config.type;

            for (int i = 0; i < config.ids.length; i++) {
                float y = config.yValues[i];
                CubesModel.Cube cube = new CubesModel.Cube(config.ids[i], x, y, z, xRot, yRot, zRot, globalTransform, type);
                cubes.add(cube);
                allCubes.add(cube);
            }
            towers.add(new CubesModel.Tower("", cubes));
        }
        /*-----------------------------------------------------------------*/

        CubesModel.Cube[] allCubesArr = new CubesModel.Cube[allCubes.size()];
        for (int i = 0; i < allCubesArr.length; i++) {
            allCubesArr[i] = allCubes.get(i);
        }

        return new CubesModel(towers, allCubesArr);
    }
}
