package com.symmetrylabs.shows.exploratorium;

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
public class ExploratoriumShow extends CubesShow implements Show, HasWorkspace {
    public static final String SHOW_NAME = "exploratorium";

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;
    static final float cuberotation = 0;

    private Workspace workspace;

    static final TowerConfig[] TOWER_CONFIG = {
        new TowerConfig(0, 0, 0, 180, -45+90, 0, new String[] { "326", "329", "39",}),
        new TowerConfig(0, SP*1, -SP*1.5f, 180, -45+90, 0, new String[] { "5410ecf67aeb",}),
        new TowerConfig(SP*1.5f, SP*1, 0, 180, -45+90, 0, new String[] { "120",}),
        new TowerConfig(SP*1.5f, SP*1, SP*1.5f, 180, -45+90, 0, new String[] { "122",}),
        new TowerConfig(SP*3, 0, SP*1.5f, 180, -45+90, 0, new String[] { "6",}),
        new TowerConfig(SP*3, 0, 0, 180, -45+90, 0, new String[] { "106", "351",}),

        new TowerConfig(SP*4.5f, SP*1, 0, 180, -45+90, 0, new String[] { "61", "362",}),
        new TowerConfig(SP*4.5f, 0, -SP*1.5f, 180, -45+90, 0, new String[] { "188", "69",}),

        new TowerConfig(SP*9, 0, 0, 180, -45+90, 0, new String[] { "77", "9", "384",}),
        new TowerConfig(SP*9, SP*1, -SP*1.5f, 180, -45+90, 0, new String[] { "31",}),
        new TowerConfig(SP*7.5f, SP*1, 0, 180, -45+90, 0, new String[] { "50",}),
        new TowerConfig(SP*7.5f, SP*1, SP*1.5f, 180, -45+90, 0, new String[] { "38",}),
        new TowerConfig(SP*6, 0, SP*1.5f, 180, -45+90, 0, new String[] { "197",}),
        new TowerConfig(SP*6, 0, 0, 180, -45+90, 0, new String[] { "111", "14",}),

        new TowerConfig(SP*1.5f, SP*3, -SP*4.5f, 180, -45+90, 0, new String[] { "356",}),
        new TowerConfig(SP*1.5f, SP*2, -SP*6, 180, -45+90, 0, new String[] { "365", "418",}),

        new TowerConfig(SP*7.5f, SP*3, -SP*4.5f, 180, -45+90, 0, new String[] { "390",}),
        new TowerConfig(SP*7.5f, SP*2, -SP*6, 180, -45+90, 0, new String[] { "55", "15",}),
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

        return new CubesModel(SHOW_NAME, towers, allCubesArr, cubeInventory, mapping);
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        workspace = new Workspace(lx, ui, "shows/exploratorium");
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public String getShowName() {
        return SHOW_NAME;
    }
}
