package com.symmetrylabs.shows.kalpa;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.net.SocketException;


import heronarts.lx.model.LXPoint;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.shows.tree.*;
import com.symmetrylabs.shows.tree.config.*;
import com.symmetrylabs.shows.tree.ui.*;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;

import static com.symmetrylabs.util.DistanceConstants.*;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import processing.core.PGraphics;
import static com.symmetrylabs.util.DistanceConstants.*;


public class KalpaShow extends TreeShow {
    public static final String SHOW_NAME = "kalpa";

    public final Map<String, AssignablePixlite> pixlites = new HashMap<>();
    public final List<AssignablePixlite.Port> pixlitePorts = new ArrayList<>();


    // female
    final TwigConfig[] BRANCH_TYPE_A = new TwigConfig[]{
        new TwigConfig( 0.0f,  37.4f, 0.0f, -32.4f, 0.0f, 0.0f, 1), // 8
        new TwigConfig(-16.3f, 36.5f, 0.0f,   6.8f, 0.0f, 0.0f, 2), // 7
        new TwigConfig(-29.8f, 25.9f, 0.0f,  28.8f, 0.0f, 0.0f, 3), // 6
        new TwigConfig(-37.4f, 10.5f, 0.0f,  28.8f, 0.0f, 0.0f, 4), // 5
        new TwigConfig( -5.7f,  9.5f, 0.0f,  14.4f, 0.0f, 0.0f, 5), // 4
        new TwigConfig( 14.4f, 19.2f, 0.0f,  18.0f, 0.0f, 0.0f, 6), // 3
        new TwigConfig( 24.5f, 16.3f, 0.0f, -36.0f, 0.0f, 0.0f, 7), // 2
        new TwigConfig( 16.3f,  0.0f, 0.0f, -61.2f, 0.0f, 0.0f, 8), // 1
    };

    // male
    final TwigConfig[] BRANCH_TYPE_B = new TwigConfig[] {
        new TwigConfig( 14.2f,  3.8f, 0.0f, -57.0f, 0.0f, 0.0f, 1), // 8
        new TwigConfig(  4.0f, 15.4f, 0.0f,  50.4f, 0.0f, 0.0f, 2), // 7
        new TwigConfig( 20.2f, 19.3f, 0.0f, -28.8f, 0.0f, 0.0f, 3), // 6
        new TwigConfig( 11.5f, 24.0f, 0.0f, -28.8f, 0.0f, 0.0f, 4), // 5
        new TwigConfig(-32.0f,  9.5f, 0.0f,  34.5f, 0.0f, 0.0f, 5), // 1
        new TwigConfig(-25.0f, 13.4f, 0.0f,   7.2f, 0.0f, 0.0f, 6), // 2
        new TwigConfig(-18.4f, 34.5f, 0.0f,  39.5f, 0.0f, 0.0f, 7), // 3
        new TwigConfig(  0.0f, 37.5f, 0.0f,   0.0f, 0.0f, 0.0f, 8)  // 4
    };

//    final BranchConfig[] LIMB_TYPE_A = new BranchConfig[] {
//        new BranchConfig(false, 56.159996f, 3.8999996f, -1.4400027f, 38.52f, 0.0f, 172.8f, BRANCH_TYPE_A),
//        new BranchConfig(false, 57.600002f, 21.0f, 3.6000035f, -21.6f, 0.0f, -7.2f, BRANCH_TYPE_A),
//        new BranchConfig(false, 32.399998f, 27.0f, 3.5999985f, -7.2f, 0.0f, -9.0f, BRANCH_TYPE_A),
//        new BranchConfig(false, 3.6000009f, 21.0f, 3.6000009f, 0.0f, 0.0f, -7.2f, BRANCH_TYPE_B),
//        new BranchConfig(false, -18.720001f, 12.0f, 4.3199987f, 27.359999f, 0.0f, -7.2f, BRANCH_TYPE_B),
//        new BranchConfig(false, -47.52f, 1.7999998f, 4.3199964f, 41.399998f, 0.0f, -8.28f, BRANCH_TYPE_B),
//        new BranchConfig(false, 3.24f, 0.0f, 9.719999f, -10.8f, 7.1999993f, -7.2f, BRANCH_TYPE_B),
//        new BranchConfig(false, -12.239999f, 1.7999998f, 10.8f, 7.2f, 20.880001f, -7.2f, BRANCH_TYPE_A)
//    };











