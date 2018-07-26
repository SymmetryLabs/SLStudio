package com.symmetrylabs.layouts.cubes;

//package com.symmetrylabs.layouts.cubes;

import java.util.*;
import java.lang.ref.WeakReference;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.Float;


import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.output.SLController;
import com.symmetrylabs.util.CubePhysicalIdMap;
import com.symmetrylabs.util.listenable.SetListener;
import heronarts.lx.LX;
import heronarts.lx.output.FadecandyOutput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transform.LXTransform;
import heronarts.p3lx.ui.UI2dScrollContext;

import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.listenable.ListenableSet;
import com.symmetrylabs.util.Utils;
import static com.symmetrylabs.util.DistanceUtils.*;


/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class SummerStageLayout extends CubesLayout {

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;

    static final float CUBE_WIDTH = 24;
    static final float CUBE_HEIGHT = 24;
    static final float TOWER_WIDTH = 24;
    static final float TOWER_HEIGHT = 24;
    static final float CUBE_SPACING = 1.5f;

    static final float TOWER_VERTICAL_SPACING = 0;
    static final float TOWER_RISER = 14;
    static final float SP = 26f;
    static final float JUMP = TOWER_HEIGHT+TOWER_VERTICAL_SPACING;

    static final float INCHES_PER_METER = 39.3701f;

    static final ClusterConfig[] clusters = new ClusterConfig[]{

        new ClusterConfig("CLUSTER_A", 0, 0, 0, new TowerConfig[] {
            // col 1
            new TowerConfig(SP * 0, SP * 1, SP * 0, new String[][]{new String[]{"0", "0"}}),
            new TowerConfig(SP * 0, SP * 0, SP * -1, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 1, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 2, SP * 0, new String[][]{new String[]{"0", "0"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}})
        }),

            new ClusterConfig("CLUSTER_B", 250, 0, 0, new TowerConfig[] {
                // col 1
                new TowerConfig(SP * 0, SP * 2, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
                // col 2
                new TowerConfig(SP * 1, SP * 2, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
                // col 3
                new TowerConfig(SP * 2, SP * 1, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
                // col 4
                new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}})
            }),

            new ClusterConfig("CLUSTER_C", 500, 0, 0, new TowerConfig[] {
                // col 1
                new TowerConfig(SP * 0, SP * 1, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
                new TowerConfig(SP * 0, SP * 0, SP * -1, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
                // col 2
                new TowerConfig(SP * 1, SP * 1, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
                new TowerConfig(SP * 1, SP * 0, SP * -1, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
                // col 3
                new TowerConfig(SP * 2, SP * 1, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
                new TowerConfig(SP * 2, SP * 0, SP * -1, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
                // col 4
                new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}}),
                new TowerConfig(SP * 3, SP * 0, SP * -1, new String[][]{new String[]{"0", "0"}}),
                new TowerConfig(SP * 3, SP * 2, SP * -1, new String[][]{new String[]{"0", "0"}}),
            }),

            new ClusterConfig("CLUSTER_D", 750, 0, 0, new TowerConfig[]{
                // col 1
                new TowerConfig(SP * 0, SP * 1, SP * -1, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
                new TowerConfig(SP * 0, SP * 1, SP * -2, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
                // col 2
                new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
                new TowerConfig(SP * 1, SP * 1, SP * -1, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
                // col 3
                new TowerConfig(SP * 2, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            }),

//        new ClusterConfig("CLUSTER_E", 400, 0, 0, new TowerConfig[]{
//
//        }),

        new ClusterConfig("CLUSTER_F", 1000, 0, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
        }),

        new ClusterConfig("CLUSTER_G", 1250, 0, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 1, SP * 0, new String[][]{new String[]{"931", "938"}, new String[]{"928", "918"}, new String[]{"906", "912"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{new String[]{"925", "934"}, new String[]{"916", "917"}, new String[]{"924", "923"}, new String[]{"922", "932"}, new String[]{"878", "888"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 0, SP * 0, new String[][]{new String[]{"936", "921"}, new String[]{"926", "998"}, new String[]{"939", "933"}, new String[]{"935", "999"}, new String[]{"904", "920"}, new String[]{"927", "915"}, new String[]{"919", "910"}}),
        }),

        new ClusterConfig("CLUSTER_H", 1500, 0, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 8, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
        }),

        new ClusterConfig("CLUSTER_I", 1750, 0, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 5, SP * 0, new String[][]{new String[]{"0", "0"}}),
            new TowerConfig(SP * 0, SP * 8, SP * 0, new String[][]{new String[]{"0", "0"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 9, SP * 0, new String[][]{new String[]{"0", "0"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 9, SP * 0, new String[][]{new String[]{"0", "0"}}),
            // col 5
            new TowerConfig(SP * 4, SP * 9, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 6
            new TowerConfig(SP * 5, SP * 9, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 7
            new TowerConfig(SP * 6, SP * 9, SP * 0, new String[][]{new String[]{"0", "0"}}),
        }),

        new ClusterConfig("CLUSTER_Z", 2000, 0, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 1, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 0, SP * 2, SP * -1, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}}),
            new TowerConfig(SP * 1, SP * 1, SP * -1, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}}),
            new TowerConfig(SP * 2, SP * 2, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 2, SP * 0, SP * -1, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 1, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 3, SP * 1, SP * -1, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}})
        }),

        new ClusterConfig("CLUSTER_Y", 2250, 0, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 1, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 1, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}})
        }),

        new ClusterConfig("CLUSTER_X", 2500, 0, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 1, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 0, SP * 1, SP * -1, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 1, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 1, SP * 1, SP * -1, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 1, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 2, SP * 1, SP * -1, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
        }),

        new ClusterConfig("CLUSTER_W", 2750, 0, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 2, SP * 0, new String[][]{new String[]{"0", "0"}}),
            new TowerConfig(SP * 1, SP * 4, SP * 0, new String[][]{new String[]{"0", "0"}}),
            new TowerConfig(SP * 1, SP * 1, SP * -1, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 1, SP * -1, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 2, SP * 2, SP * -2, new String[][]{new String[]{"0", "0"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 1, SP * -1, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 3, SP * 1, SP * -2, new String[][]{new String[]{"0", "0"}}),
        }),

        new ClusterConfig("CLUSTER_V", 3000, 0, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}}),
            new TowerConfig(SP * 0, SP * 0, SP * -1, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 0, SP * 1, SP * -2, new String[][]{new String[]{"0", "0"}}),
            new TowerConfig(SP * 0, SP * 1, SP * -3, new String[][]{new String[]{"0", "0"}}),
            new TowerConfig(SP * 0, SP * 0, SP * -4, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
        }),

        new ClusterConfig("CLUSTER_U", 3250, 0, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 1, SP * 0, new String[][]{new String[]{"623", "622"}, new String[]{"667", "666"}, new String[]{"645", "644"}, new String[]{"783", "782"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 1, SP * 0, new String[][]{new String[]{"962", "462"}, new String[]{"475", "474"}, new String[]{"589", "588"}, new String[]{"725", "724"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 1, SP * 0, new String[][]{new String[]{"673", "672"}, new String[]{"773", "772"}, new String[]{"665", "664"}, new String[]{"811", "810"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"876", "520"}, new String[]{"681", "680"}, new String[]{"595", "804"}, new String[]{"675", "674"}, new String[]{"655", "463"}}),
            // col 5
            new TowerConfig(SP * 4, SP * 3, SP * 0, new String[][]{new String[]{"813", "812"}, new String[]{"649", "648"}}),
        }),

        new ClusterConfig("CLUSTER_T", 3500, 0, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 3, SP * 0, new String[][]{new String[]{"975", "958"}, new String[]{"993", "973"}, new String[]{"953", "955"}, new String[]{"703", "963"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 3, SP * 0, new String[][]{new String[]{"940", "951"}, new String[]{"951", "950"}}),
            new TowerConfig(SP * 1, SP * 6, SP * 0, new String[][]{new String[]{"954", "959"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 0, SP * 0, new String[][]{new String[]{"952", "943"}, new String[]{"949", "946"}, new String[]{"944", "947"}, new String[]{"929", "995"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 1, SP * 0, new String[][]{new String[]{"905", "908"}, new String[]{"941", "937"}, new String[]{"942", "930"}, new String[]{"807", "830"}}),
        }),

        new ClusterConfig("CLUSTER_R", 3750, 0, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
        }),

        new ClusterConfig("CLUSTER_Q", 4000, 0, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 9, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 9, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 9, SP * 0, new String[][]{new String[]{"0", "0"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 5
            new TowerConfig(SP * 4, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}}),
            new TowerConfig(SP * 4, SP * 2, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 4, SP * 7, SP * 0, new String[][]{new String[]{"0", "0"}}),
            new TowerConfig(SP * 4, SP * 9, SP * 0, new String[][]{new String[]{"0", "0"}}),
        }),

        new ClusterConfig("CLUSTER_S", 4250, 0, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 1, SP * 0, new String[][]{new String[]{"0", "0"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 1, SP * 4, SP * 0, new String[][]{new String[]{"0", "0"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 3, SP * 3, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
        }),

        new ClusterConfig("CLUSTER_P", 4500, 0, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 0, SP * 3, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 2, SP * 4, SP * 0, new String[][]{new String[]{"0", "0"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 1, SP * 0, new String[][]{new String[]{"0", "0"}}),
            new TowerConfig(SP * 3, SP * 4, SP * 0, new String[][]{new String[]{"0", "0"}}),
            // col 5
            new TowerConfig(SP * 4, SP * 4, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 6
            new TowerConfig(SP * 5, SP * 4, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
        }),

        new ClusterConfig("CLUSTER_O", 4750, 0, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 9, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 9, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 9, SP * 0, new String[][]{new String[]{"0", "0"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 5
            new TowerConfig(SP * 4, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}}),
            new TowerConfig(SP * 4, SP * 2, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 4, SP * 7, SP * 0, new String[][]{new String[]{"0", "0"}}),
            new TowerConfig(SP * 4, SP * 9, SP * 0, new String[][]{new String[]{"0", "0"}}),
        }),

    };

    static class ClusterConfig {
        final String id;
        final float x;
        final float y;
        final float z;
        final TowerConfig[] configs;

        ClusterConfig(String id, float x, float y, float z, TowerConfig[] configs) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.z = z;
            this.configs = configs;
        }
    }

    static class TowerConfig {

        final CubesModel.Cube.Type type;
        final float x;
        final float y;
        final float z;
        final float xRot;
        final float yRot;
        final float zRot;
        final String[][] ids;
        final float[] yValues;

        TowerConfig(float x, float y, float z, String[][] ids) {
            this(CubesModel.Cube.Type.LARGE, x, y, z, ids);
        }

        TowerConfig(float x, float y, float z, float yRot, String[][] ids) {
            this(x, y, z, 0, yRot, 0, ids);
        }

        TowerConfig(CubesModel.Cube.Type type, float x, float y, float z, String[][] ids) {
            this(type, x, y, z, 0, 0, 0, ids);
        }

        TowerConfig(CubesModel.Cube.Type type, float x, float y, float z, float yRot, String[][] ids) {
            this(type, x, y, z, 0, yRot, 0, ids);
        }

        TowerConfig(float x, float y, float z, float xRot, float yRot, float zRot, String[][] ids) {
            this(CubesModel.Cube.Type.LARGE, x, y, z, xRot, yRot, zRot, ids);
        }

        TowerConfig(CubesModel.Cube.Type type, float x, float y, float z, float xRot, float yRot, float zRot, String[][] ids) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.z = z;
            this.xRot = xRot;
            this.yRot = yRot;
            this.zRot = zRot;
            this.ids = ids;

            this.yValues = new float[ids.length];
            for (int i = 0; i < ids.length; i++) {
                yValues[i] = y + i * (CUBE_HEIGHT + CUBE_SPACING);
            }
        }
    }

    public SLModel buildModel() {
        // Any global transforms
        LXTransform globalTransform = new LXTransform();
        globalTransform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
        globalTransform.rotateX(globalRotationX * Math.PI / 180.);
        globalTransform.rotateY(globalRotationY * Math.PI / 180.);
        globalTransform.rotateZ(globalRotationZ * Math.PI / 180.);

        List<CubesModel.Tower> towers = new ArrayList<>();
        List<CubesModel.Cube> allCubes = new ArrayList<>();

//        try {
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
//                Utils.createInput("data/SummerStageCoordinates.txt")));
//
//            List<CubesModel.Cube> cubes = new ArrayList<>();
//
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                line = line.replaceAll(" ","").replaceAll("\"","");
//                String[] vals = line.split(",");
//
//                float x = metersToInches(Float.parseFloat(vals[0]));
//                float y = metersToInches(Float.parseFloat(vals[2]));
//                float z = metersToInches(Float.parseFloat(vals[1]));
//
//                CubesModel.DoubleControllerCube cube = new CubesModel.DoubleControllerCube("0", "0", x, y, z, 0, 0, 0, globalTransform);
//                cubes.add(cube);
//                allCubes.add(cube);
//            }
//            towers.add(new CubesModel.Tower("", cubes));
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        for (ClusterConfig cluster : clusters) {
            List<CubesModel.Cube> cubes = new ArrayList<>();

            globalTransform.push();
            globalTransform.translate(cluster.x, cluster.y, cluster.z);

            for (TowerConfig config : cluster.configs) {
                float x = config.x;
                float z = config.z;

                for (int i = 0; i < config.ids.length; i++) {
                    String idA = config.ids[i][0];
                    String idB = config.ids[i][1];
                    float y = config.yValues[i];
                    CubesModel.DoubleControllerCube cube = new CubesModel.DoubleControllerCube(idA, idB, x, y, z, 0, 0, 0, globalTransform);
                    cubes.add(cube);
                    allCubes.add(cube);
                }
            }
            globalTransform.pop();
            towers.add(new CubesModel.Tower(cluster.id, cubes));
        }

        CubesModel.Cube[] allCubesArr = new CubesModel.Cube[allCubes.size()];
        for (int i = 0; i < allCubesArr.length; i++) {
            allCubesArr[i] = allCubes.get(i);
        }

        return new CubesModel(towers, allCubesArr);
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        UI2dScrollContext utility = ui.rightPane.utility;
        new UIOutputs(lx, ui, this, 0, 0, utility.getContentWidth()).addToContainer(utility);
        new UIMappingPanel(lx, ui, 0, 0, utility.getContentWidth()).addToContainer(utility);
    }
}
