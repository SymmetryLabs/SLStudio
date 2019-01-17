package com.symmetrylabs.shows.mae;

import java.util.*;

import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.shows.HasWorkspace;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.slstudio.model.SLModel;

import com.symmetrylabs.slstudio.workspaces.Workspace;
import heronarts.lx.transform.LXTransform;

/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class MaeShow extends CubesShow implements Show, HasWorkspace {
    public static final String SHOW_NAME = "mae";

    private Workspace workspace;

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;
    static final float TOWER_RISER = 24;
    static final float SP = 24;

    static final TowerConfig[] TOWER_CONFIG = {
/**
         * Middle Twins -----------------------------------------------------------------------------------*
        */
        // A1
        new TowerConfig(CubesModel.Cube.Type.HD, SP*0, 0, 0, new String[] {"520","876"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*0, SP*1, 0, new String[] {"520","876"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*0, SP*2, 0, new String[] {"520","876"}),
        // A2
        new TowerConfig(CubesModel.Cube.Type.HD, SP*1, 0, 0, new String[] {"1210","1211"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*1, SP*1, 0, new String[] {"520","876"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*1, SP*2, 0, new String[] {"520","876"}),

        // /**
        //  * Left Triplets --------------------------------------------------------------------------------*
        // */
        // // B1
        new TowerConfig(CubesModel.Cube.Type.HD, SP*-2, SP*.5f, SP*-1, new String[] {"520","876"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*-2, SP*1.5f, SP*-1, new String[] {"520","876"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*-2, SP*2.5f, SP*-1, new String[] {"520","876"}),
        // // B2
        new TowerConfig(CubesModel.Cube.Type.HD, SP*-3, 0, SP*-1, new String[] {"520","876"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*-3, SP*1, SP*-1, new String[] {"520","876"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*-3, SP*2, 0, new String[] {"520","876"}),
        // // B3
        new TowerConfig(CubesModel.Cube.Type.HD, SP*-4, 0, SP*-1, new String[] {"520","876"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*-4, SP*1, SP*-1, new String[] {"520","876"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*-4, SP*2, SP*-1, new String[] {"520","876"}),

        // /**
        //  * Left Wing -----------------------------------------------------------------------------------*
        // */
        // // C1
        new TowerConfig(CubesModel.Cube.Type.HD, SP*-5, 0, SP*-2, new String[] {"520","876"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*-5, SP*1, SP*-2, new String[] {"520","876"}),

        // /**
        //  * Left Front -----------------------------------------------------------------------------------*
        // */
        // // D1
        new TowerConfig(CubesModel.Cube.Type.HD, SP*-2, 0, SP*-3, new String[] {"520","876"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*-2, SP*1, SP*-3, new String[] {"520","876"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*-2, SP*2, SP*-3, new String[] {"520","876"}),

        // /*Right Triplets --------------------------------------------------------------------------------*
        // */
        // // E1
        new TowerConfig(CubesModel.Cube.Type.HD, SP*3, SP*.5f, SP*-1, new String[] {"520","876"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*3, SP*1.5f, SP*-1, new String[] {"520","876"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*3, SP*2.5f, SP*-1, new String[] {"520","876"}),        
        // // E2
        new TowerConfig(CubesModel.Cube.Type.HD, SP*4, 0, SP*-1, new String[] {"520","876"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*4, SP*1, SP*-1, new String[] {"520","876"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*4, SP*2, 0, new String[] {"520","876"}),        // // (gap)
        // // E3
        new TowerConfig(CubesModel.Cube.Type.HD, SP*5, 0, SP*-1, new String[] {"520","876"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*5, SP*1, SP*-1, new String[] {"520","876"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*5, SP*2, SP*-1, new String[] {"520","876"}),        

        // /**
        //  * Right Wing -----------------------------------------------------------------------------------*
        // */
        // // F1
        new TowerConfig(CubesModel.Cube.Type.HD, SP*6, 0, SP*-2, new String[] {"520","876"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*6, SP*1, SP*-2, new String[] {"520","876"}),        

        // /**
        //  * Right Front -----------------------------------------------------------------------------------*
        // */
        // // G1
        new TowerConfig(CubesModel.Cube.Type.HD, SP*3, 0, SP*-3, new String[] {"520","876"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*3, SP*1, SP*-3, new String[] {"520","876"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*3, SP*2, SP*-3, new String[] {"520","876"}),        // // G2
        // new TowerConfig(CubesModel.Cube.Type.HD,  SP*4+3f, TOWER_RISER*6, 0, new String[] {"1051","1058"}),
        // // G3
        // new TowerConfig(CubesModel.Cube.Type.HD,  SP*5+3f, TOWER_RISER*6, 0, new String[] {"1014","891"}),
        // // (gap)
        // // G4
        // new    TowerConfig(CubesModel.Cube.Type.HD,  SP*6.5f+3f, TOWER_RISER*6, 0, new String[] {"1132","1133"}),
        // // G5
        // new    TowerConfig(CubesModel.Cube.Type.HD,  SP*7.5f, TOWER_RISER*6, 0, new String[] {"1224","1225"}),
    };


    public SLModel buildModel() {
        LXTransform globalTransform = new LXTransform();
        globalTransform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
        globalTransform.rotateX(globalRotationX * Math.PI / 180.);
        globalTransform.rotateY(globalRotationY * Math.PI / 180.);
        globalTransform.rotateZ(globalRotationZ * Math.PI / 180.);

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

            CubesModel.DoubleControllerCube cube =
                new CubesModel.DoubleControllerCube(
                    config.ids[0], config.ids[1], x, y, z, xRot, yRot, zRot, globalTransform);
            cubes.add(cube);
            allCubes.add(cube);

            towers.add(new CubesModel.Tower("", cubes));
        }

        CubesModel.Cube[] allCubesArr = new CubesModel.Cube[allCubes.size()];
        for (int i = 0; i < allCubesArr.length; i++) {
            allCubesArr[i] = allCubes.get(i);
        }

        return new CubesModel(towers, allCubesArr);
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
      super.setupUi(lx, ui);

        workspace = new Workspace(lx, ui, "shows/mae");
        workspace.setRequestsBeforeSwitch(2);


    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }
}
