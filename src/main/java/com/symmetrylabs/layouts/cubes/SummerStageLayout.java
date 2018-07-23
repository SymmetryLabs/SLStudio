package com.symmetrylabs.layouts.cubes;

//package com.symmetrylabs.layouts.cubes;

import java.util.*;
import java.lang.ref.WeakReference;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.Float;


import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.output.SLController;
import com.symmetrylabs.util.CubePhysicalIdMap;
import com.symmetrylabs.util.listenable.SetListener;
import heronarts.lx.LX;
import heronarts.lx.output.FadecandyOutput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transform.LXTransform;
import heronarts.p3lx.ui.UI2dScrollContext;

import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.listenable.ListenableSet;
import com.symmetrylabs.util.Utils;
import static com.symmetrylabs.util.DistanceUtils.*;


/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class SummerStageLayout extends CubesLayout {

    static final float globalOffsetX = 1368;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 5;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;

    static final float CUBE_WIDTH = 24;
    static final float CUBE_HEIGHT = 24;
    static final float TOWER_WIDTH = 24;
    static final float TOWER_HEIGHT = 24;
    static final float CUBE_SPACING = 2.5f;

    static final float TOWER_VERTICAL_SPACING = 2.5f;
    static final float TOWER_RISER = 14;
    static final float SP = 24+2;
    static final float JUMP = TOWER_HEIGHT+TOWER_VERTICAL_SPACING;

    static final float INCHES_PER_METER = 39.3701f;

    static final TowerConfig[] TOWER_CONFIG = {
//        new TowerConfig(SP*0.f, 0, -SP*0.f, new String[] {"0", "0", "0", "0", "0"}),

    };

    static class TowerConfig {

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

    public SLModel buildModel() {
        // Any global transforms
        LXTransform globalTransform = new LXTransform();
        globalTransform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
        globalTransform.rotateX(globalRotationX * Math.PI / 180.);
        globalTransform.rotateY(globalRotationY * Math.PI / 180.);
        globalTransform.rotateZ(globalRotationZ * Math.PI / 180.);

        List<CubesModel.Tower> towers = new ArrayList<>();
        List<CubesModel.Cube> allCubes = new ArrayList<>();

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                Utils.createInput("data/SummerStageCoordinates.txt")));

            List<CubesModel.Cube> cubes = new ArrayList<>();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.replaceAll(" ","").replaceAll("\"","");
                String[] vals = line.split(",");

                float x = metersToInches(Float.parseFloat(vals[0]));
                float y = metersToInches(Float.parseFloat(vals[2]));
                float z = metersToInches(Float.parseFloat(vals[1]));

                CubesModel.Cube cube = new CubesModel.Cube("0", x, y, z, 0, 0, 0, globalTransform, CubesModel.Cube.Type.HD);
                cubes.add(cube);
                allCubes.add(cube);
            }
            towers.add(new CubesModel.Tower("", cubes));

        } catch (IOException e) {
            e.printStackTrace();
        }

        List<CubesModel.Cube> additionalCubes = new ArrayList<>();

        // controller order: (back-left), (front-right)
//        CubesModel.Cube cube1 = new CubesModel.DoubleControllerCube("5410ecf58ce2", "5410ecf5f105", 0, 0, 0, 0, 0, 0, globalTransform);
//        additionalCubes.add(cube1);
//        allCubes.add(cube1);
//
//        CubesModel.Cube cube2 = new CubesModel.DoubleControllerCube("0", "0", 50, 0, 0, 0, 0, 0, globalTransform);
//        additionalCubes.add(cube2);
//        allCubes.add(cube2);
//
//        CubesModel.Cube cube3 = new CubesModel.DoubleControllerCube("0", "0", 0, 50, 0, 0, 0, 0, globalTransform);
//        additionalCubes.add(cube3);
//        allCubes.add(cube3);
//
//        CubesModel.Cube cube4 = new CubesModel.DoubleControllerCube("0", "0", 50, 50, 0, 0, 0, 0, globalTransform);
//        additionalCubes.add(cube4);
//        allCubes.add(cube4);

        towers.add(new CubesModel.Tower("", additionalCubes));

        CubesModel.Cube[] allCubesArr = new CubesModel.Cube[allCubes.size()];
        for (int i = 0; i < allCubesArr.length; i++) {
            allCubesArr[i] = allCubes.get(i);
        }

        return new CubesModel(towers, allCubesArr);
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        UI2dScrollContext utility = ui.rightPane.utility;
        new UIOutputs(lx, ui, this, 0, 0, utility.getContentWidth()).addToContainer(utility);
        new UIMappingPanel(lx, ui, 0, 0, utility.getContentWidth()).addToContainer(utility);
    }
}