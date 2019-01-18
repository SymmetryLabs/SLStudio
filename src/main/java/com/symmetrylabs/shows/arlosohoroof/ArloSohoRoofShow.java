package com.symmetrylabs.shows.arlosohoroof;

import java.util.*;

import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.shows.HasWorkspace;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.arlo.ArloFaderLimiter;
import com.symmetrylabs.shows.arlo.ArloXfadeTimer;
import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.slstudio.model.SLModel;

import com.symmetrylabs.slstudio.workspaces.Workspace;
import heronarts.lx.transform.LXTransform;

/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class ArloSohoRoofShow extends CubesShow implements Show, HasWorkspace {
    public static final String SHOW_NAME = "arlosohoroof";

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
         * From the Prespective of the Front Door -------------------------------------------------------*
         */
        //BackRight Left
        new TowerConfig(CubesModel.Cube.Type.HD, 0, SP*4, 0, -45, 180-45, 0, new String[] {"1069","1068"}),
        //BackRight Center
        new TowerConfig(CubesModel.Cube.Type.HD, SP*3, SP*4, SP*-1, -45, 180-45, 0, new String[] {"143","150"}),
        //BackRight Right
        new TowerConfig(CubesModel.Cube.Type.HD, SP*5, SP*4, SP*0, -45, -180-45+90, 0, new String[] {"966","500"}),
        //Center Near Door
        new TowerConfig(CubesModel.Cube.Type.HD, SP*4.8f, SP*4, SP*-3, -45, 180-45, 0, new String[] {"614","636"}),

        //BackLeft Facing Couch Right
        new TowerConfig(CubesModel.Cube.Type.HD, SP*0, SP*4, SP*-6, -45, 180-45, 0, new String[] {"5410ecfd752a","1089"}),
        //BackLeft Facing Couch Middle
        new TowerConfig(CubesModel.Cube.Type.HD, SP*3, SP*4, SP*-6, -45, 180-45, 0, new String[] {"415hp","1050"}),
        //BackLeft Facing Couch Left
        new TowerConfig(CubesModel.Cube.Type.HD, SP*5, SP*4, SP*-6, -45, 180-45, 0, new String[] {"5410ecf5e2e2","5410ecf6b63e"}),

        //Tower Cube B
        new TowerConfig(CubesModel.Cube.Type.HD, SP*5, SP*0, SP*-6, 0, 0, 0, new String[] {"135","312"}),
        //Tower Cube M
        new TowerConfig(CubesModel.Cube.Type.HD, SP*5, SP*1, SP*-6, 0, 0, 0, new String[] {"5410ecfd52a4","5410ecfd7ce6"}),
        //Tower Cube T
        new TowerConfig(CubesModel.Cube.Type.HD, SP*5, SP*2, SP*-6, 0, 0, 0, new String[] {"683","1002"}),

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

        workspace = new Workspace(lx, ui, "shows/arlo");
        workspace.setRequestsBeforeSwitch(2);

        ArloXfadeTimer.attach(lx, ui);
        ArloFaderLimiter.attach(lx);
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }
}
