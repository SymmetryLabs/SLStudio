package com.symmetrylabs.shows.ysiadsparty;

import java.util.*;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.showplugins.FaderLimiter;

import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.shows.Show;

/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class YsiadsPartyShow extends CubesShow implements Show {
    public static final String SHOW_NAME = "ysiadsparty";

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;
    static final float CSP = 32;


    static final TowerConfig[] TOWER_CONFIG = {
        //Floaty Barge
        new TowerConfig(CSP*0, 0, 0, 0, -45, 0, new String[] { "62", "39", "549"}),
        new TowerConfig(CSP*1, 0, 0, 0, -45, 0, new String[] { "1055"}),
        new TowerConfig(CSP*2, 0, 0, 0, -45, 0, new String[] { "1008", "31", "157"}),

        //CANVAS PYRAMID
        //LEFT
        new TowerConfig(CSP*0, CSP*5, CSP*1, 0,-45, 0, new String[] { "113"}),
        //TOP
        new TowerConfig(CSP*1, CSP*6, CSP*1, 0,-45, 0, new String[] { "326"}),
        //RIGHT
        new TowerConfig(CSP*2, CSP*5, CSP*1, 0,-45, 0, new String[] { "9"}),
        //LEFT TOWER CUBES
        new TowerConfig(CSP*0, CSP*9, CSP*1, 0,-45, 0, new String[] { "1118", "5", "47", "356"}),
        //RIGHT TOWER CUBES
        new TowerConfig(CSP*2, CSP*9, CSP*1, 0,-45, 0, new String[] { "71", "337", "191", "23"}),
        //TOP CUBES
        new TowerConfig(CSP*1, CSP*14, CSP*1, 0,-45, 0, new String[] { "51"}),
        new TowerConfig(CSP*1, CSP*15, CSP*1, 0,-45, 0, new String[] { "422"}),






        // //Right Tower
        // new TowerConfig(CSP*4.5f, 0, CSP*  2, 0, -45, 0, new String[] { "71", "5", "51", "337"}),
        // new TowerConfig(CSP*5, 0, CSP*  1, 0, 0, 0, new String[] { "1055", "1008", "23"}),
        // new TowerConfig(CSP*5, 0, 0, 0, -45, 0, new String[] { "77", "157"}),
        //         new TowerConfig(CSP*5, 0, 0, 0, -45, 0, new String[] { "47", ""}),

        // //Bridge
        // new TowerConfig(CSP*1,CSP* 3, CSP*  2, 0, -45, 0, new String[] {"31"}),
        // new TowerConfig(CSP*2,CSP* 3, CSP*  2, 0, -45, 0, new String[] {"62"}),
        // new TowerConfig(CSP*3,CSP* 3, CSP*  2, 0, -45, 0, new String[] {"39"}),
        // new TowerConfig(CSP*4,CSP* 3, CSP*  2, 0, -45, 0, new String[] {"422"}),
        

        // new TowerConfig(CSP*1, 0, CSP*-1, 0, 45, 0, new String[] { "86", "68", "174",}),
        // new TowerConfig(CSP*1.5f, 0, CSP*-1.5f, 0, 45, 0, new String[] { "43", "25"}),
        // //back towers going to the left
        // new TowerConfig(CSP*-.5f, 0, CSP*-.5f, 0, 45, 0, new String[] { "128", "113", "63", "51"}),
        // new TowerConfig(CSP*-1, 0, CSP*-1, 0, 45, 0, new String[] { "132", "22", "1151",}),
        // //two towers in the middle going to the right
        // new TowerConfig(CSP*0, 0, CSP*-1, 0, 45, 0, new String[] { "1117", "172", "211",}),
        // new TowerConfig(CSP*.5f, 0, CSP*-1.5f, 0, 45, 0, new String[] { "314", "408"}),
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

    public void setupUi(final SLStudioLX lx, SLStudioLX.UI ui) {
        super.setupUi(lx, ui);
        FaderLimiter.attach(lx, 0.9f);
    
    }
}
