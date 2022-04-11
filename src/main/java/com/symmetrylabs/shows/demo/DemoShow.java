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

    static final int SP = 24;
    static final int SPR = 26;

    static final TowerConfig[] TOWER_CONFIG = {

    	//1012 top 1039 bottom cube of bottom two stack

    	//1084 top 204 bottom of top cube



    //--------------------------------------------SIDE 1-------------------------
    //LEFT SIDE STARTING FROM BOTTOM
// /*0*/new TowerConfig(CubesModel.Cube.Type.LARGE, 0, SP * 1, 0, 0, 0, 0, new String[] {"0"}),
/*1*/new TowerConfig(CubesModel.Cube.Type.HD, 0, SP * 1, 0, 0, 0, 0, new String[] {"0", "0"}),
/*2*/new TowerConfig(CubesModel.Cube.Type.HD, SP*.2f, SP*2, SP*0, 0, 0, 0, new String[] {"0", "0"}),
/*3*/new TowerConfig(CubesModel.Cube.Type.HD, SP*.2f, SP*3, SP*0, 0, 0, 0, new String[] {"0", "0"}),
/*4*/new TowerConfig(CubesModel.Cube.Type.HD, SP*.6f, SP*4, SP*-.15f, 0, 0, 0, new String[] {"0", "0"}),
/*5*/new TowerConfig(CubesModel.Cube.Type.HD, SP*.8f, SP*5, SP*-.2f, 0, 0, 0, new String[] {"0", "0"}),
/*6*/new TowerConfig(CubesModel.Cube.Type.HD, SP*.5f, SP*6, SP*.5f, 0, 0, 0, new String[] {"0", "0"}),
/*7*/new TowerConfig(CubesModel.Cube.Type.HD, SP*-1.2f, SP*7, SP*0f, 0, 0, 0, new String[] {"0", "0"}),
/*8*/new TowerConfig(CubesModel.Cube.Type.HD, SP*.5f, SP*8, SP*0, 0, 0, 0, new String[] {"0", "0"}),
/*9*/new TowerConfig(CubesModel.Cube.Type.HD, SP*1.5f, SP*7.8f, SP*-1, 0, 0, 0, new String[] {"0", "0"}),
/*10*/new TowerConfig(CubesModel.Cube.Type.HD, SP*1.7f, SP*7.7f, SP*0, 0, 0, 0, new String[] {"0", "0"}),
/*11*/new TowerConfig(CubesModel.Cube.Type.HD, SP*2.9f, SP*7f, SP*-.6f, 0, 0, 0, new String[] {"0", "0"}),
/*12*/new TowerConfig(CubesModel.Cube.Type.HD, SP*3.9f, SP*7f, SP*-.5f, 0, 0, 0, new String[] {"0", "0"}),
/*13*/new TowerConfig(CubesModel.Cube.Type.HD, SP*2.9f, SP*6f, SP*-.5f, 0, 0, 0, new String[] {"0", "0"}),
/*14*/new TowerConfig(CubesModel.Cube.Type.HD, SP*1.8f, SP*5f, SP*-.5f, 0, 0, 0, new String[] {"0", "0"}),
/*15*/new TowerConfig(CubesModel.Cube.Type.HD, SP*2.3f, SP*4f, SP*-.5f, 0, 0, 0, new String[] {"0", "0"}),
/*16*/new TowerConfig(CubesModel.Cube.Type.HD, SP*2.3f, SP*3f, SP*-.5f, 0, 0, 0, new String[] {"0", "0"}),
/*17*/new TowerConfig(CubesModel.Cube.Type.HD, SP*3, SP*2, SP*-.5f, 0, 0, 0, new String[] {"0", "0"}),
/*18*/new TowerConfig(CubesModel.Cube.Type.HD, SP*3.1f, SP*1, SP*-.5f, 0, 0, 0, new String[] {"0", "0"}),



    };

    static class TowerConfigHD {

        final CubesModel.Cube.Type type;
        final float x;
        final float y;
        final float z;
        final float xRot;
        final float yRot;
        final float zRot;
        final String[][] ids;
        final float[] yValues;

        TowerConfigHD(float x, float y, float z, String[][] ids) {
            this(CubesModel.Cube.Type.LARGE, x, y, z, ids);
        }

        TowerConfigHD(float x, float y, float z, float yRot, String[][] ids) {
            this(x, y, z, 0, yRot, 0, ids);
        }

        TowerConfigHD(CubesModel.Cube.Type type, float x, float y, float z, String[][] ids) {
            this(type, x, y, z, 0, 0, 0, ids);
        }

        TowerConfigHD(CubesModel.Cube.Type type, float x, float y, float z, float yRot, String[][] ids) {
            this(type, x, y, z, 0, yRot, 0, ids);
        }

        TowerConfigHD(float x, float y, float z, float xRot, float yRot, float zRot, String[][] ids) {
            this(CubesModel.Cube.Type.LARGE, x, y, z, xRot, yRot, zRot, ids);
        }

        TowerConfigHD(CubesModel.Cube.Type type, float x, float y, float z, float xRot, float yRot, float zRot, String[][] ids) {
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
