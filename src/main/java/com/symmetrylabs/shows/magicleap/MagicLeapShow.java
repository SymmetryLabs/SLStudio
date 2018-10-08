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

        // row 1
        // new TowerConfig(SP*1f, 0, SP*0, new String[] {"0", "0", "0", "0"}),
        // new TowerConfig(SP*2f, 0, SP*0, new String[] {"0", "0", "0", "0"}),
        // new TowerConfig(SP*3f, 0, SP*0, new String[] {"0", "0", "0", "0"}),

        // row 2
// d880399b0833
// 5410ecf67aeb
        new TowerConfig(SP*-2, 0, SP*1, 0, -90, 0, new String[] {"5410ecf67aeb", "69", "205", "330", "356"}),
        new TowerConfig(SP*-1, 0, SP*1, 0, -90, 0, new String[] {"329", "186", "51", "87", "35"}),
        new TowerConfig(SP*0, 0, SP*1, 0, -90, 0, new String[] {"38", "43", "354", "31", "132"}),
        new TowerConfig(SP*1, 0, SP*1, 0, 0, 0, new String[] {"39", "9", "174", "14", "50"}),
        new TowerConfig(SP*2, 0, SP*1, 0, 0, 0, new String[] {"412", "185", "120", "55", "320"}),
        new TowerConfig(SP*3, 0, SP*1, 0, 0, 0, new String[] {"25", "337", "384", "1", "138"}),



        // row 3

        new TowerConfig(SP*-2, 0, SP*2, 0, -90, 0, new String[] {"191", "22", "68", "94", "128"}),
        new TowerConfig(SP*-1, 0, SP*2, 0, -90, 0, new String[] {"351", "326", "126", "111", "188"}),
        new TowerConfig(SP*0, 0, SP*2, 0, -90, 0, new String[] {"105", "157", "98", "21", "197"}),
        new TowerConfig(SP*1, 0, SP*2, 0, 0, 0, new String[] {"34", "358", "408", "91", "76"}),
        new TowerConfig(SP*2, 0, SP*2, 0, 0, 0, new String[] {"d880399b0833", "23", "122", "61", "66"}),
        new TowerConfig(SP*3, 0, SP*2, 0, 0, 0, new String[] {"56", "123", "15", "77", "11"}),
// new TowerConfig(SP*3, 0, SP*2, 0, 90, 0, new String[] {"356"}),


        // row 4
        // new TowerConfig(SP*1f, 0, SP*3, new String[] {"0", "0", "0", "0"}),
        // new TowerConfig(SP*2f, 0, SP*3, new String[] {"0", "0", "0", "0"}),
        // new TowerConfig(SP*3f, 0, SP*3, new String[] {"0", "0", "0", "0"}),
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

        CubesModel m = new CubesModel(towers, allCubesArr);
        m.setTopologyTolerances(2, 6, 8);
        return m;
    }
}