    final BranchConfig[] LIMB_TYPE_A = new BranchConfig[] {
        new BranchConfig(false,  66, 12, 0, 52, 0, 172, BRANCH_TYPE_A),
        new BranchConfig(false,  40, 21, 0, 46, 0, 171, BRANCH_TYPE_A),
        new BranchConfig(false,  14, 33, 0, 29, 0, 171, BRANCH_TYPE_A),
        new BranchConfig(false,  -10, 42, 0, 7.2f, 0, 172, BRANCH_TYPE_B),
        new BranchConfig(false,  -32, 27, 0, -8, 0, 176, BRANCH_TYPE_B),
        new BranchConfig(false,  -46, 14, 0, -35, 0, 172, BRANCH_TYPE_B),
        new BranchConfig(false,  21, 20, 0, 14, 13, 176, BRANCH_TYPE_B),
        new BranchConfig(false,  -6.5f, 0, -6.48f, -5.4f, 20.88f, 176, BRANCH_TYPE_A)
    };

    public SLModel buildModel() {
        TwigConfig.setZEnabled(false);
        TwigConfig.setElevationEnabled(false);
        TwigConfig.setTiltEnabled(false);

        TreeConfig.createLimbType("Type A", LIMB_TYPE_A);
        TreeConfig.createBranchType("Type A", BRANCH_TYPE_A);
        TreeConfig.createBranchType("Type B", BRANCH_TYPE_B);

        TreeConfig config = new TreeConfig(new LimbConfig[] {
            // bottom
            new LimbConfig(false, 50, 13*FEET, -210.0f, -90, 0, new BranchConfig[] {
//                new BranchConfig(false, "0.0.0.0", 0, 28.439999f, 12.0f, -8.28f, 43.920002f, 0.0f, 172.8f, BRANCH_TYPE_A),
//                new BranchConfig(false, "10.200.1.103", 5, 21.960005f, 21.0f, -5.7599936f, -21.6f, 0.0f, -7.2f, BRANCH_TYPE_A),
//                new BranchConfig(false, "10.200.1.100", 7, 11.879999f, 35.7f, -3.6f, -20.519999f, 0.0f, -8.28f, BRANCH_TYPE_A),
//                new BranchConfig(false, "10.200.1.106", 7, 0.0f, 42.0f, -1.7999998f, 0.0f, 0.0f, -7.2f, BRANCH_TYPE_B),
//                new BranchConfig(false, "10.200.1.100", 6, -12.960002f, 27.0f, 2.159997f, 27.359999f, 0.0f, -7.2f, BRANCH_TYPE_B),
//                new BranchConfig(false, "0.0.0.0", 0, -21.240002f, 14.699999f, 7.1999965f, 47.159996f, 0.0f, -8.28f, BRANCH_TYPE_B),
//                new BranchConfig(false, "10.200.1.100", 8, 3.24f, 20.0f, 11.879999f, -10.8f, 7.1999993f, -7.2f, BRANCH_TYPE_B),
//                new BranchConfig(false, "10.200.1.103", 6, -6.479999f, 21.7999998f, 12.24f, 9.0f, 20.880001f, -7.2f, BRANCH_TYPE_A)
                new BranchConfig(false, "0.0.0.0", 0, 66, 12, 0, 52, 0, 172, BRANCH_TYPE_A),
                  new BranchConfig(false, "10.200.1.103", 5, 40, 21, 0, 46, 0, 171, BRANCH_TYPE_A),
                  new BranchConfig(false, "10.200.1.100", 7, 14, 33, 0, 29, 0, 171, BRANCH_TYPE_A),
                new BranchConfig(false, "10.200.1.106", 7, -10, 42, 0, 7.2f, 0, 172, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.100", 6, -32, 27, 0, -8, 0, 176, BRANCH_TYPE_B),
                new BranchConfig(false, "0.0.0.0", 0, -46, 14, 0, -35, 0, 172, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.100", 8, 21, 20, 7, 14, 13, 176, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.103", 6, -6.5f, 21.8f, 14, -5.4f, 20.88f, 176, BRANCH_TYPE_A)
            }),

            new LimbConfig(false, 50, 13*FEET, -270.0f, -90, 0, new BranchConfig[] {
//                new BranchConfig(false, "10.200.1.104", 8, 28.439999f, 12.0f, -8.28f, 43.920002f, 0.0f, 172.8f, BRANCH_TYPE_A),
//                new BranchConfig(false, "10.200.1.109", 6, 21.960005f, 21.0f, -5.7599936f, -21.6f, 0.0f, -7.2f, BRANCH_TYPE_A),
//                new BranchConfig(false, "0.0.0.0", 0, 11.879999f, 35.7f, -3.6f, -20.519999f, 0.0f, -8.28f, BRANCH_TYPE_A),
//                new BranchConfig(false, "10.200.1.109", 8, 0.0f, 42.0f, -1.7999998f, 0.0f, 0.0f, -7.2f, BRANCH_TYPE_B),
//                new BranchConfig(false, "10.200.1.105", 6, -12.960002f, 27.0f, 2.159997f, 27.359999f, 0.0f, -7.2f, BRANCH_TYPE_B),
//                new BranchConfig(false, "10.200.1.105", 5, -21.240002f, 14.699999f, 7.1999965f, 47.159996f, 0.0f, -8.28f, BRANCH_TYPE_B),
//                new BranchConfig(false, "10.200.1.105", 7, 3.24f, 20.0f, 11.879999f, -10.8f, 7.1999993f, -7.2f, BRANCH_TYPE_B),
//                new BranchConfig(false, "10.200.1.109", 7, -6.479999f, 21.7999998f, 12.24f, 9.0f, 20.880001f, -7.2f, BRANCH_TYPE_A)
                new BranchConfig(false, "10.200.1.104", 8, 66, 12, 0, 52, 0, 172, BRANCH_TYPE_A),
                  new BranchConfig(false, "10.200.1.109", 6, 40, 21, 0, 46, 0, 171, BRANCH_TYPE_A),
                  new BranchConfig(false, "0.0.0.0", 0, 14, 33, 0, 0, 0, 171, BRANCH_TYPE_A),
                  new BranchConfig(false, "10.200.1.109", 8, -10, 42, 0, 7.2f, 0, 172, BRANCH_TYPE_B),
                  new BranchConfig(false, "10.200.1.105", 6, -32, 27, 0, -8, 0, 176, BRANCH_TYPE_B),
                  new BranchConfig(false, "10.200.1.105", 5, -46, 14, 0, -35, 0, 172, BRANCH_TYPE_B),
                  new BranchConfig(false, "10.200.1.105", 7, 21, 20, 7, 14, 13, 176, BRANCH_TYPE_B),
                  new BranchConfig(false, "10.200.1.109", 7, -6.5f, 21.8f, 14, -5.4f, 20.88f, 176, BRANCH_TYPE_A)
            }),

            new LimbConfig(false, 50, 13*FEET, -330.0f, -90, 0, new BranchConfig[] {
                //new BranchConfig(false, "10.200.1.108", 6, 28.439999f, 12.0f, -8.28f, 43.920002f, 0.0f, 172.8f, BRANCH_TYPE_A), // READDRESS
                //new BranchConfig(false, "10.200.1.108", 1, 21.960005f, 21.0f, -5.7599936f, -21.6f, 0.0f, -7.2f, BRANCH_TYPE_A),
                //new BranchConfig(false, "0.0.0.0", 0, 11.879999f, 35.7f, -3.6f, -20.519999f, 0.0f, -8.28f, BRANCH_TYPE_A),
                //new BranchConfig(false, "10.200.1.105", 8, 0.0f, 42.0f, -1.7999998f, 0.0f, 0.0f, -7.2f, BRANCH_TYPE_B),
                //new BranchConfig(false, "10.200.1.108", 7, -12.960002f, 27.0f, 2.159997f, 27.359999f, 0.0f, -7.2f, BRANCH_TYPE_B),
                //new BranchConfig(false, "10.200.1.108", 5, -21.240002f, 14.699999f, 7.1999965f, 47.159996f, 0.0f, -8.28f, BRANCH_TYPE_B),
                //new BranchConfig(false, "10.200.1.108", 2, 3.24f, 20.0f, 11.879999f, -10.8f, 7.1999993f, -7.2f, BRANCH_TYPE_B),
                //new BranchConfig(false, "10.200.1.108", 4, -6.479999f, 21.7999998f, 12.24f, 9.0f, 20.880001f, -7.2f, BRANCH_TYPE_A)
                new BranchConfig(false, "10.200.1.108", 6,  66, 12, 0, 52, 0, 172, BRANCH_TYPE_A),
                  new BranchConfig(false, "10.200.1.108", 1,  40, 21, 0, 46, 0, 171, BRANCH_TYPE_A),
                  new BranchConfig(false, "0.0.0.0", 0,       14, 33, 0, 29, 0, 171, BRANCH_TYPE_A),
                  new BranchConfig(false, "10.200.1.105", 8,  -10, 42, 0, 7.2f, 0, 172, BRANCH_TYPE_B),
                  new BranchConfig(false, "10.200.1.108", 7,  -32, 27, 0, -8, 0, 176, BRANCH_TYPE_B),
                  new BranchConfig(false, "10.200.1.108", 5,  -46, 14, 0, -35, 0, 172, BRANCH_TYPE_B),
                  new BranchConfig(false, "10.200.1.108", 2,  21, 20, 7, 14, 13, 176, BRANCH_TYPE_B),
                  new BranchConfig(false, "10.200.1.108", 4,  -6.5f, 21.8f, 14, -5.4f, 20.88f, 176, BRANCH_TYPE_A)
            }),

            new LimbConfig(false, 50, 13*FEET, -30.0f, -90, 0, new BranchConfig[] {
                //new BranchConfig(false, "0.0.0.0", 0, 28.439999f, 12.0f, -8.28f, 43.920002f, 0.0f, 172.8f, BRANCH_TYPE_A),
                //new BranchConfig(false, "10.200.1.101", 3, 21.960005f, 21.0f, -5.7599936f, -21.6f, 0.0f, -7.2f, BRANCH_TYPE_A),
                //new BranchConfig(false, "10.200.1.101", 2, 11.879999f, 35.7f, -3.6f, -20.519999f, 0.0f, -8.28f, BRANCH_TYPE_A),
                //new BranchConfig(false, "10.200.1.101", 5, 0.0f, 42.0f, -1.7999998f, 0.0f, 0.0f, -7.2f, BRANCH_TYPE_B),
                //new BranchConfig(false, "10.200.1.105", 1, -12.960002f, 27.0f, 2.159997f, 27.359999f, 0.0f, -7.2f, BRANCH_TYPE_B),
                //new BranchConfig(false, "10.200.1.101", 6, -21.240002f, 14.699999f, 7.1999965f, 47.159996f, 0.0f, -8.28f, BRANCH_TYPE_B),
                //new BranchConfig(false, "10.200.1.101", 7, 3.24f, 20.0f, 11.879999f, -10.8f, 7.1999993f, -7.2f, BRANCH_TYPE_B),
                //new BranchConfig(false, "0.0.0.0", 0, -6.479999f, 21.7999998f, 12.24f, 9.0f, 20.880001f, -7.2f, BRANCH_TYPE_A)
                new BranchConfig(false, "0.0.0.0", 0,  66, 12, 0, 52, 0, 172, BRANCH_TYPE_A),
                new BranchConfig(false, "10.200.1.101", 3,  40, 21, 0, 46, 0, 171, BRANCH_TYPE_A),
                  new BranchConfig(false, "10.200.1.101", 2,  14, 33, 0, 29, 0, 171, BRANCH_TYPE_A),
                  new BranchConfig(false, "10.200.1.101", 5,  -10, 42, 0, 7.2f, 0, 172, BRANCH_TYPE_B),
                  new BranchConfig(false, "10.200.1.105", 1,  -32, 27, 0, -8, 0, 176, BRANCH_TYPE_B),
                  new BranchConfig(false, "10.200.1.101", 6,  -46, 14, 0, -35, 0, 172, BRANCH_TYPE_B),
                  new BranchConfig(false, "10.200.1.101", 7,  21, 20, 7, 14, 13, 176, BRANCH_TYPE_B),
                  new BranchConfig(false, "0.0.0.0", 0,  -6.5f, 21.8f, 14, -5.4f, 20.88f, 176, BRANCH_TYPE_A)
            }),

            new LimbConfig(false, 50, 13*FEET, -90.0f, -90, 0, new BranchConfig[] {
//                new BranchConfig(false, "0.0.0.0", 0, 28.439999f, 12.0f, -8.28f, 43.920002f, 0.0f, 172.8f, BRANCH_TYPE_A),
//                new BranchConfig(false, "0.0.0.0", 0, 21.960005f, 21.0f, -5.7599936f, -21.6f, 0.0f, -7.2f, BRANCH_TYPE_A),
//                new BranchConfig(false, "0.0.0.0", 0, 11.879999f, 35.7f, -3.6f, -20.519999f, 0.0f, -8.28f, BRANCH_TYPE_A),
//                new BranchConfig(false, "10.200.1.106", 1, 0.0f, 42.0f, -1.7999998f, 0.0f, 0.0f, -7.2f, BRANCH_TYPE_B),
//                new BranchConfig(false, "10.200.1.106", 4, -12.960002f, 27.0f, 2.159997f, 27.359999f, 0.0f, -7.2f, BRANCH_TYPE_B),
//                new BranchConfig(false, "10.200.1.106", 3, -21.240002f, 14.699999f, 7.1999965f, 47.159996f, 0.0f, -8.28f, BRANCH_TYPE_B),
//                new BranchConfig(false, "10.200.1.106", 2, 3.24f, 20.0f, 11.879999f, -10.8f, 7.1999993f, -7.2f, BRANCH_TYPE_B),
//                new BranchConfig(false, "10.200.1.103", 7, -6.479999f, 21.7999998f, 12.24f, 9.0f, 20.880001f, -7.2f, BRANCH_TYPE_A)
                 new BranchConfig(false, "0.0.0.0", 0,  66, 12, 0, 52, 0, 172, BRANCH_TYPE_A),
                 new BranchConfig(false, "0.0.0.0", 0,  40, 21, 0, 46, 0, 171, BRANCH_TYPE_A),
                 new BranchConfig(false, "0.0.0.0", 0,  14, 33, 0, 29, 0, 171, BRANCH_TYPE_A),
                 new BranchConfig(false, "10.200.1.106", 1,  -10, 42, 0, 7.2f, 0, 172, BRANCH_TYPE_B),
                 new BranchConfig(false, "10.200.1.106", 4,  -32, 27, 0, -8, 0, 176, BRANCH_TYPE_B),
                 new BranchConfig(false, "10.200.1.106", 3,  -46, 14, 0, -35, 0, 172, BRANCH_TYPE_B),
                 new BranchConfig(false, "10.200.1.106", 2,  21, 20, 7, 14, 13, 176, BRANCH_TYPE_B),
                 new BranchConfig(false, "10.200.1.103", 7,  -6.5f, 21.8f, 14, -5.4f, 20.88f, 176, BRANCH_TYPE_A)
            }),

            new LimbConfig(false, 50, 13*FEET, -150.0f, -90, 0, new BranchConfig[] {
                //new BranchConfig(false, "0.0.0.0", 0, 28.439999f, 12.0f, -8.28f, 43.920002f, 0.0f, 172.8f, BRANCH_TYPE_A),
                //new BranchConfig(false, "10.200.1.104", 5, 21.960005f, 21.0f, -5.7599936f, -21.6f, 0.0f, -7.2f, BRANCH_TYPE_A),
                //new BranchConfig(false, "10.200.1.105", 2, 11.879999f, 35.7f, -3.6f, -20.519999f, 0.0f, -8.28f, BRANCH_TYPE_A),
                //new BranchConfig(false, "0.0.0.0", 0, 0.0f, 42.0f, -1.7999998f, 0.0f, 0.0f, -7.2f, BRANCH_TYPE_B),
                //new BranchConfig(false, "10.200.1.105", 4, -12.960002f, 27.0f, 2.159997f, 27.359999f, 0.0f, -7.2f, BRANCH_TYPE_B),
                //new BranchConfig(false, "10.200.1.104", 6, -21.240002f, 14.699999f, 7.1999965f, 47.159996f, 0.0f, -8.28f, BRANCH_TYPE_B),
                //new BranchConfig(false, "10.200.1.105", 3, 3.24f, 20.0f, 11.879999f, -10.8f, 7.1999993f, -7.2f, BRANCH_TYPE_B),
                //new BranchConfig(false, "10.200.1.104", 2, -6.479999f, 21.7999998f, 12.24f, 9.0f, 20.880001f, -7.2f, BRANCH_TYPE_A)
                new BranchConfig(false, "0.0.0.0", 0, 66, 12, 0, 52, 0, 172, BRANCH_TYPE_A),
                  new BranchConfig(false, "10.200.1.104", 5, 40, 21, 0, 46, 0, 171, BRANCH_TYPE_A),
                  new BranchConfig(false, "10.200.1.105", 2, 14, 33, 0, 29, 0, 171, BRANCH_TYPE_A),
                  new BranchConfig(false, "0.0.0.0", 0, -10, 42, 0, 7.2f, 0, 172, BRANCH_TYPE_B),
                  new BranchConfig(false, "10.200.1.105", 4, -32, 27, 0, -8, 0, 176, BRANCH_TYPE_B),
                  new BranchConfig(false, "10.200.1.104", 6, -46, 14, 0, -35, 0, 172, BRANCH_TYPE_B),
                  new BranchConfig(false, "10.200.1.105", 3, 21, 20, 7, 14, 13, 176, BRANCH_TYPE_B),
                  new BranchConfig(false, "10.200.1.104", 2, -6.5f, 21.8f, 14, -5.4f, 20.88f, 176, BRANCH_TYPE_A)
            }),

            // middle
            new LimbConfig(false, 40, 14.25f*FEET, -295.0f, -63, 0, new BranchConfig[] {
            //new BranchConfig(false, "10.200.1.103", 4, 28.439999f, 12.0f, -8.28f, 43.920002f, 0.0f, 172.8f, BRANCH_TYPE_A),
            //new BranchConfig(false, "10.200.1.100", 4, 21.960005f, 21.0f, -5.7599936f, -21.6f, 0.0f, -7.2f, BRANCH_TYPE_A),
            //new BranchConfig(false, "10.200.1.102", 4, 11.879999f, 35.7f, -3.6f, -20.519999f, 0.0f, -8.28f, BRANCH_TYPE_A),
            //new BranchConfig(false, "0.0.0.0", 0, 0.0f, 42.0f, -1.7999998f, 0.0f, 0.0f, -7.2f, BRANCH_TYPE_B),
            //new BranchConfig(false, "0.0.0.0", 0, -12.960002f, 27.0f, 2.159997f, 27.359999f, 0.0f, -7.2f, BRANCH_TYPE_B),
            //new BranchConfig(false, "10.200.1.102", 5, -21.240002f, 14.699999f, 7.1999965f, 47.159996f, 0.0f, -8.28f, BRANCH_TYPE_B),
            //new BranchConfig(false, "10.200.1.103", 3, 3.24f, 20.0f, 11.879999f, -10.8f, 7.1999993f, -7.2f, BRANCH_TYPE_B),
            //new BranchConfig(false, "0.0.0.0", 0, -6.479999f, 21.7999998f, 12.24f, 9.0f, 20.880001f, -7.2f, BRANCH_TYPE_A)
               new BranchConfig(false, "10.200.1.103", 4,66, 12, 0, 52, 0, 172, BRANCH_TYPE_A),
                 new BranchConfig(false, "10.200.1.100", 4,40, 21, 0, 46, 0, 171, BRANCH_TYPE_A),
                 new BranchConfig(false, "10.200.1.102", 4,14, 33, 0, 29, 0, 171, BRANCH_TYPE_A),
                 new BranchConfig(false, "0.0.0.0", 0,-10, 42, 0, 7.2f, 0, 172, BRANCH_TYPE_B),
                 new BranchConfig(false, "0.0.0.0", 0,-32, 27, 0, -8, 0, 176, BRANCH_TYPE_B),
                 new BranchConfig(false, "10.200.1.102", 5,-46, 14, 0, -35, 0, 172, BRANCH_TYPE_B),
                 new BranchConfig(false, "10.200.1.103", 3,21, 20, 0, 14, 13, 176, BRANCH_TYPE_B),
                 new BranchConfig(false, "0.0.0.0", 0,-6.5f, 0, -6.48f, -5.4f, 20.88f, 176, BRANCH_TYPE_A)

            }),

            new LimbConfig(false, 40, 14.25f*FEET, -55.0f, -63, 0, new BranchConfig[] {
                //new BranchConfig(false, "10.200.1.102", 2, 28.439999f, 12.0f, -8.28f, 43.920002f, 0.0f, 172.8f, BRANCH_TYPE_A),
                //new BranchConfig(false, "10.200.1.102", 7, 21.960005f, 21.0f, -5.7599936f, -21.6f, 0.0f, -7.2f, BRANCH_TYPE_A),
                //new BranchConfig(false, "10.200.1.102", 8, 11.879999f, 35.7f, -3.6f, -20.519999f, 0.0f, -8.28f, BRANCH_TYPE_A),
                //new BranchConfig(false, "10.200.1.102", 6, 0.0f, 42.0f, -1.7999998f, 0.0f, 0.0f, -7.2f, BRANCH_TYPE_B),
                //new BranchConfig(false, "10.200.1.106", 8, -12.960002f, 27.0f, 2.159997f, 27.359999f, 0.0f, -7.2f, BRANCH_TYPE_B),
                //new BranchConfig(false, "10.200.1.102", 3, -21.240002f, 14.699999f, 7.1999965f, 47.159996f, 0.0f, -8.28f, BRANCH_TYPE_B),
                //new BranchConfig(false, "0.0.0.0", 0, 3.24f, 20.0f, 11.879999f, -10.8f, 7.1999993f, -7.2f, BRANCH_TYPE_B),
                //new BranchConfig(false, "10.200.1.102", 1, -6.479999f, 21.7999998f, 12.24f, 9.0f, 20.880001f, -7.2f, BRANCH_TYPE_A)
                new BranchConfig(false, "10.200.1.102", 2,66, 12, 0, 52, 0, 172, BRANCH_TYPE_A),
                  new BranchConfig(false, "10.200.1.102", 7,40, 21, 0, 46, 0, 171, BRANCH_TYPE_A),
                  new BranchConfig(false, "10.200.1.102", 8,14, 33, 0, 29, 0, 171, BRANCH_TYPE_A),
                  new BranchConfig(false, "10.200.1.102", 6,-10, 42, 0, 7.2f, 0, 172, BRANCH_TYPE_B),
                  new BranchConfig(false, "10.200.1.106", 8,-32, 27, 0, -8, 0, 176, BRANCH_TYPE_B),
                  new BranchConfig(false, "10.200.1.102", 3,-46, 14, 0, -35, 0, 172, BRANCH_TYPE_B),
                  new BranchConfig(false, "0.0.0.0", 0,21, 20, 0, 14, 13, 176, BRANCH_TYPE_B),
                  new BranchConfig(false, "10.200.1.102", 1,-6.5f, 0, -6.48f, -5.4f, 20.88f, 176, BRANCH_TYPE_A)
            }),

            new LimbConfig(false, 40, 14.25f*FEET, -175.0f, -63, 0, new BranchConfig[] {
                //new BranchConfig(false, "10.200.1.109", 4, 28.439999f, 12.0f, -8.28f, 43.920002f, 0.0f, 172.8f, BRANCH_TYPE_A),
                //new BranchConfig(false, "10.200.1.109", 1, 21.960005f, 21.0f, -5.7599936f, -21.6f, 0.0f, -7.2f, BRANCH_TYPE_A),
                //new BranchConfig(false, "10.200.1.104", 7, 11.879999f, 35.7f, -3.6f, -20.519999f, 0.0f, -8.28f, BRANCH_TYPE_A),
                //new BranchConfig(false, "10.200.1.109", 3, 0.0f, 42.0f, -1.7999998f, 0.0f, 0.0f, -7.2f, BRANCH_TYPE_B),
                //new BranchConfig(false, "10.200.1.104", 1, -12.960002f, 27.0f, 2.159997f, 27.359999f, 0.0f, -7.2f, BRANCH_TYPE_B),
                //new BranchConfig(false, "0.0.0.0", 0, -21.240002f, 14.699999f, 7.1999965f, 47.159996f, 0.0f, -8.28f, BRANCH_TYPE_B),
                //new BranchConfig(false, "0.0.0.0", 0, 3.24f, 20.0f, 11.879999f, -10.8f, 7.1999993f, -7.2f, BRANCH_TYPE_B),
                //new BranchConfig(false, "0.0.0.0", 0, -6.479999f, 21.7999998f, 12.24f, 9.0f, 20.880001f, -7.2f, BRANCH_TYPE_A)
            new BranchConfig(false, "10.200.1.109", 4,66, 12, 0, 52, 0, 172, BRANCH_TYPE_A),
                new BranchConfig(false, "10.200.1.109", 1,40, 21, 0, 46, 0, 171, BRANCH_TYPE_A),
            new BranchConfig(false, "10.200.1.104", 7,14, 33, 0, 29, 0, 171, BRANCH_TYPE_A),
                new BranchConfig(false, "10.200.1.109", 3,-10, 42, 0, 7.2f, 0, 172, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.104", 1,-32, 27, 0, -8, 0, 176, BRANCH_TYPE_B),
                new BranchConfig(false, "0.0.0.0", 0, -46, 14, 0, -35, 0, 172, BRANCH_TYPE_B),
                new BranchConfig(false, "0.0.0.0", 0, 21, 20, 0, 14, 13, 176, BRANCH_TYPE_B),
                new BranchConfig(false, "0.0.0.0", 0, -6.5f, 0, -6.48f, -5.4f, 20.88f, 176, BRANCH_TYPE_A)
            }),

            // top
            new LimbConfig(false, 5, 17*FEET, -240, -45, 0, new BranchConfig[] {
                new BranchConfig(false, "10.200.1.107", 7, 0, 12.0f, 0, 0, 0, 0, BRANCH_TYPE_B)
            }),
            new LimbConfig(false, 5, 17*FEET, -0, -45, 0, new BranchConfig[] {
                new BranchConfig(false, "10.200.1.107", 2, 0, 12.0f, 0, 0, 0, 0, BRANCH_TYPE_A)
            }),
            new LimbConfig(false, 5, 17*FEET, -120, -45, 0, new BranchConfig[] {
                new BranchConfig(false, "10.200.1.107", 4, 0, 12.0f, 0, 0, 0, 0, BRANCH_TYPE_A)
            }),

            // top top
            new LimbConfig(false, 5, 19*FEET, -300, -30, 0, new BranchConfig[] {
                new BranchConfig(false, "10.200.1.107", 6, 0, 12.0f, 0, 0, 0, 180, BRANCH_TYPE_A)
            }),
            new LimbConfig(false, 5, 19*FEET, -60, -30, 0, new BranchConfig[] {
                new BranchConfig(false, "10.200.1.107", 3, 0, 12.0f, 0, 0, 0, 180, BRANCH_TYPE_A)
            }),
            new LimbConfig(false, 5, 19*FEET, -180, -30, 0, new BranchConfig[] {
                new BranchConfig(false, "10.200.1.107", 5, 0, 12.0f, 0, 0, 0, 180, BRANCH_TYPE_A)
            }),
        });

        return new TreeModel(SHOW_NAME, config);
    }

    public void setupLx(SLStudioLX lx) {
        super.setupLx(lx);
        //lx.engine.framesPerSecond.setValue(30);

        final String[] ipAddresses = new String[] {
            "10.200.1.100", "10.200.1.101", "10.200.1.102", "10.200.1.103", "10.200.1.104",
            "10.200.1.105", "10.200.1.106", "10.200.1.107", "10.200.1.108", "10.200.1.109"
        };

//        for (int i = 0; i < ipAddresses.length; i++) {
//            addPixlite(lx, new AssignablePixlite(lx, ipAddresses[i]));
//        }

        for (int i = 0; i < ipAddresses.length; i++) {
            AssignablePixlite pixlite = new AssignablePixlite(lx, ipAddresses[i]);
            pixlites.put(ipAddresses[i], pixlite);
            pixlitePorts.addAll(pixlite.ports);
            lx.addOutput(pixlite);
        }

        //System.out.println("------------------------------");
        for (AssignablePixlite.Port port : pixlitePorts) {
            for (TreeModel.Branch branch : ((TreeModel)lx.model).getBranches()) {
                if (port.ipAddress.equals(branch.getConfig().ipAddress)
                    && port.index == branch.getConfig().channel) {
                    //System.out.println(port.index + " - " + branch.getConfig().channel);
                    port.setBranch(branch);
                }
            }
        }
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        super.setupUi(lx, ui);
        ui.preview.addComponent(new UIRocoBuilding());
        ui.preview.addComponent(new UITreeStructure((TreeModel) lx.model));
        // UITreeLeaves uiTreeLeaves = new UITreeLeaves(lx, applet, (TreeModel) lx.model);
        // ui.preview.addComponent(uiTreeLeaves);
        // new UITreeControls(ui, uiTreeStructure, uiTreeLeaves).setExpanded(false).addToContainer(ui.leftPane.global);
        new UIPixlites(lx, ui, 0, 0, ui.rightPane.utility.getContentWidth()).addToContainer(ui.rightPane.model);
    }

    @Override
    public String getShowName() {
        return SHOW_NAME;
    }
}

class UIRocoBuilding extends UI3dComponent {
    protected void onDraw(UI ui, PGraphics pg) {
        pg.fill(0xff8c5431);
        pg.pushMatrix();
        pg.translate(0, 18*FEET, 34*FEET);
        pg.box(100*FEET, 34*FEET,33*FEET);
        pg.popMatrix();
    }
}
