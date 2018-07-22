package com.symmetrylabs.layouts.tree;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.net.SocketException;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.layouts.tree.*;
import com.symmetrylabs.layouts.tree.config.*;
import static com.symmetrylabs.util.DistanceConstants.*;


public class KalpaLayout extends TreeLayout implements Layout {

    public final Map<String, AssignablePixlite> pixlites = new HashMap<>();
    public final List<AssignablePixlite.Port> pixlitePorts = new ArrayList<>();

    // female
    final TwigConfig[] BRANCH_TYPE_A = new TwigConfig[]{
        new TwigConfig( 16.3f,  0.0f, 0.0f, -61.2f, 0.0f, 0.0f, 1),
        new TwigConfig( 24.5f, 16.3f, 0.0f, -36.0f, 0.0f, 0.0f, 2),
        new TwigConfig( 14.4f, 19.2f, 0.0f,  18.0f, 0.0f, 0.0f, 3),
        new TwigConfig( -5.7f,  9.5f, 0.0f,  14.4f, 0.0f, 0.0f, 4),
        new TwigConfig(-37.4f, 10.5f, 0.0f,  28.8f, 0.0f, 0.0f, 8),
        new TwigConfig(-29.8f, 25.9f, 0.0f,  28.8f, 0.0f, 0.0f, 7),
        new TwigConfig(-16.3f, 36.5f, 0.0f,   6.8f, 0.0f, 0.0f, 6),
        new TwigConfig( 0.0f,  37.4f, 0.0f, -32.4f, 0.0f, 0.0f, 5)
    };

    // male
    final TwigConfig[] BRANCH_TYPE_B = new TwigConfig[] {
        new TwigConfig( 14.2f,  3.8f, 0.0f, -57.0f, 0.0f, 0.0f, 1),
        new TwigConfig(  4.0f, 15.4f, 0.0f,  50.4f, 0.0f, 0.0f, 2),
        new TwigConfig( 20.2f, 19.3f, 0.0f, -28.8f, 0.0f, 0.0f, 3),
        new TwigConfig( 11.5f, 24.0f, 0.0f, -28.8f, 0.0f, 0.0f, 4),
        new TwigConfig(-32.0f,  9.5f, 0.0f,  34.5f, 0.0f, 0.0f, 8),
        new TwigConfig(-25.0f, 13.4f, 0.0f,   7.2f, 0.0f, 0.0f, 7),
        new TwigConfig(-18.4f, 34.5f, 0.0f,  39.5f, 0.0f, 0.0f, 6),
        new TwigConfig(  0.0f, 37.5f, 0.0f,   0.0f, 0.0f, 0.0f, 5)
    };

    final BranchConfig[] LIMB_TYPE_A = new BranchConfig[] {
        new BranchConfig(false, 56.159996f, 3.8999996f, -1.4400027f, 38.52f, 0.0f, 172.8f, BRANCH_TYPE_A),
        new BranchConfig(false, 57.600002f, 21.0f, 3.6000035f, -21.6f, 0.0f, -7.2f, BRANCH_TYPE_A),
        new BranchConfig(false, 32.399998f, 27.0f, 3.5999985f, -7.2f, 0.0f, -9.0f, BRANCH_TYPE_A),
        new BranchConfig(false, 3.6000009f, 21.0f, 3.6000009f, 0.0f, 0.0f, -7.2f, BRANCH_TYPE_B),
        new BranchConfig(false, -18.720001f, 12.0f, 4.3199987f, 27.359999f, 0.0f, -7.2f, BRANCH_TYPE_B),
        new BranchConfig(false, -47.52f, 1.7999998f, 4.3199964f, 41.399998f, 0.0f, -8.28f, BRANCH_TYPE_B),
        new BranchConfig(false, 3.24f, 0.0f, 9.719999f, -10.8f, 7.1999993f, -7.2f, BRANCH_TYPE_B),
        new BranchConfig(false, -12.239999f, 1.7999998f, 10.8f, 7.2f, 20.880001f, -7.2f, BRANCH_TYPE_A)
    };

