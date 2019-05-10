package com.symmetrylabs.shows.summerstage19;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.shows.cubes.UICubesMappingPanel;
import com.symmetrylabs.shows.cubes.UICubesOutputs;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.transform.LXTransform;
import heronarts.p3lx.ui.UI2dScrollContext;
import heronarts.lx.LX;

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
        new ClusterConfig("L3", 3, 5, 7,
            new CubeConfig("A1", 0, 0, 0),
            new CubeConfig("A2", 0, 1, 0),
            new CubeConfig("A3", 0, 2, 0)),
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
        new ClusterConfig("L9", 6, 1, 7,
            new CubeConfig("A1", 0, 0, 0),
            new CubeConfig("A2", 0, 1, 0),
            new CubeConfig("A3", 0, 2, 0),
            new CubeConfig("A4", 0, 3, 0)),
        new ClusterConfig("LC1", 6.5, 5.5, 4,
            new CubeConfig("A1A", 0, 0, 0),
            new CubeConfig("A2A", 0, 1, 0),
            new CubeConfig("A3A", 0, 2, 0),
            new CubeConfig("A1.5B", 0, 0.5, 1),
            new CubeConfig("A2.5B", 0, 1.5, 1)),
        new ClusterConfig("LC2", 6.5, 6, 1,
            new CubeConfig("A2A", 0, 1, 0),
            new CubeConfig("A1B", 0, 0, 1),
            new CubeConfig("A2B", 0, 1, 1),
            new CubeConfig("A3B+", 0, 2, 1.5),
            new CubeConfig("A1C", 0, 0, 2),
            new CubeConfig("A2C", 0, 1, 2)),
        new ClusterConfig("CS1", 7, 5, 1,
            new CubeConfig("A1.5", 0, 0.5, 0),
            new CubeConfig("B1", 1, 0, 0),
            new CubeConfig("B2", 1, 1, 0),
            new CubeConfig("B3", 1, 2, 0),
            new CubeConfig("C1", 2, 0, 0),
            new CubeConfig("C2", 2, 1, 0)),
        new ClusterConfig("CS2", 10, 4, 1,
            new CubeConfig("A3A", 0, 2, 0),
            new CubeConfig("A3B", 0, 2, 1),
            new CubeConfig("A4B", 0, 3, 1),
            new CubeConfig("A5B", 0, 4, 1),
            new CubeConfig("B1A", 1, 0, 0),
            new CubeConfig("B2A", 1, 1, 0),
            new CubeConfig("B3A", 1, 2, 0),
            new CubeConfig("B3B", 1, 2, 1),
            new CubeConfig("B4B", 1, 3, 1),
            new CubeConfig("B5B", 1, 4, 1),
            new CubeConfig("B6B", 1, 5, 1),
            new CubeConfig("C3A+", 2, 2, 0.5),
            new CubeConfig("C4A+", 2, 3, 0.5),
            new CubeConfig("C5A+", 2, 4, 0.5)),
        new ClusterConfig("CS3", 13, 5, 1,
            new CubeConfig("A3B", 0, 2, 1),
            new CubeConfig("A4B", 0, 3, 1),
            new CubeConfig("A5B", 0, 4, 1),
            new CubeConfig("A6B", 0, 5, 1),
            new CubeConfig("B1A", 1, 0, 0),
            new CubeConfig("B2A", 1, 1, 0),
            new CubeConfig("B3A", 1, 2, 0),
            new CubeConfig("B3B", 1, 2, 1),
            new CubeConfig("C2A", 2, 1, 0),
            new CubeConfig("C3A", 2, 2, 0),
            new CubeConfig("C3B", 2, 2, 1),
            new CubeConfig("C4B", 2, 3, 1)),
        new ClusterConfig("CS4", 16, 5, 1,
            new CubeConfig("A1A", 0, 0, 0),
            new CubeConfig("A2A", 0, 1, 0),
            new CubeConfig("A3A", 0, 2, 0),
            new CubeConfig("A4A", 0, 3, 0),
            new CubeConfig("A5A", 0, 4, 0),
            new CubeConfig("A2B", 0, 1, 1),
            new CubeConfig("A3B", 0, 2, 1),
            new CubeConfig("B1A", 1, 0, 0),
            new CubeConfig("B2A", 1, 1, 0),
            new CubeConfig("B3A", 1, 2, 0),
            new CubeConfig("C3A", 2, 2, 0),
            new CubeConfig("C4A", 2, 3, 0),
            new CubeConfig("C3B", 2, 2, 1),
            new CubeConfig("C4B", 2, 3, 1),
            new CubeConfig("C5B", 2, 4, 1)),
        new ClusterConfig("CS5", 19, 6, 1,
            new CubeConfig("A3A", 0, 2, 0),
            new CubeConfig("A4A", 0, 3, 0),
            new CubeConfig("A1B", 0, 0, 1),
            new CubeConfig("A2B", 0, 1, 1),
            new CubeConfig("A3B", 0, 2, 1),
            new CubeConfig("A4B", 0, 3, 1),
            new CubeConfig("B1B", 1, 0, 1),
            new CubeConfig("B2B", 1, 1, 1),
            new CubeConfig("B3B", 1, 2, 1),
            new CubeConfig("B4B", 1, 3, 1),
            new CubeConfig("B5B", 1, 4, 1)),
        new ClusterConfig("CS6", 21, 4, 0,
            new CubeConfig("A2B", 0, 1, 1),
            new CubeConfig("A3B", 0, 2, 1),
            new CubeConfig("A4B", 0, 3, 1),
            new CubeConfig("A3C", 0, 2, 2),
            new CubeConfig("A4C", 0, 3, 2),
            new CubeConfig("A5C", 0, 4, 2),
            new CubeConfig("B1A+", 1, 0, 0.5),
            new CubeConfig("B2A+", 1, 1, 0.5),
            new CubeConfig("B3A+", 1, 2, 0.5),
            new CubeConfig("B4A+", 1, 3, 0.5),
            new CubeConfig("B5A+", 1, 4, 0.5),
            new CubeConfig("B6A+", 1, 5, 0.5),
            new CubeConfig("C2B", 2, 1, 1),
            new CubeConfig("C3B", 2, 2, 1),
            new CubeConfig("C3C", 2, 2, 2),
            new CubeConfig("C4C", 2, 3, 2)),
        new ClusterConfig("CS7", 24, 5, 1,
            new CubeConfig("A1A", 0, 0, 0),
            new CubeConfig("A2A", 0, 1, 0),
            new CubeConfig("A3A", 0, 2, 0),
            new CubeConfig("A4A", 0, 3, 0),
            new CubeConfig("A5A", 0, 4, 0),
            new CubeConfig("A6A", 0, 5, 0),
            new CubeConfig("B2A", 1, 1, 0),
            new CubeConfig("B3A", 1, 2, 0),
            new CubeConfig("B2B", 1, 1, 1),
            new CubeConfig("B3B", 1, 2, 1),
            new CubeConfig("B4B", 1, 3, 1),
            new CubeConfig("B5B", 1, 4, 1)),
        new ClusterConfig("CS8", 26, 4, 1,
            new CubeConfig("A3A", 0, 2, 0),
            new CubeConfig("A4A", 0, 3, 0),
            new CubeConfig("A5A", 0, 4, 0),
            new CubeConfig("A3B", 0, 2, 1),
            new CubeConfig("A4B", 0, 3, 1),
            new CubeConfig("A5B", 0, 4, 1),
            new CubeConfig("B4B", 1, 3, 1),
            new CubeConfig("B5B", 1, 4, 1),
            new CubeConfig("B6B", 1, 5, 1),
            new CubeConfig("C1A", 2, 0, 0),
            new CubeConfig("C2A", 2, 1, 0),
            new CubeConfig("C3A", 2, 2, 0),
            new CubeConfig("C4A", 2, 3, 0),
            new CubeConfig("C3B", 2, 2, 1),
            new CubeConfig("C4B", 2, 3, 1)),
        new ClusterConfig("CS9", 29, 5, 1,
            new CubeConfig("A1A", 0, 0, 0),
            new CubeConfig("A2A", 0, 1, 0),
            new CubeConfig("A3A", 0, 2, 0),
            new CubeConfig("A4A", 0, 3, 0),
            new CubeConfig("A5A", 0, 4, 0),
            new CubeConfig("B1A", 1, 0, 0),
            new CubeConfig("B2A", 1, 1, 0),
            new CubeConfig("B3A", 1, 2, 0),
            new CubeConfig("B2B", 1, 1, 1),
            new CubeConfig("B3B", 1, 2, 1),
            new CubeConfig("B4B", 1, 3, 1)),
        new ClusterConfig("CS10", 31, 5, 1,
            new CubeConfig("A2A", 0, 1, 0),
            new CubeConfig("A3A", 0, 2, 0),
            new CubeConfig("A2B", 0, 1, 1),
            new CubeConfig("A3B", 0, 2, 1),
            new CubeConfig("A4B", 0, 3, 1),
            new CubeConfig("A5B", 0, 4, 1),
            new CubeConfig("A6B", 0, 5, 1),
            new CubeConfig("B1B", 1, 0, 1),
            new CubeConfig("B2B", 1, 1, 1),
            new CubeConfig("B3B", 1, 2, 1),
            new CubeConfig("B4B", 1, 3, 1),
            new CubeConfig("C3A", 2, 2, 0),
            new CubeConfig("C4A", 2, 3, 0),
            new CubeConfig("C5A", 2, 4, 0),
            new CubeConfig("C6A", 2, 5, 0)),
        new ClusterConfig("CS11", 34, 4, 1,
            new CubeConfig("A2A", 0, 1, 0),
            new CubeConfig("A3A", 0, 2, 0),
            new CubeConfig("A4A", 0, 3, 0),
            new CubeConfig("A5A", 0, 4, 0),
            new CubeConfig("A6A", 0, 5, 0),
            new CubeConfig("B1A", 1, 0, 0),
            new CubeConfig("B2A", 1, 1, 0),
            new CubeConfig("B3A", 1, 2, 0),
            new CubeConfig("B3B", 1, 2, 1),
            new CubeConfig("B4B", 1, 3, 1),
            new CubeConfig("B5B", 1, 4, 1),
            new CubeConfig("C2A", 2, 1, 0),
            new CubeConfig("C3A", 2, 2, 0),
            new CubeConfig("C3B", 2, 2, 1),
            new CubeConfig("C4B", 2, 3, 1),
            new CubeConfig("C5B", 2, 4, 1)),
        new ClusterConfig("CS12", 37, 5, 1,
            new CubeConfig("A1", 0, 0, 0),
            new CubeConfig("A2", 0, 1, 0),
            new CubeConfig("A3", 0, 2, 0),
            new CubeConfig("A4", 0, 3, 0),
            new CubeConfig("A5", 0, 4, 0),
            new CubeConfig("A6", 0, 5, 0),
            new CubeConfig("B2", 1, 1, 0),
            new CubeConfig("B3", 1, 2, 0)),
        new ClusterConfig("RC1", 39.5, 5.5, 2,
            new CubeConfig("A1.5A", 0, 0.5, 0),
            new CubeConfig("A1B", 0, 0, 1),
            new CubeConfig("A2B", 0, 1, 1),
            new CubeConfig("A1.5C", 0, 0.5, 2),
            new CubeConfig("A2.5C", 0, 1.5, 2)),
        new ClusterConfig("RC2", 39.5, 6, 5,
            new CubeConfig("A1.5A", 0, 0.5, 0),
            new CubeConfig("A2.5A", 0, 1.5, 0),
            new CubeConfig("A1B", 0, 0, 1),
            new CubeConfig("A2B", 0, 1, 1),
            new CubeConfig("A3B", 0, 2, 1)),
        new ClusterConfig("R1", 40, 1, 8,
            new CubeConfig("A1", 0, 0, 0),
            new CubeConfig("A2", 0, 1, 0),
            new CubeConfig("A3", 0, 2, 0),
            new CubeConfig("A4", 0, 3, 0)),
        new ClusterConfig("R2", 41, 5, 7,
            new CubeConfig("A1", 0, 0, 0),
            new CubeConfig("A2", 0, 1, 0),
            new CubeConfig("A3", 0, 2, 0),
            new CubeConfig("A4", 0, 3, 0)),
        new ClusterConfig("R3", 41, 0, 7,
            new CubeConfig("A4A", 0, 3, 0),
            new CubeConfig("A5A", 0, 4, 0),
            new CubeConfig("A1B", 0, 0, 1),
            new CubeConfig("A2B", 0, 1, 1),
            new CubeConfig("A3B", 0, 2, 1),
            new CubeConfig("A4B", 0, 3, 1),
            new CubeConfig("A5B", 0, 4, 1)),
        new ClusterConfig("R4", 42, 3, 7,
            new CubeConfig("A1", 0, 0, 0),
            new CubeConfig("A2", 0, 1, 0),
            new CubeConfig("A3", 0, 2, 0),
            new CubeConfig("A4", 0, 3, 0),
            new CubeConfig("A5", 0, 4, 0),
            new CubeConfig("A6", 0, 5, 0)),
        new ClusterConfig("R5", 43, 5, 7,
            new CubeConfig("A1", 0, 0, 0),
            new CubeConfig("A2", 0, 1, 0),
            new CubeConfig("A3", 0, 2, 0),
            new CubeConfig("A4", 0, 3, 0),
            new CubeConfig("A5", 0, 4, 0)),
        new ClusterConfig("R6", 44, 4.5, 7,
            new CubeConfig("A1", 0, 0, 0),
            new CubeConfig("A2", 0, 1, 0),
            new CubeConfig("A3", 0, 2, 0),
            new CubeConfig("A4", 0, 3, 0)),
        new ClusterConfig("R7", 45, 4.5, 6,
            new CubeConfig("A1", 0, 0, 0),
            new CubeConfig("A2", 0, 1, 0),
            new CubeConfig("A3", 0, 2, 0),
            new CubeConfig("A4", 0, 3, 0),
            new CubeConfig("A5", 0, 4, 0),
            new CubeConfig("A6", 0, 5, 0)),
        new ClusterConfig("R8", 46, 6.5, 6,
            new CubeConfig("A1", 0, 0, 0)),
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


    @Override
    public void setupLx(LX lx) {
        super.setupLx(lx);

        /* TODO(jake): set actual brightness cap here! */
        outputScaler.setTargetLinearScale(1.0);
    }

    @Override
    public String getShowName() {
        return SHOW_NAME;
    }
}
