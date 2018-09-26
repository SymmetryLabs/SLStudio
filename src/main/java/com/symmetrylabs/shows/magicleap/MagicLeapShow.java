package com.symmetrylabs.shows.magicleap;

import java.util.*;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.slstudio.model.SLModel;

import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.shows.Show;

/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class MagicLeapShow extends CubesShow implements Show {
    public static final String SHOW_NAME = "magicleap";

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;


    static final TowerConfig[] TOWER_CONFIG = {
        // back row
        new TowerConfig(SP*0.0f, 0, 0, new String[] {"0", "0", "0", "0", "0"}),
        new TowerConfig(SP*1.0f, 0, 0, new String[] {"0", "0", "0", "0", "0"}),
        new TowerConfig(SP*2.0f, 0, 0, new String[] {"0", "0", "0", "0", "0"}),
        new TowerConfig(SP*3.0f, 0, 0, new String[] {"0", "0", "0", "0", "0"}),
        new TowerConfig(SP*4.0f, 0, 0, new String[] {"0", "0", "0", "0", "0"}),

        // front
        new TowerConfig(SP*0.5f, SP*0.5f, -SP, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*1.5f, SP*0.5f, -SP, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*2.5f, SP*0.5f, -SP, new String[] {"0", "0", "0", "0"}),
        new TowerConfig(SP*3.5f, SP*0.5f, -SP, new String[] {"0", "0", "0", "0"}),
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
}