    @Override
    public SLModel buildModel() {
        TwigConfig.setZEnabled(false);
        TwigConfig.setElevationEnabled(false);
        TwigConfig.setTiltEnabled(false);

        TreeConfig.createLimbType("Type A", LIMB_TYPE_A);
        TreeConfig.createBranchType("Type A", BRANCH_TYPE_A);
        TreeConfig.createBranchType("Type B", BRANCH_TYPE_B);

        TreeConfig config = new TreeConfig(new LimbConfig[] {
            // bottom
            new LimbConfig(false, 50, 6*FEET, 0.0f, -90, 0, new BranchConfig[] {
                new BranchConfig(false, "0.0.0.0", 0, 28.439999f, 12.0f, -8.28f, 43.920002f, 0.0f, 172.8f, BRANCH_TYPE_A),
                new BranchConfig(false, "10.200.1.103", 5, 21.960005f, 21.0f, -5.7599936f, -21.6f, 0.0f, -7.2f, BRANCH_TYPE_A),
                new BranchConfig(false, "10.200.1.100", 7, 11.879999f, 35.7f, -3.6f, -20.519999f, 0.0f, -8.28f, BRANCH_TYPE_A),
                new BranchConfig(false, "10.200.1.106", 7, 0.0f, 42.0f, -1.7999998f, 0.0f, 0.0f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.100", 6, -12.960002f, 27.0f, 2.159997f, 27.359999f, 0.0f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "0.0.0.0", 0, -21.240002f, 14.699999f, 7.1999965f, 47.159996f, 0.0f, -8.28f, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.100", 8, 3.24f, 20.0f, 11.879999f, -10.8f, 7.1999993f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.103", 6, -6.479999f, 21.7999998f, 12.24f, 9.0f, 20.880001f, -7.2f, BRANCH_TYPE_A)
            }),

            new LimbConfig(false, 50, 6*FEET, -60.0f, -90, 0, new BranchConfig[] {
                new BranchConfig(false, "0.0.0.0", 0, 28.439999f, 12.0f, -8.28f, 43.920002f, 0.0f, 172.8f, BRANCH_TYPE_A),
                new BranchConfig(false, "0.0.0.0", 0, 21.960005f, 21.0f, -5.7599936f, -21.6f, 0.0f, -7.2f, BRANCH_TYPE_A),
                new BranchConfig(false, "0.0.0.0", 0, 11.879999f, 35.7f, -3.6f, -20.519999f, 0.0f, -8.28f, BRANCH_TYPE_A),
                new BranchConfig(false, "10.200.1.109", 8, 0.0f, 42.0f, -1.7999998f, 0.0f, 0.0f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.105", 6, -12.960002f, 27.0f, 2.159997f, 27.359999f, 0.0f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.105", 5, -21.240002f, 14.699999f, 7.1999965f, 47.159996f, 0.0f, -8.28f, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.105", 7, 3.24f, 20.0f, 11.879999f, -10.8f, 7.1999993f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.109", 7, -6.479999f, 21.7999998f, 12.24f, 9.0f, 20.880001f, -7.2f, BRANCH_TYPE_A)
            }),

            new LimbConfig(false, 50, 6*FEET, -120.0f, -90, 0, new BranchConfig[] {
                new BranchConfig(false, "10.200.1.108", 6, 28.439999f, 12.0f, -8.28f, 43.920002f, 0.0f, 172.8f, BRANCH_TYPE_A), // READDRESS
                new BranchConfig(false, "0.0.0.0", 0, 21.960005f, 21.0f, -5.7599936f, -21.6f, 0.0f, -7.2f, BRANCH_TYPE_A),
                new BranchConfig(false, "0.0.0.0", 0, 11.879999f, 35.7f, -3.6f, -20.519999f, 0.0f, -8.28f, BRANCH_TYPE_A),
                new BranchConfig(false, "0.0.0.0", 0, 0.0f, 42.0f, -1.7999998f, 0.0f, 0.0f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.108", 7, -12.960002f, 27.0f, 2.159997f, 27.359999f, 0.0f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.108", 5, -21.240002f, 14.699999f, 7.1999965f, 47.159996f, 0.0f, -8.28f, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.108", 2, 3.24f, 20.0f, 11.879999f, -10.8f, 7.1999993f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.108", 4, -6.479999f, 21.7999998f, 12.24f, 9.0f, 20.880001f, -7.2f, BRANCH_TYPE_A)
            }),

            new LimbConfig(false, 50, 6*FEET, -180.0f, -90, 0, new BranchConfig[] {
                new BranchConfig(false, "0.0.0.0", 0, 28.439999f, 12.0f, -8.28f, 43.920002f, 0.0f, 172.8f, BRANCH_TYPE_A),
                new BranchConfig(false, "10.200.1.101", 3, 21.960005f, 21.0f, -5.7599936f, -21.6f, 0.0f, -7.2f, BRANCH_TYPE_A),
                new BranchConfig(false, "10.200.1.101", 2, 11.879999f, 35.7f, -3.6f, -20.519999f, 0.0f, -8.28f, BRANCH_TYPE_A),
                new BranchConfig(false, "10.200.1.101", 5, 0.0f, 42.0f, -1.7999998f, 0.0f, 0.0f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.105", 1, -12.960002f, 27.0f, 2.159997f, 27.359999f, 0.0f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "0.0.0.0", 0, -21.240002f, 14.699999f, 7.1999965f, 47.159996f, 0.0f, -8.28f, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.101", 7, 3.24f, 20.0f, 11.879999f, -10.8f, 7.1999993f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "0.0.0.0", 0, -6.479999f, 21.7999998f, 12.24f, 9.0f, 20.880001f, -7.2f, BRANCH_TYPE_A)
            }),

            new LimbConfig(false, 50, 6*FEET, -240.0f, -90, 0, new BranchConfig[] {
                new BranchConfig(false, "0.0.0.0", 0, 28.439999f, 12.0f, -8.28f, 43.920002f, 0.0f, 172.8f, BRANCH_TYPE_A),
                new BranchConfig(false, "0.0.0.0", 0, 21.960005f, 21.0f, -5.7599936f, -21.6f, 0.0f, -7.2f, BRANCH_TYPE_A),
                new BranchConfig(false, "0.0.0.0", 0, 11.879999f, 35.7f, -3.6f, -20.519999f, 0.0f, -8.28f, BRANCH_TYPE_A),
                new BranchConfig(false, "10.200.1.106", 1, 0.0f, 42.0f, -1.7999998f, 0.0f, 0.0f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.106", 4, -12.960002f, 27.0f, 2.159997f, 27.359999f, 0.0f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.106", 3, -21.240002f, 14.699999f, 7.1999965f, 47.159996f, 0.0f, -8.28f, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.106", 2, 3.24f, 20.0f, 11.879999f, -10.8f, 7.1999993f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.103", 7, -6.479999f, 21.7999998f, 12.24f, 9.0f, 20.880001f, -7.2f, BRANCH_TYPE_A)
            }),

            new LimbConfig(false, 50, 6*FEET, -300.0f, -90, 0, new BranchConfig[] {
                new BranchConfig(false, "0.0.0.0", 0, 28.439999f, 12.0f, -8.28f, 43.920002f, 0.0f, 172.8f, BRANCH_TYPE_A),
                new BranchConfig(false, "10.200.1.104", 5, 21.960005f, 21.0f, -5.7599936f, -21.6f, 0.0f, -7.2f, BRANCH_TYPE_A),
                new BranchConfig(false, "10.200.1.105", 2, 11.879999f, 35.7f, -3.6f, -20.519999f, 0.0f, -8.28f, BRANCH_TYPE_A),
                new BranchConfig(false, "0.0.0.0", 0, 0.0f, 42.0f, -1.7999998f, 0.0f, 0.0f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.105", 4, -12.960002f, 27.0f, 2.159997f, 27.359999f, 0.0f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.104", 6, -21.240002f, 14.699999f, 7.1999965f, 47.159996f, 0.0f, -8.28f, BRANCH_TYPE_B),
                new BranchConfig(false, "0.0.0.0", 0, 3.24f, 20.0f, 11.879999f, -10.8f, 7.1999993f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.104", 2, -6.479999f, 21.7999998f, 12.24f, 9.0f, 20.880001f, -7.2f, BRANCH_TYPE_A)
            }),

            // middle
            new LimbConfig(false, 40, 87, -90.0f, -63, 0, new BranchConfig[] {
                new BranchConfig(false, "10.200.1.103", 4, 28.439999f, 12.0f, -8.28f, 43.920002f, 0.0f, 172.8f, BRANCH_TYPE_A),
                new BranchConfig(false, "0.0.0.0", 0, 21.960005f, 21.0f, -5.7599936f, -21.6f, 0.0f, -7.2f, BRANCH_TYPE_A),
                new BranchConfig(false, "0.0.0.0", 0, 11.879999f, 35.7f, -3.6f, -20.519999f, 0.0f, -8.28f, BRANCH_TYPE_A),
                new BranchConfig(false, "0.0.0.0", 0, 0.0f, 42.0f, -1.7999998f, 0.0f, 0.0f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "0.0.0.0", 0, -12.960002f, 27.0f, 2.159997f, 27.359999f, 0.0f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "0.0.0.0", 0, -21.240002f, 14.699999f, 7.1999965f, 47.159996f, 0.0f, -8.28f, BRANCH_TYPE_B),
                new BranchConfig(false, "0.0.0.0", 0, 3.24f, 20.0f, 11.879999f, -10.8f, 7.1999993f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "0.0.0.0", 0, -6.479999f, 21.7999998f, 12.24f, 9.0f, 20.880001f, -7.2f, BRANCH_TYPE_A)
            }),

            new LimbConfig(false, 40, 87, -210.0f, -63, 0, new BranchConfig[] {
                new BranchConfig(false, "0.0.0.0", 0, 28.439999f, 12.0f, -8.28f, 43.920002f, 0.0f, 172.8f, BRANCH_TYPE_A),
                new BranchConfig(false, "10.200.1.102", 7, 21.960005f, 21.0f, -5.7599936f, -21.6f, 0.0f, -7.2f, BRANCH_TYPE_A),
                new BranchConfig(false, "10.200.1.102", 8, 11.879999f, 35.7f, -3.6f, -20.519999f, 0.0f, -8.28f, BRANCH_TYPE_A),
                new BranchConfig(false, "10.200.1.102", 6, 0.0f, 42.0f, -1.7999998f, 0.0f, 0.0f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.106", 8, -12.960002f, 27.0f, 2.159997f, 27.359999f, 0.0f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "0.0.0.0", 0, -21.240002f, 14.699999f, 7.1999965f, 47.159996f, 0.0f, -8.28f, BRANCH_TYPE_B),
                new BranchConfig(false, "0.0.0.0", 0, 3.24f, 20.0f, 11.879999f, -10.8f, 7.1999993f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.102", 1, -6.479999f, 21.7999998f, 12.24f, 9.0f, 20.880001f, -7.2f, BRANCH_TYPE_A)
            }),

            new LimbConfig(false, 40, 87, -330.0f, -63, 0, new BranchConfig[] {
                new BranchConfig(false, "0.0.0.0", 0, 28.439999f, 12.0f, -8.28f, 43.920002f, 0.0f, 172.8f, BRANCH_TYPE_A),
                new BranchConfig(false, "0.0.0.0", 0, 21.960005f, 21.0f, -5.7599936f, -21.6f, 0.0f, -7.2f, BRANCH_TYPE_A),
                new BranchConfig(false, "10.200.1.104", 7, 11.879999f, 35.7f, -3.6f, -20.519999f, 0.0f, -8.28f, BRANCH_TYPE_A),
                new BranchConfig(false, "0.0.0.0", 0, 0.0f, 42.0f, -1.7999998f, 0.0f, 0.0f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "10.200.1.104", 1, -12.960002f, 27.0f, 2.159997f, 27.359999f, 0.0f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "0.0.0.0", 0, -21.240002f, 14.699999f, 7.1999965f, 47.159996f, 0.0f, -8.28f, BRANCH_TYPE_B),
                new BranchConfig(false, "0.0.0.0", 0, 3.24f, 20.0f, 11.879999f, -10.8f, 7.1999993f, -7.2f, BRANCH_TYPE_B),
                new BranchConfig(false, "0.0.0.0", 0, -6.479999f, 21.7999998f, 12.24f, 9.0f, 20.880001f, -7.2f, BRANCH_TYPE_A)
            }),

            // top
            new LimbConfig(false, 5, 130, -90, -53, 0, new BranchConfig[] {
                new BranchConfig(false, "10.200.1.107", 7, 0, 12.0f, 0, 0, 0, 0, BRANCH_TYPE_A)
            }),
            new LimbConfig(false, 5, 130, -150, -53, 0, new BranchConfig[] {
                new BranchConfig(false, "10.200.1.107", 2, 0, 12.0f, 0, 0, 0, 0, BRANCH_TYPE_A)
            }),
            new LimbConfig(false, 5, 130, -270, -53, 0, new BranchConfig[] {
                new BranchConfig(false, "10.200.1.107", 4, 0, 12.0f, 0, 0, 0, 0, BRANCH_TYPE_A)
            }),

            // top top
             new LimbConfig(false, 5, 150, -30, -53, 0, new BranchConfig[] {
                 new BranchConfig(false, "10.200.1.107", 6, 0, 12.0f, 0, 0, 0, 180, BRANCH_TYPE_A)
             }),
             new LimbConfig(false, 5, 150, -210, -53, 0, new BranchConfig[] {
                 new BranchConfig(false, "10.200.1.107", 3, 0, 12.0f, 0, 0, 0, 180, BRANCH_TYPE_A)
             }),
             new LimbConfig(false, 5, 150, -330, -53, 0, new BranchConfig[] {
                 new BranchConfig(false, "10.200.1.107", 5, 0, 12.0f, 0, 0, 0, 180, BRANCH_TYPE_A)
             }),
        });

        return new TreeModel(config);
    }

    @Override
    public void setupLx(SLStudioLX lx) {
        lx.engine.framesPerSecond.setValue(30);
//        for (TreeModel.Branch branch : ((TreeModel)lx.model).getBranches()) {
//            try {
//                PixliteOutput output = new PixliteOutput(lx, branch);
//                pixliteOutputs.put(branch, output);
//                lx.addOutput(output);
//            } catch (SocketException e) {
//                e.printStackTrace();
//            }
//        }

        final String[] ipAddresses = new String[] {
            "10.200.1.100", "10.200.1.101", "10.200.1.102", "10.200.1.103", "10.200.1.104",
            "10.200.1.105", "10.200.1.106", "10.200.1.107", "10.200.1.108", "10.200.1.109"
        };

        for (int i = 0; i < ipAddresses.length; i++) {
            AssignablePixlite pixlite = new AssignablePixlite(lx, ipAddresses[i]);
            pixlites.put(ipAddresses[i], pixlite);
            pixlitePorts.addAll(pixlite.ports);
            lx.addOutput(pixlite);
        }

        System.out.println("------------------------------");
        for (AssignablePixlite.Port port : pixlitePorts) {
            for (TreeModel.Branch branch : ((TreeModel)lx.model).getBranches()) {
                if (port.ipAddress.equals(branch.getConfig().ipAddress)
                    && port.index == branch.getConfig().channel) {
                    System.out.println(port.index + " - " + branch.getConfig().channel);
                    port.setBranch(branch);
                }

            }
        }
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        // ui.preview.addComponent(new UITreeGround(applet));
        // UITreeStructure uiTreeStructure = new UITreeStructure((TreeModel) lx.model);
        // ui.preview.addComponent(uiTreeStructure);
        // UITreeLeaves uiTreeLeaves = new UITreeLeaves(lx, applet, (TreeModel) lx.model);
        // ui.preview.addComponent(uiTreeLeaves);
        // new UITreeControls(ui, uiTreeStructure, uiTreeLeaves).setExpanded(false).addToContainer(ui.leftPane.global);
    }
}
