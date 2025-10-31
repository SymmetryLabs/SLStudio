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
        //back middle (tallest tower)
        new TowerConfig(CSP*0, 0, CSP*0, 0, 45, 0, new String[] {"71", "1361", "337"}),
        new TowerConfig(CSP*1, 0, CSP*0, 0, 45, 0, new String[] {"2001", "1055", "569"}),
        new TowerConfig(CSP*2, 0, CSP*0, 0, 45, 0, new String[] {"21", "326", "157", "51"}),
        new TowerConfig(CSP*3, 0, CSP*0, 0, 45, 0, new String[] {"1346", "549", "1217", "192"}),
        new TowerConfig(CSP*4, 0, CSP*0, 0, 45, 0, new String[] {"113", "787", "9", "938"}),
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

        CubesModel m = new CubesModel(SHOW_NAME, towers, allCubesArr, cubeInventory, mapping);
        m.setTopologyTolerances(2, 6, 8);
        return m;
    }

    public void setupUi(final SLStudioLX lx, SLStudioLX.UI ui) {
        super.setupUi(lx, ui);


    }

    @Override
    public String getShowName() {
        return SHOW_NAME;
    }
}
