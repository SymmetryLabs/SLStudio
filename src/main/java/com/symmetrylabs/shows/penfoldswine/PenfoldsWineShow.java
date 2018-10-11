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
        // group 1
        new TowerConfig(48-175, 0, -144, 0, 0, 0, new String[] {""}),
        new TowerConfig(72-175, 0, -168, 0, 0, 0, new String[] {""}),
        new TowerConfig(72-175, 0, -144, 0, 0, 0, new String[] {"" }),
        new TowerConfig(72-175, 0, -120, 0, 0, 0, new String[] {"" }),
        new TowerConfig(72-175, TOWER_RISER, -120, 0, 0, 0, new String[] {"" }),
        new TowerConfig(78-175, TOWER_RISER*2, -111, 0, 0, 0, new String[] {"" }),
        new TowerConfig(87-175, TOWER_RISER*3, -114, 0, 0, 0, new String[] {"" }),    
        new TowerConfig(99-175, TOWER_RISER*4, -117, 0, 0, 0, new String[] {"" }),
        new TowerConfig(123-175, TOWER_RISER*4, -117, 0, 0, 0, new String[] {"" }),
        //new TowerConfig(147-175, TOWER_RISER*3, -117, 0, 0, 0, new String[] {"" }),
        new TowerConfig(171-175, TOWER_RISER*4, -117, 0, 0, 0, new String[] {"" }),
            new TowerConfig(171-175-12-SP, TOWER_RISER*3, -117, 0, 0, 0, new String[] {"" }),
            new TowerConfig(171-175-12, TOWER_RISER*3, -117, 0, 0, 0, new String[] {"" }),
            new TowerConfig(171-175-12+SP, TOWER_RISER*3, -117, 0, 0, 0, new String[] {"" }),
        new TowerConfig(195-175, TOWER_RISER*4, -117, 0, 0, 0, new String[] {"" }),
        new TowerConfig(219-175, TOWER_RISER*4, -117, 0, 0, 0, new String[] {"" }),
        new TowerConfig(243-175, TOWER_RISER*4, -117, 0, 0, 0, new String[] {"" }),
        new TowerConfig(267-175, TOWER_RISER*4, -117, 0, 0, 0, new String[] {"" }),
            new TowerConfig(171-175-12+(SP*3), TOWER_RISER*3, -117, 0, 0, 0, new String[] {"" }),
                new TowerConfig(171-175-12+(SP*3), TOWER_RISER*2, -117, 0, 0, 0, new String[] {"" }),
                new TowerConfig(171-175-12+(SP*2), TOWER_RISER*2, -117, 0, 0, 0, new String[] {"" }),
                new TowerConfig(171-175-12+(SP*4), TOWER_RISER*2, -117, 0, 0, 0, new String[] {"" }),
                new TowerConfig(171-175-12+(SP*4), TOWER_RISER*1, -117, 0, 0, 0, new String[] {"" }),
                new TowerConfig(171-175-12+(SP*2), TOWER_RISER*1, -117, 0, 0, 0, new String[] {"" }),
            new TowerConfig(171-175-12+(SP*4), TOWER_RISER*3, -117, 0, 0, 0, new String[] {"" }),
            new TowerConfig(171-175-12+(SP*5), TOWER_RISER*3, -117, 0, 0, 0, new String[] {"" }),
            new TowerConfig(171-175-12+(SP*6), TOWER_RISER*3, -117, 0, 0, 0, new String[] {"" }),
        new TowerConfig(291-175, TOWER_RISER*4, -117, 0, 0, 0, new String[] {"" }),
        new TowerConfig(315-175, TOWER_RISER*4, -117, 0, 0, 0, new String[] {"" }),
        new TowerConfig(339-175, TOWER_RISER*4, -117, 0, 0, 0, new String[] {"" }),
        new TowerConfig(363-175, TOWER_RISER*4, -117, 0, 0, 0, new String[] {"" }),
            new TowerConfig(363-175+10, TOWER_RISER*3, -114, 0, 0, 0, new String[] {"" }),
            new TowerConfig(363-175+20, TOWER_RISER*2, -111, 0, 0, 0, new String[] {"" }),
        new TowerConfig(363-175+30, 0, -120, 0, 0, 0, new String[] {"" }),
        new TowerConfig(363-175+30, TOWER_RISER, -120, 0, 0, 0, new String[] {"" }),
        new TowerConfig(363-175+30, 0, -144, 0, 0, 0, new String[] {"" }),
        new TowerConfig(363-175+30, 0, -168, 0, 0, 0, new String[] {"" }),
            new TowerConfig(123-175+12, TOWER_RISER*5, -117, 0, 0, 0, new String[] {"" }),
            new TowerConfig(123-175+12+SP, TOWER_RISER*5, -117, 0, 0, 0, new String[] {"" }),
            new TowerConfig(123-175+12+(SP*2), TOWER_RISER*5, -117, 0, 0, 0, new String[] {"" }),
            new    TowerConfig(123-175+12+(SP*4), TOWER_RISER*5, -117, 0, 0, 0, new String[] {"" }),
            new    TowerConfig(123-175+12+(SP*5), TOWER_RISER*5, -117, 0, 0, 0, new String[] {"" }),
            new    TowerConfig(123-175+12+(SP*7.5f), TOWER_RISER*5, -117, 0, 0, 0, new String[] {"" }),
            new    TowerConfig(123-175+12+(SP*8.5f), TOWER_RISER*5, -117, 0, 0, 0, new String[] {"" }),
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
        workspace = new Workspace(lx, ui, "shows/penfoldswine");
        workspace.setRequestsBeforeSwitch(2);
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }
}
