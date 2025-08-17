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
    static final float CSP = 24;
    static final int CHT = 26;


    static final TowerConfig[] TOWER_CONFIG = {
    new TowerConfig(98, CSP*2.5f, -153, 0, -45, 0, new String[] { "326"}),
    new TowerConfig(115, CSP*0, -136, 0, -45, 0, new String[] {"33","35","549","113"}),
    new TowerConfig(98, CSP*3.5f, -119, 0, -45, 0, new String[] {"71"}),
    new TowerConfig(132, CSP*1.5f, -153, 0, -45, 0, new String[] { "1118"}),
    new TowerConfig(149, CSP*2, -136, 0, -45, 0, new String[] { "21"}),
    new TowerConfig(157, CSP*0, -144, 0, -45, 0, new String[] { "1346","51"}),
    new TowerConfig(170, CSP*2, -132, 0, -45, 0, new String[] { "787"}),
    new TowerConfig(187, CSP*1.5f, -149, 0, -45, 0, new String[] { "337"}),
    new TowerConfig(204, CSP*0, -132, 0, -45, 0, new String[] { "9","938","1055","1361"}),
    new TowerConfig(221, CSP*2.5f, -149, 0, -45, 0, new String[] { "157"}),
    new TowerConfig(221, CSP*3.5f, -115, 0, -45, 0, new String[] { "571"}),

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
