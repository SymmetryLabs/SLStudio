package com.symmetrylabs.shows.arlo;

import java.util.*;

import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.shows.HasWorkspace;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.showplugins.FaderLimiter;
import com.symmetrylabs.slstudio.showplugins.XfadeTimer;
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

        // Middle Cubes Left To Right
        new TowerConfig(CubesModel.Cube.Type.HD, SP*-4, SP * 1, 0, 0, -45+90, 0, new String[] {"797","459"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*-2, SP*1, SP*0, 0, -45+90, 0, new String[] {"900","907"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*0, SP*1, SP*0, 0, -45+90, 0, new String[] {"1133","1139"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*2, SP*1, SP*0, 0, -45+90, 0, new String[] {"1135","1132"}),
        new TowerConfig(CubesModel.Cube.Type.HD, SP*4, SP*1, SP*0, 0, -45+90, 0, new String[] {"1128","1130"}),

    //       //Right Towers Left To Right-------------------------------------------------------------------------*
                new TowerConfig(CubesModel.Cube.Type.HD, SP*5.5f, SP*0, SP*-2, 0, 45+90, 0, new String[] {"1122","1120"}),
                new TowerConfig(CubesModel.Cube.Type.HD, SP*5.5f, SP*1, SP*-2, 0, 0+90, 0, new String[] {"1134","1121"}),

                new TowerConfig(CubesModel.Cube.Type.HD, SP*5.5f, SP*2, SP*-3, 0, 45+90, 0, new String[] {"1138","1149"}),

                new TowerConfig(CubesModel.Cube.Type.HD, SP*5.5f, SP*0, SP*-4.1f, 0, 45+90, 0, new String[] {"1127","1147"}),
                new TowerConfig(CubesModel.Cube.Type.HD, SP*5.5f, SP*1, SP*-4.1f, 0, 0+90, 0, new String[] {"1136","1123"}),

                new TowerConfig(CubesModel.Cube.Type.HD, SP*5.5f, SP*2, SP*-5f, 0, 45+90, 0, new String[] {"1053","1062"}),

                new TowerConfig(CubesModel.Cube.Type.HD, SP*5.5f, SP*0, SP*-5.8f, 0, 45+90, 0, new String[] {"196","1088"}),
                new TowerConfig(CubesModel.Cube.Type.HD, SP*5.5f, SP*1, SP*-5.8f, 0, 0+90, 0, new String[] {"1086","1088"}),

                new TowerConfig(CubesModel.Cube.Type.HD, SP*5.5f, SP*0, SP*-7.2f, 0, 45+90, 0, new String[] {"1140","1191"}),
                new TowerConfig(CubesModel.Cube.Type.HD, SP*5.2f, SP*1, SP*-7.2f, 0, 45+90, 0, new String[] {"1144","1137"}),

        // //Left Towers Right To Left-------------------------------------------------------------------------*
        new TowerConfig(CubesModel.Cube.Type.HD, SP*-6, SP*0, SP*-1, 0, -90+90, 0, new String[] {"1148","1120"}),
                new TowerConfig(CubesModel.Cube.Type.HD, SP*-6, SP*1, SP*-1, 0, -45+90, 0, new String[] {"1146","1124"}),
                new TowerConfig(CubesModel.Cube.Type.HD, SP*-6, SP*2, SP*-1, 0, -90+90, 0, new String[] {"572","1143"}),

                new TowerConfig(CubesModel.Cube.Type.HD, SP*-7.5f, SP*0, SP*-2.5f, 0, -90+90, 0, new String[] {"0","1129"}),
                new TowerConfig(CubesModel.Cube.Type.HD, SP*-7.5f, SP*1, SP*-2.5f, 0, -45+90, 0, new String[] {"1125","1141"}),
                new TowerConfig(CubesModel.Cube.Type.HD, SP*-7.5f, SP*2, SP*-2.5f, 0, -90+90, 0, new String[] {"590","640"}),

                new TowerConfig(CubesModel.Cube.Type.HD, SP*-9, SP*0, SP*-4, 0, -90+90, 0, new String[] {"1142","1131"}),
                new TowerConfig(CubesModel.Cube.Type.HD, SP*-9, SP*1, SP*-4, 0, -45+90, 0, new String[] {"308","159"}),
                new TowerConfig(CubesModel.Cube.Type.HD, SP*-9, SP*2, SP*-4, 0, -90+90, 0, new String[] {"1150","1145"}),



/*
 5410ecf56e84
 5410ecf5e4d7
 5410ecf5e5aa
 5410ecf5fd6b
 5410ecf6272a
 5410ecf65045
 5410ecf67178
 5410ecf671ca
 5410ec8fc7
 5410ecf6a9b4
 5410ecfd512c
 5410ecfd5554
 5410ecfd58fb
 5410ecfd5d77


*/

//         new TowerConfig(CubesModel.Cube.Type.HD, SP*0, SP*0, SP*0, 0, 0, 0, new String[] {"415hp","1050"}),
//         new TowerConfig(CubesModel.Cube.Type.HD, SP*0, SP*0, SP*0, 0, 0, 0, new String[] {"5410ecf5e2e2","5410ecf6b63e"}),

//     new TowerConfig(CubesModel.Cube.Type.HD, SP*5, SP*0, SP*-6, 0, 0, 0, new String[] {"135","312"}),

// new TowerConfig(CubesModel.Cube.Type.HD, SP*5, SP*1, SP*-6, 0, 0, 0, new String[] {"5410ecfd52a4","5410ecfd7ce6"}),

// new TowerConfig(CubesModel.Cube.Type.HD, SP*5, SP*2, SP*-6, 0, 0, 0, new String[] {"683","1002"}),

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

            CubesModel.DoubleControllerCube cube = new CubesModel.DoubleControllerCube(config.ids[0], x, y, z, xRot, yRot, zRot, globalTransform);
            cubes.add(cube);
            allCubes.add(cube);

            towers.add(new CubesModel.Tower("", cubes));
        }
        /*-----------------------------------------------------------------*/

        CubesModel.Cube[] allCubesArr = new CubesModel.Cube[allCubes.size()];
        for (int i = 0; i < allCubesArr.length; i++) {
            allCubesArr[i] = allCubes.get(i);
        }

        return new CubesModel(SHOW_NAME, towers, allCubesArr, cubeInventory, mapping);
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
      super.setupUi(lx, ui);

        workspace = new Workspace(lx, ui, "shows/arlo");
        workspace.setRequestsBeforeSwitch(2);

        XfadeTimer.attach(lx, ui);
        FaderLimiter.attach(lx);
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public String getShowName() {
        return SHOW_NAME;
    }
}
