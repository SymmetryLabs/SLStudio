package com.symmetrylabs.shows.office;

//package com.symmetrylabs.shows.cubes;

import java.util.*;

import com.symmetrylabs.shows.HasWorkspace;
import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.workspaces.Workspace;

import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.shows.Show;

/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class OfficeShow extends CubesShow implements Show, HasWorkspace {

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 180;
    static final float globalRotationZ = 0;
    static final float cuberotation = 135;

    static final float diag = (float) Math.sqrt(2) * SP;

    private Workspace workspace;

    static final TowerConfig[] TOWER_CONFIG = {
        new TowerConfig(diag * 0,    0, 0,           0, cuberotation, 0, new String[] { "33", "5410ecf67aeb", "106", "6", "14" }),
        new TowerConfig(diag * -1,    0, 0,           0, cuberotation, 0, new String[] { "197", "362", "31", "326" }),
        new TowerConfig(diag * 1,    0, diag * 1.5f,           0, cuberotation, 0, new String[] { "1118", "329", "391" }),
        new TowerConfig(diag * 0.5f, 0, diag * .75f,  0, cuberotation, 0, new String[] { "351", "111", "91", "55" }),
        new TowerConfig(diag * -0.5f, 0, diag * .75f,  0, cuberotation, 0, new String[] { "1119", "62", "38", "418" }),
        new TowerConfig(diag * -1.5f, 0, diag * .75f,  0, cuberotation, 0, new String[] { "188", "77", "390" }),
        new TowerConfig(diag * 0,    0, diag * 1.5f, 0, cuberotation, 0, new String[] { "61", "26", "365" }),
        new TowerConfig(diag * -1,    0, diag * 1.5f, 0, cuberotation, 0, new String[] { "50", "69" }),
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

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        super.setupUi(lx, ui);
        workspace = new Workspace(lx, ui, "shows/office");
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }
}
