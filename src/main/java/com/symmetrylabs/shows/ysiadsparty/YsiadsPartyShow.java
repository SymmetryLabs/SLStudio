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
    static final int CHT = 26;


    static final TowerConfig[] TOWER_CONFIG = {
        //Back Row 1-4
        new TowerConfig(CSP*-1.5f, CHT*.667f, CSP*0, 0, -45, 0, new String[] { "568", "787","51"}),
        new TowerConfig(CSP*-.5f, CHT*1.667f, CSP*.5f, 0, -45, 0, new String[] { "422", "39"}),
        new TowerConfig(CSP*.5f, CHT*1.667f, CSP*.5f, 0, -45, 0, new String[] { "23", "356"}),
        new TowerConfig(CSP*1.5f, CHT*.667f, CSP*0, 0, -45, 0, new String[] { "14", "9","677"}),


        //Middle row 5-10
       
        new TowerConfig(CSP*-2, CHT*1.333f, CSP*0, 0, -45, 0, new String[] {"33","35"}),
        new TowerConfig(CSP*-1, CHT*1, CSP*0, 0, -45, 0, new String[] {"191", "1118", "321"}),
        new TowerConfig(CSP*0, CHT*2.333f, CSP*0, 0, -45, 0, new String[] {"326", "21"}),
        new TowerConfig(CSP*1, CHT*1, CSP*0, 0, -45, 0, new String[] {"71", "5410ecf5317f", "185"}),
        new TowerConfig(CSP*2, CHT*1.333f, CSP*0, 0, -45, 0, new String[] {"5","337"}),
        new TowerConfig(CSP*-2.75f, CHT*1.75f, CSP*0.25f, 0, -45, 0, new String[] {"1055"}),
        

        //Front row and top 11-16
        new TowerConfig(CSP*-1.333f, CHT*0, CSP*-.5f, 0, -45, 0, new String[] {"752"}),
        new TowerConfig(CSP*-.5f, CHT*3.667f, CSP*-.667f, 0, -45, 0, new String[] {"113"}),
        new TowerConfig(CSP*.5f, CHT*3.667f, CSP*-.667f, 0, -45, 0, new String[] {"910"}),
        new TowerConfig(CSP*1.333f, CHT*0, CSP*-.5f, 0, -45, 0, new String[] {"157"}),
        new TowerConfig(CSP*2.333f, CHT*1.75f, CSP*-.75f, 0, -45, 0, new String[] {"1106"}),
        new TowerConfig(CSP*0, CHT*4.333f, CSP*-1.333f, 0, -45, 0, new String[] {"549"})


        // //RIGHT FRONT SINGLE LOWEST
        //  new TowerConfig(CSP*.5f, CHT*-.5f, CSP*-1.5f, 0, 45, 0, new String[] {""}),

        // //RIGHT FRONT SINGLE MID
        // new TowerConfig(CSP*3, CHT*.5f, CSP*-.5f, 0, 45, 0, new String[] {""}),

        // //RIGHT FRONT SINGLE LOWEST
        //  new TowerConfig(CSP*3, CHT*-.5f, CSP*-1.5f, 0, 45, 0, new String[] {""}),
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
