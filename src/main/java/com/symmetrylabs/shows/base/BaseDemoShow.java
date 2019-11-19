package com.symmetrylabs.shows.base;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.shows.cubes.CubesShow.TowerConfig;
import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.model.LXModel;
import heronarts.lx.transform.LXTransform;

import java.util.ArrayList;
import java.util.List;

import static com.symmetrylabs.shows.cubes.CubesShow.JUMP;

/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class BaseDemoShow extends SLShow implements Show {
    public static String SHOW_NAME = "baseDemo";

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = -45;
    static final float globalRotationZ = 0;

    private static final float TOWER_RISER = 0;
//    static final TowerConfig[] TOWER_CONFIG = new TowerConfig[]{
//        // group 1
//        new TowerConfig(0, 0, 48, new String[]{"92", "203", "64"}),
//        new TowerConfig(0, 0, 0, new String[]{"87", "351", "366"}),
//        new TowerConfig(-24, TOWER_RISER, 12, new String[]{"22", "10"}),
//        new TowerConfig(12, JUMP + TOWER_RISER, -25, new String[]{"85"}),
//        new TowerConfig(12, JUMP + TOWER_RISER, 25, new String[]{"94"}),
//
//        // group 2
//        new TowerConfig(25, TOWER_RISER, 11, new String[]{"115"}),
//        new TowerConfig(26, 2 * JUMP + TOWER_RISER, 36, new String[]{"15"}),
//        new TowerConfig(51, 0, 24, new String[]{"63", "314", "71", "43"}),
//
//        // group 3
//        new TowerConfig(38, 0, -14, new String[]{"70", "337", "76", "31"}),
//        new TowerConfig(63, TOWER_RISER, -2, new String[]{"182"}),
//        new TowerConfig(50, TOWER_RISER, -39, new String[]{"137"}),
//
//        // group 4
//        new TowerConfig(88, 0, -14, new String[]{"17", "356", "132", "83"}),
//        new TowerConfig(76, JUMP + TOWER_RISER, 12, new String[]{"52"}),
//        new TowerConfig(63, 2 * JUMP + TOWER_RISER, -26, new String[]{"11"}),
//        new TowerConfig(100, TOWER_RISER, -39, new String[]{"56"}),
//        new TowerConfig(113, 2 * JUMP + TOWER_RISER, -26, new String[]{"352"}),
//
//        // group 5
//        new TowerConfig(88, 0, -65, new String[]{"120", "68", "54", "46"}),
//        new TowerConfig(63, JUMP + TOWER_RISER, -53, new String[]{"181"}),
//        new TowerConfig(76, TOWER_RISER, -90, new String[]{"38"}),
//        new TowerConfig(100, 2 * JUMP + TOWER_RISER, -90, new String[]{"62"}),
//
//        // group 6
//        new TowerConfig(125, 0, -78, new String[]{"7", "127", "86"}),
//        new TowerConfig(113, JUMP + TOWER_RISER, -53, new String[]{"320"}),
//
//        // group 7
//        new TowerConfig(112, 0, -115, new String[]{"90", "121", "32"}),
//        new TowerConfig(87, JUMP + TOWER_RISER, -103, new String[]{"156"})
//
//        // group 7
////        new TowerConfig(124, TOWER_RISER, -139, new String[] {"82", "4"}),
////
////
////        // other side
////        new TowerConfig(124, TOWER_RISER, -RIGHT_SIDE_SPACING, -90, new String[] {"383"}),
////        new TowerConfig(124, JUMP*2+TOWER_RISER, -RIGHT_SIDE_SPACING, -90, new String[] {"358"}),
////
////        new TowerConfig(112, 0, -RIGHT_SIDE_SPACING-24, -90, new String[] {"91", "40", "340", "360"}),
////
////        new TowerConfig(88, JUMP+TOWER_RISER, -RIGHT_SIDE_SPACING-12, -90, new String[] {"172"}),
////
////        new TowerConfig(100, JUMP*2+TOWER_RISER, -RIGHT_SIDE_SPACING-48, -90, new String[] {"57"}),
////
////        new TowerConfig(64, 0, -RIGHT_SIDE_SPACING-24, -90, new String[] {"390", "408", "185"}),
////
////        new TowerConfig(52, TOWER_RISER, -RIGHT_SIDE_SPACING-48, -90, new String[] {"369"}),
////        new TowerConfig(52, JUMP*2+TOWER_RISER, -RIGHT_SIDE_SPACING-48, -90, new String[] {"398"}),
////
////        new TowerConfig(76, 0, -RIGHT_SIDE_SPACING-60, -90, new String[] {"326", "128", "391", "341"}),
////
////        new TowerConfig(28, 0, -RIGHT_SIDE_SPACING-60, -90, new String[] {"129", "13", "77"})
//    };


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
        List<CubesModel.Cube> cubes = new ArrayList<>();
//        for (TowerConfig config : TOWER_CONFIG) {
//            List<CubesModel.Cube> cubes = new ArrayList<>();
//            float x = config.x;
//            float z = config.z;
//            float xRot = config.xRot;
//            float yRot = config.yRot;
//            float zRot = config.zRot;
//            CubesModel.Cube.Type type = config.type;
//
//            for (int i = 0; i < config.ids.length; i++) {
//                float y = config.yValues[i];
//                CubesModel.Cube cube = new CubesModel.Cube(config.ids[i], x, y, z, xRot, yRot, zRot, globalTransform, type);
//                cubes.add(cube);
//                allCubes.add(cube);
//            }
//            towers.add(new CubesModel.Tower("", cubes));
//        }
        /*-----------------------------------------------------------------*/

        CubesModel.Cube[] allCubesArr = new CubesModel.Cube[allCubes.size()];
        for (int i = 0; i < allCubesArr.length; i++) {
            allCubesArr[i] = allCubes.get(i);
        }

//        return new SLModel(SHOW_NAME, towers, allCubesArr, cubeInventory, mapping);
//        return new SLModel(SHOW_NAME, towers, allCubesArr, cubeInventory, mapping);
        return new SLModel(SHOW_NAME, new LXModel("baseDemo")); // fix this later.
    }

    @Override
    public String getShowName() {
        return SHOW_NAME;
    }
}
