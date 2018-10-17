package com.symmetrylabs.shows.penfoldswine;

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
public class PenfoldsWineShow extends CubesShow implements Show, HasWorkspace {
public static final String SHOW_NAME = "penfoldswine";

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
         * Layer A -----------------------------------------------------------------------------------*
        */
        // A1
        new TowerConfig(SP*-1.75f, 0, SP*-1.5f, new String[] {""}),
        // A2
        new TowerConfig(SP*-0.75f, 0, SP*-2.5f, new String[] {""}),
        // A3
        new TowerConfig(SP*-0.75f, 0, SP*-1.5f, new String[] {""}),
        // A4
        new TowerConfig(SP*-0.75f, 0, SP*-0.5f, new String[] {""}),
        // A5
        new TowerConfig(SP*12f+9, 0, SP*-2.5f, new String[] {""}),
        // A6
        new TowerConfig(SP*12.5f+3, 0, SP*-1.5f, new String[] {""}),
        // A7
        new TowerConfig(SP*12f+9, 0, SP*-0.5f, new String[] {""}),


        /**
         * Layer B -----------------------------------------------------------------------------------*
        */
        // B1
        new TowerConfig(SP*-1, TOWER_RISER, -SP,  new String[] {""}),
        // B2
        new TowerConfig(SP*-0.5f, TOWER_RISER, 0,  new String[] {""}),
        // (gap)
        // B3
        new TowerConfig(SP*4.5f+2, TOWER_RISER, 0,  new String[] {""}),
        // (gap)
        // B4
        new TowerConfig(SP*6.5f+2, TOWER_RISER, 0,  new String[] {""}),
        // (gap)
        // B5
        new TowerConfig(SP*11.5f+3, TOWER_RISER, 0,  new String[] {""}),
        // B6
        new TowerConfig(SP*12f+3, TOWER_RISER, -SP,  new String[] {""}),


        /**
         * Layer C -----------------------------------------------------------------------------------*
        */
        // C1
        new TowerConfig(SP*0-6, TOWER_RISER*2, 0, new String[] {""}),
        // (gap)
        // C2
        new TowerConfig(SP*4.5f+2, TOWER_RISER*2, 0, new String[] {""}),
        // C3
        new TowerConfig(SP*5.5f+2, TOWER_RISER*2, 0, new String[] {""}),
        // C4
        new TowerConfig(SP*6.5f+2, TOWER_RISER*2, 0, new String[] {""}),
        // (gap)
        // C5
        new TowerConfig(SP*11f+9, TOWER_RISER*2, 0, new String[] {""}),

        /**
         * Layer D -----------------------------------------------------------------------------------*
        */
        // D1
        new TowerConfig(SP*0, TOWER_RISER*3, 0, new String[] {""}),
        // (gap)
        // D2
        new TowerConfig(SP*1.5f, TOWER_RISER*3, 0, new String[] {""}),
        // D3
        new TowerConfig(SP*2.5f, TOWER_RISER*3, 0, new String[] {""}),
        // D4
        new TowerConfig(SP*3.5f, TOWER_RISER*3, 0, new String[] {""}),
        // (gap)
        // D5
        new TowerConfig(SP*5.5f+3, TOWER_RISER*3, 0, new String[] {""}),
        // D6
        new TowerConfig(SP*6.5f+3, TOWER_RISER*3, 0, new String[] {""}),
        // D7
        new TowerConfig(SP*7.5f+3, TOWER_RISER*3, 0, new String[] {""}),
        // D8
        new TowerConfig(SP*8.5f+3, TOWER_RISER*3, 0, new String[] {""}),
        // D9
        new TowerConfig(SP*9.5f+3, TOWER_RISER*3, 0, new String[] {""}),
        // (gap)
        // D10
        new TowerConfig(SP*11f+3, TOWER_RISER*3, 0, new String[] {""}),

        /**
         * Layer E -----------------------------------------------------------------------------------*
        */
        // E1
        new TowerConfig(SP*0.0f,TOWER_RISER*4, 0, new String[] {""}),
        // E2
        new TowerConfig(SP*1.0f,TOWER_RISER*4, 0, new String[] {""}),
        // (gap)
        // E3
        new TowerConfig(SP*3.0f+3,TOWER_RISER*4, 0, new String[] {""}),
        // E4
        new TowerConfig(SP*4.0f+3,TOWER_RISER*4, 0, new String[] {""}),
        // E5
        new TowerConfig(SP*5.0f+3,TOWER_RISER*4, 0, new String[] {""}),
        // E6
        new TowerConfig(SP*6.0f+3,TOWER_RISER*4, 0, new String[] {""}),
        // E7
        new TowerConfig(SP*7.0f+3,TOWER_RISER*4, 0, new String[] {""}),
        // E8
        new TowerConfig(SP*8.0f+3,TOWER_RISER*4, 0, new String[] {""}),
        // E9
        new TowerConfig(SP*9.0f+3,TOWER_RISER*4, 0, new String[] {""}),
        // E10
        new TowerConfig(SP*10.0f+3,TOWER_RISER*4, 0, new String[] {""}),
        // E11
        new TowerConfig(SP*11.0f+3,TOWER_RISER*4, 0, new String[] {""}),

        /**
         * Layer F -----------------------------------------------------------------------------------*
        */
        // F1
        new TowerConfig( SP*1.5f, TOWER_RISER*5, 0, new String[] {""}),
        // F2
        new TowerConfig( SP*2.5f, TOWER_RISER*5, 0, new String[] {""}),
        // F3
        new TowerConfig( SP*3.5f, TOWER_RISER*5, 0, new String[] {""}),
        // (gap)
        // F4
        new    TowerConfig( SP*5.5f, TOWER_RISER*5, 0, new String[] {""}),
        // F5
        new    TowerConfig( SP*6.5f, TOWER_RISER*5, 0, new String[] {""}),
        // (gap)
        // F6
        new    TowerConfig( SP*9f-3, TOWER_RISER*5, 0, new String[] {""}),
        // F7
        new    TowerConfig( SP*10f-3, TOWER_RISER*5, 0, new String[] {""}),
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

        return new CubesModel(towers, allCubesArr);
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
      super.setupUi(lx, ui);

        workspace = new Workspace(lx, ui, "shows/penfoldswine");
        workspace.setRequestsBeforeSwitch(2);
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }
}
