package com.symmetrylabs.shows.arlo;

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
public class ArloShow extends CubesShow implements Show, HasWorkspace {
public static final String SHOW_NAME = "arlo";

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
        new TowerConfig(CubesModel.Cube.Type.HD, SP*3, SP*4, SP*-1, -45, 180-45, 0, new String[] {"143","150"}),        // new TowerConfig(CubesModel.Cube.Type.HD, SP*-0.75f, 0, SP*-1.5f, new String[] {"996","997"}),
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

        // /**
        //  * Layer B -----------------------------------------------------------------------------------*
        // */
        // // B1
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*-1, TOWER_RISER, -SP,  new String[] {"1111","1112"}),
        // // B2
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*-0.5f, TOWER_RISER, 0,  new String[] {"1109","1108"}),
        // // (gap)
        // // B3
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*4.5f+2, TOWER_RISER, 0,  new String[] {"654","1083"}),
        // // (gap)
        // // B4
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*6.5f+2, TOWER_RISER, 0,  new String[] {"942","930"}),
        // // (gap)
        // // B5 hinge
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*11.5f+3, TOWER_RISER, 0,  new String[] {"1241","1240"}),
        // // B6
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*12f+3, TOWER_RISER, -SP,  new String[] {"918","917"}),


        // /**
        //  * Layer C -----------------------------------------------------------------------------------*
        // */
        // // C1
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*0-6, TOWER_RISER*2, 0, new String[] {"924","923"}),
        // // (gap)
        // // C2
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*4.5f+2, TOWER_RISER*2, 0, new String[] {"929","995"}),
        // // C3
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*5.5f+2, TOWER_RISER*2, 0, new String[] {"1027","612"}),
        // // C4
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*6.5f+2, TOWER_RISER*2, 0, new String[] {"771","770"}),
        // // (gap)
        // // C5
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*11f+9, TOWER_RISER*2, 0, new String[] {"940","948"}),

        // /**
        //  * Layer D -----------------------------------------------------------------------------------*
        // */
        // // D1
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*0, TOWER_RISER*3, 0, new String[] {"919","910"}),
        // // (gap)
        // // D2
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*1.5f, TOWER_RISER*3, 0, new String[] {"1067","1066"}),
        // // D3
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*2.5f, TOWER_RISER*3, 0, new String[] {"957","956"}),
        // // D4
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*3.5f, TOWER_RISER*3, 0, new String[] {"604","1087"}),
        // // (gap)
        // // D5
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*5.5f+3, TOWER_RISER*3, 0, new String[] {"1009","1008"}),
        // // D6
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*6.5f+3, TOWER_RISER*3, 0, new String[] {"691","690"}),
        // // D7
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*7.5f+3, TOWER_RISER*3, 0, new String[] {"906","912"}),
        // // D8
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*8.5f+3, TOWER_RISER*3, 0, new String[] {"926","998"}),
        // // D9
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*9.5f+3, TOWER_RISER*3, 0, new String[] {"641","978"}),
        // // (gap)
        // // D10
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*11f+3, TOWER_RISER*3, 0, new String[] {"1041","1042"}),

        // /**
        //  * Layer E -----------------------------------------------------------------------------------*
        // */
        // // E1
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*0.0f,TOWER_RISER*4, 0, new String[] {"481","480"}),
        // // E2
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*1.0f,TOWER_RISER*4, 0, new String[] {"928","918"}),
        // // (gap)
        // // E3
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*3.0f+3,TOWER_RISER*4, 0, new String[] {"1047","883"}),
        // // E4
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*4.0f+3,TOWER_RISER*4, 0, new String[] {"1075","593"}),
        // // E5
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*5.0f+3,TOWER_RISER*4, 0, new String[] {"960","974"}),
        // // E6
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*6.0f+3,TOWER_RISER*4, 0, new String[] {"905","955"}),
        // // E7
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*7.0f+3,TOWER_RISER*4, 0, new String[] {"441","440"}),
        // // E8
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*8.0f+3,TOWER_RISER*4, 0, new String[] {"495","494"}),
        // // E9
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*9.0f+3,TOWER_RISER*4, 0, new String[] {"1065","1074"}),
        // // E10
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*10.0f+3,TOWER_RISER*4, 0, new String[] {"759","547"}),
        // // E11
        // new TowerConfig(CubesModel.Cube.Type.HD, SP*11.0f+3,TOWER_RISER*4, 0, new String[] {"949","946"}),

        // /**
        //  * Layer F -----------------------------------------------------------------------------------*
        // */
        // // F1
        // new TowerConfig(CubesModel.Cube.Type.HD,  SP*1.5f, TOWER_RISER*5, 0, new String[] {"713","712"}),
        // // F2
        // new TowerConfig(CubesModel.Cube.Type.HD,  SP*2.5f, TOWER_RISER*5, 0, new String[] {"763","742"}),
        // // F3
        // new TowerConfig(CubesModel.Cube.Type.HD,  SP*3.5f, TOWER_RISER*5, 0, new String[] {"944","947"}),
        // // (gap)
        // // F4
        // new    TowerConfig(CubesModel.Cube.Type.HD,  SP*5.5f, TOWER_RISER*5, 0, new String[] {"516","1101"}),
        // // F5
        // new    TowerConfig(CubesModel.Cube.Type.HD,  SP*6.5f, TOWER_RISER*5, 0, new String[] {"727","726"}),
        // // (gap)
        // // F6
        // new    TowerConfig(CubesModel.Cube.Type.HD,  SP*9f-3, TOWER_RISER*5, 0, new String[] {"927","915"}),
        // // F7
        // new    TowerConfig(CubesModel.Cube.Type.HD,  SP*10f-3, TOWER_RISER*5, 0, new String[] {"1099","1098"}),

        // /**
        //  * Layer G -----------------------------------------------------------------------------------*
        // */
        // // G1
        // new TowerConfig(CubesModel.Cube.Type.HD,  SP*3+3f, TOWER_RISER*6, 0, new String[] {"889","119"}),
        // // G2
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

            CubesModel.DoubleControllerCube cube = new CubesModel.DoubleControllerCube(config.ids[0], config.ids[1], x, y, z, xRot, yRot, zRot, globalTransform);
            cubes.add(cube);
            allCubes.add(cube);

            towers.add(new CubesModel.Tower("", cubes));
        }
        /*-----------------------------------------------------------------*/

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
