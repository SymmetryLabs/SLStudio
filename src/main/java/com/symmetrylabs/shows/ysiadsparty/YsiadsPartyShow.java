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


    static final TowerConfig[] TOWER_CONFIG = {
new TowerConfig(93, CSP*0, -229, 0, -45, 0, new String[] {"566"}), 
new TowerConfig(119, CSP*0, -238, 0, -45, 0, new String[] { "549"}),
new TowerConfig(102, CSP*1, -238, 0, -45, 0, new String[] { "9","337" }),
new TowerConfig(127, CSP*1, -238, 0, -45, 0, new String[] { "21","833" }), // end of 2 stacks left
new TowerConfig(110, CSP*3, -221, 0, -45, 0, new String[] { "1106" }), 
new TowerConfig(110, CSP*4, -195, 0, -45, 0, new String[] { "d880399b135e" }),
new TowerConfig(136, CSP*4, -204, 0, -45, 0, new String[] { "326" }),
new TowerConfig(161, CSP*4, -212, 0, -45, 0, new String[] { "51" }),
new TowerConfig(187, CSP*4, -221, 0, -45, 0, new String[] { "001ec0f4f4e0" }), 
new TowerConfig(212, CSP*4, -229, 0, -45, 0, new String[] { "5410ecf5560b" }), //top arch
new TowerConfig(238, CSP*4, -238, 0, -45, 0, new String[] { "5410ecf5c417" }),
new TowerConfig(246, CSP*3, -255, 0, -45, 0, new String[] { "1346" }),
new TowerConfig(238, CSP*1, -272, 0, -45, 0, new String[] { "938","787" }),
new TowerConfig(263, CSP*1, -263, 0, -45, 0, new String[] { "5410ecf56e83","1217" }),
new TowerConfig(238, CSP*0, -263, 0, -45, 0, new String[] { "d8803962b0e7" }),
new TowerConfig(263, CSP*0, -263, 0, -45, 0, new String[] { "5410ecf4a773" }),


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
