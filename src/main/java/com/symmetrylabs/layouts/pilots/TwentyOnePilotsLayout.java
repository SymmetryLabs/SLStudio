package com.symmetrylabs.layouts.pilots;

import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.layouts.cubes.CubesController;
import com.symmetrylabs.layouts.cubes.CubesModel;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.util.CubePhysicalIdMap;
import com.symmetrylabs.util.listenable.ListenableSet;
import heronarts.lx.transform.LXTransform;

import java.util.ArrayList;
import java.util.List;


/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class TwentyOnePilotsLayout implements Layout {
    ListenableSet<CubesController> controllers = new ListenableSet<>();
    CubePhysicalIdMap cubePhysicalIdMap = new CubePhysicalIdMap();

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 168;

    static final float globalRotationX = 0;
    static final float globalRotationY = 90;
    static final float globalRotationZ = 0;

    static final float CUBE_WIDTH = 24;
    static final float CUBE_HEIGHT = 24;
    static final float TOWER_WIDTH = 24;
    static final float TOWER_HEIGHT = 24;
    static final float CUBE_SPACING = -1.5f;

    static final float TOWER_VERTICAL_SPACING = 2.5f;
    static final float TOWER_RISER = 14;
    static final float SP = 24;
    static final float JUMP = TOWER_HEIGHT + TOWER_VERTICAL_SPACING;

    static final float INCHES_PER_METER = 39.3701f;

    static final TowerConfig[] TOWER_CONFIG = {
        // row 1 (back)
        new TowerConfig(SP * 2, 0, SP * 0, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 2, 0, SP * 1, new String[]{"0", "0", "0", "0"}),
        // gap
        new TowerConfig(SP * 2, 0, SP * 4, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 2, 0, SP * 5, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 2, 0, SP * 6, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 2, 0, SP * 7, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 2, 0, SP * 8, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 2, 0, SP * 9, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 2, 0, SP * 10, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 2, 0, SP * 11, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 2, 0, SP * 12, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 2, 0, SP * 13, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 2, 0, SP * 14, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 2, 0, SP * 15, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 2, 0, SP * 16, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 2, 0, SP * 17, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 2, 0, SP * 18, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 2, 0, SP * 19, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 2, 0, SP * 20, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 2, 0, SP * 21, new String[]{"0", "0", "0", "0"}),
        // gap
        new TowerConfig(SP * 2, 0, SP * 24, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 2, 0, SP * 25, new String[]{"0", "0", "0", "0"}),

        // row 2
        new TowerConfig(SP * 3, 0, SP * 0, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 3, 0, SP * 1, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 3, 0, SP * 4, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 3, 0, SP * 5, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 3, 0, SP * 6, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 3, 0, SP * 7, new String[]{"0", "0", "0", "0"}),
        // gap
        // new TowerConfig(SP*1, 0, SP*4, new String[] {"0", "0", "0", "0"}),
        // new TowerConfig(SP*1, 0, SP*5, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP * 1, 0, SP * 6, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 1, 0, SP * 7, new String[]{"0", "0", "0", "0"}),
        // new TowerConfig(SP*1, 0, SP*8, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP * 3, 0, SP * 9, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 3, 0, SP * 10, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 3, 0, SP * 11, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 3, 0, SP * 12, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 3, 0, SP * 13, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 3, 0, SP * 14, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 3, 0, SP * 15, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 3, 0, SP * 16, new String[]{"0", "0", "0", "0"}),
        // new TowerConfig(SP*1, 0, SP*17, new String[] {"0", "0", "0", "0"}),
        // new TowerConfig(SP*1, 0, SP*18, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP * 3, 0, SP * 19, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 3, 0, SP * 20, new String[]{"0", "0", "0", "0"}),
        // new TowerConfig(SP*1, 0, SP*21, new String[] {"0", "0", "0", "0"}),
        // gap
        new TowerConfig(SP * 3, 0, SP * 24, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 3, 0, SP * 25, new String[]{"0", "0", "0", "0"}),

        // row 3
        new TowerConfig(SP * 4, 0, SP * 0, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 4, 0, SP * 1, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 4, 0, SP * 2, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 4, 0, SP * 3, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 4, 0, SP * 4, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 4, 0, SP * 5, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 4, 0, SP * 6, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 4, 0, SP * 7, new String[]{"0", "0", "0", "0"}),
        // gap
        new TowerConfig(SP * 4, 0, SP * 18, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 4, 0, SP * 19, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 4, 0, SP * 20, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 4, 0, SP * 21, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 4, 0, SP * 22, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 4, 0, SP * 23, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 4, 0, SP * 24, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 4, 0, SP * 25, new String[]{"0", "0", "0", "0"}),

        // row 4
        new TowerConfig(SP * 5, 0, SP * 0, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 5, 0, SP * 1, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 5, 0, SP * 2, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 5, 0, SP * 3, new String[]{"0", "0", "0", "0"}),
        // gap
        new TowerConfig(SP * 5, 0, SP * 22, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 5, 0, SP * 23, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 5, 0, SP * 24, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 5, 0, SP * 25, new String[]{"0", "0", "0", "0"}),

        // row 5
        new TowerConfig(SP * 6, 0, SP * 0, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 6, 0, SP * 1, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 6, 0, SP * 2, new String[]{"0", "0", "0", "0"}),
        // gap
        new TowerConfig(SP * 6, 0, SP * 23, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 6, 0, SP * 24, new String[]{"0", "0", "0", "0"}),
        new TowerConfig(SP * 6, 0, SP * 25, new String[]{"0", "0", "0", "0"}),
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

        /* Cubes ----------------------------------------------------------*/
        List<CubesModel.Tower> towers = new ArrayList<>();
        List<CubesModel.Cube> allCubes = new ArrayList<>();

        int stripId = 0;
        for (TowerConfig config : TOWER_CONFIG) {
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


    public void setupLx(SLStudioLX lx) {

    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {

    }
}
