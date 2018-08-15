package com.symmetrylabs.shows.office;

//package com.symmetrylabs.shows.cubes;

import java.util.*;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.slstudio.model.SLModel;

import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.shows.Show;

/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class OfficeShow extends CubesShow implements Show {

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 180;
    static final float globalRotationZ = 0;


    static final TowerConfig[] TOWER_CONFIG = {
//        new TowerConfig(SP*0.f, 0, -SP*0.f, new String[] {"0", "0", "0", "0", "0"}),
//        new TowerConfig(SP*1.f, 0, -SP*1.f, new String[] {"0", "0", "0", "0", "0"}),
//        new TowerConfig(SP*2.f, 0, -SP*2.f, new String[] {"0", "0", "0", "0", "0"}),
//        new TowerConfig(SP*3.f, 0, -SP*3.f, new String[] {"0", "0", "0", "0", "0"}),
//        new TowerConfig(SP*4.f, 0, -SP*4.f, new String[] {"0", "0", "0", "0", "0"}),
//        new TowerConfig(SP*5.f, 0, -SP*5.f, new String[] {"0", "0", "0", "0", "0"}),

// back row right to left

        new TowerConfig(0, 0, -264+168, 0, 45, 0, new String[] { "d880399b2000", "d880396305af", "d8803962b00e", "398", "44"}), //397, 16, 50
        new TowerConfig(36, 0, -264+168, 0, 45, 0, new String[] { "d8803962f603", "87", "d880399b2b08", "001ec0f543f4"}),  //76, 420,  186
        new TowerConfig(54, SP*4, -264+168, 0, 45, 0, new String[] {"367"}),
        new TowerConfig(72, 0, -264+168, 0, 45, 0, new String[] { "d8803963520d", "336", "001ec0f56380", "82"}),//68, 199
        new TowerConfig(108, 0, -264+168, 0, 45, 0, new String[] { "d8803962cc02", "419", "1", "57", "415hp"}), //17

// 2nd to back row right to left

//        new TowerConfig(144, 0, -264+168, 0, 45, 0, new String[] { "0"}),
        new TowerConfig(24, 0, -228+168, 0, 45, 0, new String[] { "372", "6", "001ec0f43d1d", "4"}), // 211
        new TowerConfig(90 , 0, -228+168, 0, 45, 0, new String[] { "001ec0f4ea81", "d8803963052f", "001ec0f4abc2", "21"}), // 138, 27, 156

// 3rd from back r to l
        new TowerConfig(40, 0, -192+164, 0, 45, 0, new String[] { "001ec0f5270f", "418", "65"}),        //348
        new TowerConfig(74, 0, -192+164, 0, 45, 0, new String[] { "351", "79", "001ec0f54cd7"}),   //184

// front

        new TowerConfig(54, 0, -168+164, 0, 45, 0, new String[] { "d880399b2b0a", "001ec0f56df8", "353"}),     // 312, 189
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
}
