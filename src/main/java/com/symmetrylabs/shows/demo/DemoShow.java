package com.symmetrylabs.shows.demo;

import java.util.*;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.slstudio.model.SLModel;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEngine;
import heronarts.lx.transform.LXTransform;

/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class DemoShow extends CubesShow implements Show, LXEngine.Listener {

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;

    static final int SP = 34;
    static final int SPR = 26;

    static final TowerConfig[] TOWER_CONFIG = {

    	//1012 top 1039 bottom cube of bottom two stack

    	//1084 top 204 bottom of top cube



    //Bottom Row
    new TowerConfig(SP * 0, 0, SP * 0, 0, -45, 0, new String[] {"329"}),
    new TowerConfig(SP * 1, 0, SP * 0, 0, -45, 0, new String[] {"61"}),
    new TowerConfig(SP * 2, 0, SP * 0, 0, -45, 0, new String[] {"1117"}),
    new TowerConfig(SP * 3, 0, SP * 0, 0, -45, 0, new String[] {"188"}),
    new TowerConfig(SP * 4, 0, SP * 0, 0, -45, 0, new String[] {"337"}),
    new TowerConfig(SP * 5, 0, SP * 0, 0, -45, 0, new String[] {"1119"}),

    //Middle Row
    new TowerConfig(SP * 0.5f, SPR*1, SP * 0, 0, -45, 0, new String[] {"412"}),
    new TowerConfig(SP * 1.5f, SPR*1, SP * 0, 0, -45, 0, new String[] {"326"}),
    new TowerConfig(SP * 2.5f, SPR*1, SP * 0, 0, -45, 0, new String[] {"5410ecf67aeb"}),
    new TowerConfig(SP * 3.5f, SPR*1, SP * 0, 0, -45, 0, new String[] {"31"}),
    new TowerConfig(SP * 4.5f, SPR*1, SP * 0, 0, -45, 0, new String[] {"5410ecf48d34"}),

    new TowerConfig(SP * 1, SPR*2, SP * 0, 0, -45, 0, new String[] {"128"}),
    new TowerConfig(SP * 2, SPR*2, SP * 0, 0, -45, 0, new String[] {"123"}),
    new TowerConfig(SP * 3, SPR*2, SP * 0, 0, -45, 0, new String[] {"1008"}),
    new TowerConfig(SP * 4, SPR*2, SP * 0, 0, -45, 0, new String[] {"38"}),


    new TowerConfig(SP * 2.5f, SPR*1, SP * 0, 0, -45, 0, new String[] {"157"}),





//     	new TowerConfig(SP * -1.9f + .9f, 0, SP * .7f, 0, -45, 0, new String[] {"71", "9"}),
//     	new TowerConfig(SP * -0.6f, 0, SP * .5f, 0, -45, 0, new String[] {"422"}),

//     	new TowerConfig(SP * -1.1f,SP * 0, 0, 0, -45, 0, new String[] {"56"}),
//         new TowerConfig(SP * -1.1f, SP * 1, 0, 0, -45, 0, new String[] {"23"}),
//         new TowerConfig(SP * -1.1f, SP * 2, 0, 0, -45, 0, new String[] {"341"}),
//         new TowerConfig(SP * -1.1f, SP * 3, 0, 0, -45, 0, new String[] {"186"}),
//         new TowerConfig(SP * -1.1f, SP * 4, 0, 0, -45, 0, new String[] {"320"}),
// //        new TowerConfig(CubesModel.Cube.Type.HD, SP * -1.1f, SP * -6, SP * -5, 0, 0, 0, new String[] {"",""}),
// //         new TowerConfig(CubesModel.Cube.Type.HD, SP * -1.1f, SP * -7, SP * -5, 0, 0, 0, new String[] {"",""}),


//         new TowerConfig(SP * 0, SP * 5, 0, 0, -45, 0, new String[] {"5"}),

//         new TowerConfig(SP * 1.1f, SP * 0, 0, 0, -45, 0, new String[] {"87"}),
//         new TowerConfig(SP * 1.1f, SP * 1, 0, 0, -45, 0, new String[] {"321"}),
//         new TowerConfig(SP * 1.1f, SP * 2, 0, 0, -45, 0, new String[] {"111"}),
//         new TowerConfig(SP * 1.1f, SP * 3, 0, 0, -45, 0, new String[] {"47"}),
//         new TowerConfig(SP * 1.1f, SP * 4, 0, 0, -45, 0, new String[] {"35"}),
//         // new TowerConfig(SP * 1.1f, SP * 5, 0, 0, -45, 0, new String[] {""}),
// //        new TowerConfig(CubesModel.Cube.Type.HD, SP * 1.1f, SP * -4, SP * -5, 0, 0, 0, new String[] {"1039","1012"}),




//         new TowerConfig(SP * 2, 0, SP * .7f, 0, -45, 0, new String[] {"120", "177"}),
//         new TowerConfig(SP * 0.6f, 0, SP * .5f, 0, -45, 0, new String[] {"21"}),


//         new TowerConfig(SP * -1.5f, SP * -2, SP * -1, 0, -45, 0, new String[] {"356"}),
//         new TowerConfig(SP * 0, SP * -1, SP * -1, 0, -45, 0, new String[] {"39"}),
//         new TowerConfig(SP * 1.5f, SP * -2, SP * -1, 0, -45, 0, new String[] {"191"}),

//         new TowerConfig(CubesModel.Cube.Type.HD, SP * 1.1f, SP * -6, SP * -5, 0, 0, 0, new String[] {"204","1084"}),
//         new TowerConfig(CubesModel.Cube.Type.HD, SP * 1.1f, SP * -7, SP * -5, 0, 0, 0, new String[] {"1012","1039"}),

//         new TowerConfig(CubesModel.Cube.Type.HD, SP * -1.1f, SP * -7, SP * -5, 0, 0, 0, new String[] {"1077","1076"}),
//         new TowerConfig(CubesModel.Cube.Type.HD, SP * -1.1f, SP * -6, SP * -5, 0, 0, 0, new String[] {"587","586"}),


//         new TowerConfig(CubesModel.Cube.Type.HD, SP * 1.5f, SP * -4, SP * -2, 0, 0, 0, new String[] {"", ""}),
//         new TowerConfig(CubesModel.Cube.Type.HD, SP * -1.5f, SP * -4, SP * -2, 0, 0, 0, new String[] {"", ""}),
// 	static final TowerConfigHD[] TOWER_CONFIG = {
//         new TowerConfigHD(SP * 1.5f, SP * -4, SP * -2, 0, 0, 0, new String[][]{new String[] {"1012", "1039"},}),
//         new TowerConfigHD(SP * -1.5f, SP * -4, SP * -2, 0, 0, 0, new String[][]{new String[] {"1012", "1039"},}),
// }
// TowerConfig(CubesModel.Cube.Type type, float x, float y, float z, float xRot, float yRot, float zRot, String[] ids)

        // new TowerConfig(0, 0, 0, new String[] {"354", "365", "21", "47"}),

        // new TowerConfig(48, 24, 24, 0, -90, 0, new String[] {"186", "9"}),
        // new TowerConfig(48+24, 24, 24, 0, 0, 0, new String[] {"120", "71"}),

        // new TowerConfig(24*4, 28*4, 0, 0, 45, 0, new String[] {"321"}),
        // new TowerConfig(24*4, 28*4, -24, 0, 45, 0, new String[] {"356"}),
        // new TowerConfig(24*4, 28*4, -24*2, 0, 45, 0, new String[] {"320"}),
        // new TowerConfig(24*4, 28*4, -24*3, 0, 45, 0, new String[] {"87"}),
        // // new TowerConfig(24*4, 28*4, -24*4, 0, 45, 0, new String[] {"177"}),

        // new TowerConfig(24*3, 0, -28*5, 0, 90, 0, new String[] {"56", "422", "23"}),








        // new TowerConfig(-24, TOWER_RISER, 12, new String[] {"22", "10"}),
        // new TowerConfig(12, JUMP+TOWER_RISER, -25, new String[] {"85"}),
        // new TowerConfig(12, JUMP+TOWER_RISER, 25, new String[] {"94"}),

        // // group 2
        // new TowerConfig(25, TOWER_RISER, 11, new String[] {"115"}),
        // new TowerConfig(26, 2*JUMP+TOWER_RISER, 36, new String[] {"15"}),
        // new TowerConfig(51, 0, 24, new String[] {"63", "314", "71", "43"}),

        // // group 3
        // new TowerConfig(38, 0, -14, new String[] {"70", "337", "76", "31"}),
        // new TowerConfig(63, TOWER_RISER, -2, new String[] {"182"}),
        // new TowerConfig(50, TOWER_RISER, -39, new String[] {"137"}),

        // // group 4
        // new TowerConfig(88, 0, -14, new String[] {"17", "356", "132", "83"}),
        // new TowerConfig(76, JUMP+TOWER_RISER, 12, new String[] {"52"}),
        // new TowerConfig(63, 2*JUMP+TOWER_RISER, -26, new String[] {"11"}),
        // new TowerConfig(100, TOWER_RISER, -39, new String[] {"56"}),
        // new TowerConfig(113, 2*JUMP+TOWER_RISER, -26, new String[] {"352"}),

        // // group 5
        // new TowerConfig(88, 0, -65, new String[] {"120", "68", "54", "46"}),
        // new TowerConfig(63, JUMP+TOWER_RISER, -53, new String[] {"181"}),
        // new TowerConfig(76, TOWER_RISER, -90, new String[] {"38"}),
        // new TowerConfig(100, 2*JUMP+TOWER_RISER, -90, new String[] {"62"}),

        // // group 6
        // new TowerConfig(125, 0, -78, new String[] {"7", "127", "86"}),
        // new TowerConfig(113, JUMP+TOWER_RISER, -53, new String[] {"320"}),

        // // group 7
        // new TowerConfig(112, 0, -115, new String[] {"90", "121", "32"}),
        // new TowerConfig(87, JUMP+TOWER_RISER, -103, new String[] {"156"})

        // group 7
//        new TowerConfig(124, TOWER_RISER, -139, new String[] {"82", "4"}),
//
//
//        // other side
//        new TowerConfig(124, TOWER_RISER, -RIGHT_SIDE_SPACING, -90, new String[] {"383"}),
//        new TowerConfig(124, JUMP*2+TOWER_RISER, -RIGHT_SIDE_SPACING, -90, new String[] {"358"}),
//
//        new TowerConfig(112, 0, -RIGHT_SIDE_SPACING-24, -90, new String[] {"91", "40", "340", "360"}),
//
//        new TowerConfig(88, JUMP+TOWER_RISER, -RIGHT_SIDE_SPACING-12, -90, new String[] {"172"}),
//
//        new TowerConfig(100, JUMP*2+TOWER_RISER, -RIGHT_SIDE_SPACING-48, -90, new String[] {"57"}),
//
//        new TowerConfig(64, 0, -RIGHT_SIDE_SPACING-24, -90, new String[] {"390", "408", "185"}),
//
//        new TowerConfig(52, TOWER_RISER, -RIGHT_SIDE_SPACING-48, -90, new String[] {"369"}),
//        new TowerConfig(52, JUMP*2+TOWER_RISER, -RIGHT_SIDE_SPACING-48, -90, new String[] {"398"}),
//
//        new TowerConfig(76, 0, -RIGHT_SIDE_SPACING-60, -90, new String[] {"326", "128", "391", "341"}),
//
//        new TowerConfig(28, 0, -RIGHT_SIDE_SPACING-60, -90, new String[] {"129", "13", "77"})
    };

    // static class TowerConfigHD {

    //     final CubesModel.Cube.Type type;
    //     final float x;
    //     final float y;
    //     final float z;
    //     final float xRot;
    //     final float yRot;
    //     final float zRot;
    //     final String[][] ids;
    //     final float[] yValues;

    //     TowerConfigHD(float x, float y, float z, String[][] ids) {
    //         this(CubesModel.Cube.Type.LARGE, x, y, z, ids);
    //     }

    //     TowerConfigHD(float x, float y, float z, float yRot, String[][] ids) {
    //         this(x, y, z, 0, yRot, 0, ids);
    //     }

    //     TowerConfigHD(CubesModel.Cube.Type type, float x, float y, float z, String[][] ids) {
    //         this(type, x, y, z, 0, 0, 0, ids);
    //     }

    //     TowerConfigHD(CubesModel.Cube.Type type, float x, float y, float z, float yRot, String[][] ids) {
    //         this(type, x, y, z, 0, yRot, 0, ids);
    //     }

    //     TowerConfigHD(float x, float y, float z, float xRot, float yRot, float zRot, String[][] ids) {
    //         this(CubesModel.Cube.Type.LARGE, x, y, z, xRot, yRot, zRot, ids);
    //     }

    //     TowerConfigHD(CubesModel.Cube.Type type, float x, float y, float z, float xRot, float yRot, float zRot, String[][] ids) {
    //         this.type = type;
    //         this.x = x;
    //         this.y = y;
    //         this.z = z;
    //         this.xRot = xRot;
    //         this.yRot = yRot;
    //         this.zRot = zRot;
    //         this.ids = ids;

    //         this.yValues = new float[ids.length];
    //         for (int i = 0; i < ids.length; i++) {
    //             yValues[i] = y + i * (CUBE_HEIGHT + CUBE_SPACING);
    //         }
    //     }
    // }


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
            float y = config.yValues[0];
            float z = config.z;
            float xRot = config.xRot;
            float yRot = config.yRot;
            float zRot = config.zRot;
            CubesModel.Cube.Type type = config.type;
            System.out.println(type);
            if (type == CubesModel.Cube.Type.HD) {

                CubesModel.DoubleControllerCube cube = new CubesModel.DoubleControllerCube(config.ids[0], config.ids[1], x, y, z, xRot, yRot, zRot, globalTransform);
                cubes.add(cube);
                allCubes.add(cube);

            }
            for (int i = 0; i < config.ids.length; i++) {
                float yTower = config.yValues[i];
                if (type == CubesModel.Cube.Type.LARGE) {
                    CubesModel.Cube cube = new CubesModel.Cube(config.ids[i], x, yTower, z, xRot, yRot, zRot, globalTransform, type);
                    cubes.add(cube);
                    allCubes.add(cube);
                }


            }
            towers.add(new CubesModel.Tower("", cubes));
        }

        // for (TowerConfig config : TOWER_CONFIG) {
        //     List<CubesModel.Cube> cubes = new ArrayList<>();
        //     float x = config.x;
        //     float z = config.z;
        //     float xRot = config.xRot;
        //     float yRot = config.yRot;
        //     float zRot = config.zRot;
        //     CubesModel.Cube.Type type = config.type;

        //     for (int i = 0; i < config.ids.length; i++) {
        //         float y = config.yValues[i];
        //         CubesModel.DoubleControllerCube cube =
        //             new CubesModel.DoubleControllerCube(
        //                 config.ids[i][0], config.ids[i][1],
        //                 x, y, z, xRot, yRot, zRot, globalTransform);
        //         cubes.add(cube);
        //         allCubes.add(cube);
        //     }
        //     towers.add(new CubesModel.Tower("", cubes));
        // }
        /*-----------------------------------------------------------------*/

        CubesModel.Cube[] allCubesArr = new CubesModel.Cube[allCubes.size()];
        for (int i = 0; i < allCubesArr.length; i++) {
            allCubesArr[i] = allCubes.get(i);
        }

        return new CubesModel(towers, allCubesArr);
    }

    @Override
    public void setupLx(LX lx) {
        lx.engine.addListener(this);
        for (LXChannel c : lx.engine.channels) {
            System.out.println("is this working" + c);
            c.autoDisable.setValue(true);
        }

    }

    @Override
    public void channelAdded(LXEngine engine, LXChannel lxChannel) {
        lxChannel.autoDisable.setValue(true);
    }

    @Override
    public void channelRemoved(LXEngine engine, LXChannel channel) {

    }

    @Override
    public void channelMoved(LXEngine engine, LXChannel channel) {

    }
}
