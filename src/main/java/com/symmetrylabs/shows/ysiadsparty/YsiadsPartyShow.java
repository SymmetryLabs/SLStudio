package com.symmetrylabs.shows.ysiadsparty;

import java.util.*;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;


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
         //back towers left to the right
        new TowerConfig(CSP*0, 0, 0, 0, -45, 0, new String[] { "50", "15", "122", "61", "188"}),
        new TowerConfig(CSP*1, 0, 0, 0, -45, 0, new String[] { "55", "26", "111", "384", "6"}),
        new TowerConfig(CSP*2, 0, 0, 0, -45, 0, new String[] { "91", "362", "31", "9"}),
        new TowerConfig(CSP*3, 0, 0, 0, -45, 0, new String[] { "197", "1118", "418"}),
        new TowerConfig(CSP*4, 0, 0, 0, -45, 0, new String[] { "329", "356", "69", "62"}),
        // //second row towers left to right
        new TowerConfig(CSP*2.5f, 0, CSP*-1, 0, -45, 0, new String[] { "33", "351", "365"}),
        new TowerConfig(CSP*3, CSP*2.5f, CSP*-1, 0, -45, 0, new String[] { "77"}),
        new TowerConfig(CSP*3.5f, 0, CSP*-1, 0, -45, 0, new String[] { "391", "123", "54"}),
            // //two towers by speaker
        new TowerConfig(CSP*1.5f, 0, CSP*-4, 0, +90, 0, new String[] { "14", "128"}),
        new TowerConfig(CSP*3.5f, 0, CSP*-4, 0, -90, 0, new String[] { "1119", "5410ecf67aeb"}),

        // new TowerConfig(CSP*-.5f, 0, CSP*-.5f, 0, 45, 0, new String[] { "128", "113", "63", "51"}),
        // new TowerConfig(CSP*-1, 0, CSP*-1, 0, 45, 0, new String[] { "132", "22", "1151",}),
   
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

    
    }
}
