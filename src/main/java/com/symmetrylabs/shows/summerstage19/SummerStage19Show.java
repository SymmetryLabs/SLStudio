package com.symmetrylabs.shows.summerstage19;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.shows.cubes.UICubesMappingPanel;
import com.symmetrylabs.shows.cubes.UICubesOutputs;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.transform.LXTransform;
import heronarts.p3lx.ui.UI2dScrollContext;

import java.util.ArrayList;
import java.util.List;

/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class SummerStage19Show extends CubesShow {
    public static final String SHOW_NAME = "summerstage19";

    static final float CUBE_WIDTH = 24;
    static final float CUBE_HEIGHT = 24;
    static final float TOWER_WIDTH = 24;
    static final float TOWER_HEIGHT = 24;
    static final float CUBE_SPACING = 1.5f;

    static final float TOWER_VERTICAL_SPACING = 0;
    static final float TOWER_RISER = 14;
    static final float SP = CUBE_WIDTH + CUBE_SPACING;
    static final float JUMP = TOWER_HEIGHT+TOWER_VERTICAL_SPACING;

    static final float INCHES_PER_METER = 39.3701f;

    private static final ClusterConfig[] clusters = new ClusterConfig[] {
        new ClusterConfig("L1", 0, 5, 6,
            new CubeConfig("A1.5", 0, 0.5, 0),
            new CubeConfig("B1", 1, 0, 0),
            new CubeConfig("B2", 1, 1, 0),
            new CubeConfig("B3", 1, 2, 0)),
        new ClusterConfig("L2", 2, 5, 7,
            new CubeConfig("A1", 0, 0, 0),
            new CubeConfig("A2", 0, 1, 0),
            new CubeConfig("A3", 0, 2, 0),
            new CubeConfig("A4", 0, 3, 0),
            new CubeConfig("A5", 0, 4, 0)),
        new ClusterConfig("L3", 3, 4, 7,
            new CubeConfig("A1", 0, 0, 0),
            new CubeConfig("A2", 0, 1, 0),
            new CubeConfig("A3", 0, 2, 0),
            new CubeConfig("A4", 0, 3, 0)),
        new ClusterConfig("L4", 4, 5, 7,
            new CubeConfig("A1", 0, 0, 0),
            new CubeConfig("A2", 0, 1, 0),
            new CubeConfig("A3", 0, 2, 0),
            new CubeConfig("A4", 0, 3, 0),
            new CubeConfig("A5", 0, 4, 0)),
        new ClusterConfig("L5", 4, 1, 7,
            new CubeConfig("A1", 0, 0, 0),
            new CubeConfig("A2", 0, 1, 0),
            new CubeConfig("A3", 0, 2, 0),
            new CubeConfig("A4", 0, 3, 0)),
        new ClusterConfig("L6", 5, 4, 7,
            new CubeConfig("A1", 0, 0, 0),
            new CubeConfig("A2", 0, 1, 0),
            new CubeConfig("A3", 0, 2, 0),
            new CubeConfig("A4", 0, 3, 0),
            new CubeConfig("A5", 0, 4, 0)),
        new ClusterConfig("L7", 5, 0, 7,
            new CubeConfig("A1", 0, 0, 0),
            new CubeConfig("A2", 0, 1, 0),
            new CubeConfig("A3", 0, 2, 0),
            new CubeConfig("A4", 0, 3, 0)),
        new ClusterConfig("L8", 6, 6, 7,
            new CubeConfig("A1", 0, 0, 0),
            new CubeConfig("A2", 0, 1, 0),
            new CubeConfig("A3", 0, 2, 0)),
        new ClusterConfig("L9", 6, 0.5, 7,
            new CubeConfig("A1", 0, 0, 0),
            new CubeConfig("A2", 0, 1, 0),
            new CubeConfig("A3", 0, 2, 0),
            new CubeConfig("A4", 0, 3, 0)),
    };

    static class ClusterConfig {
        final String id;
        final float x;
        final float y;
        final float z;
        final CubeConfig[] configs;

        // these are doubles so we can use numeric literals without the f
        ClusterConfig(String id, double x, double y, double z, CubeConfig... configs) {
            this.id = id;
            this.x = (float) x;
            this.y = (float) y;
            this.z = (float) z;
            this.configs = configs;
        }
    }

    static class CubeConfig {
        final String baseModelId;
        final float x;
        final float y;
        final float z;

        CubeConfig(String baseModelId, double x, double y, double z) {
            this.baseModelId = baseModelId;
            this.x = (float) x;
            this.y = (float) y;
            this.z = (float) z;
        }
    }

    public SLModel buildModel() {
        LXTransform globalTransform = new LXTransform();
        // we're abusing tower to mean cluster, oh well
        List<CubesModel.Tower> towers = new ArrayList<>();
        List<CubesModel.Cube> allCubes = new ArrayList<>();

        for (ClusterConfig cluster : clusters) {
            List<CubesModel.Cube> clusterCubes = new ArrayList<>();

            globalTransform.push();
            globalTransform.translate(SP * cluster.x, SP * cluster.y, SP * cluster.z);

            for (CubeConfig config : cluster.configs) {
                float x = SP * config.x;
                float y = SP * config.y;
                float z = SP * config.z;
                CubesModel.DoubleControllerCube cube = new CubesModel.DoubleControllerCube(
                    cluster.id + "-" + config.baseModelId,
                    x, y, z, 0, 0, 0, globalTransform);
                clusterCubes.add(cube);
                allCubes.add(cube);
            }

            globalTransform.pop();
            towers.add(new CubesModel.Tower(cluster.id, clusterCubes));
        }

        CubesModel.Cube[] allCubesArr = new CubesModel.Cube[allCubes.size()];
        allCubes.toArray(allCubesArr);

        CubesModel model = new CubesModel(towers, allCubesArr, cubeInventory, mapping);
        model.setTopologyTolerances(6, 6, 8);
        return model;
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        UI2dScrollContext utility = ui.rightPane.utility;
        new UICubesOutputs(lx, ui, this, 0, 0, utility.getContentWidth()).addToContainer(utility);
        new UICubesMappingPanel(lx, ui, 0, 0, utility.getContentWidth()).addToContainer(utility);
    }

    @Override
    public String getShowName() {
        return SHOW_NAME;
    }
}
