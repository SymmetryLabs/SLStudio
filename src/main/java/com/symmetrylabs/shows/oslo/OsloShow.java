package com.symmetrylabs.shows.oslo;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.base.SLShow;
import com.symmetrylabs.shows.base.UIOutputs;
import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.MappingWindow;
import com.symmetrylabs.shows.cubes.UICubesMappingPanel;
import com.symmetrylabs.shows.cubes.UICubesOutputs;
import com.symmetrylabs.shows.tree.AssignablePixlite;
import com.symmetrylabs.shows.tree.AssignableTenereController;
import com.symmetrylabs.shows.tree.TreeModel;
import com.symmetrylabs.shows.tree.TreeModelingTool;
import com.symmetrylabs.shows.tree.config.BranchConfig;
import com.symmetrylabs.shows.tree.config.LimbConfig;
import com.symmetrylabs.shows.tree.config.TreeConfig;
import com.symmetrylabs.shows.tree.config.TwigConfig;
import com.symmetrylabs.shows.tree.ui.UITreeModelAxes;
import com.symmetrylabs.shows.tree.ui.UITreeModelingTool;
import com.symmetrylabs.shows.tree.ui.UITreeTrunk;
import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.ui.v2.SLModelMappingWindow;
import com.symmetrylabs.slstudio.ui.v2.WindowManager;
import com.symmetrylabs.util.NetworkChannelDebugMonitor.DebugPortMonitor;
import com.symmetrylabs.util.NetworkChannelDebugMonitor.MachinePortMonitor;
import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.p3lx.ui.UI2dScrollContext;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OsloShow extends SLShow implements Show {

    public static final String SHOW_NAME = "oslo";

    public final Map<TreeModel.Branch, AssignableTenereController> treeControllers = new HashMap<>();

    public Map<TreeModel.Branch, AssignableTenereController> getTenereControllers() {
        return treeControllers;
    }
//
    public void setupLx(LX lx) {
        super.setupLx(lx);
        boolean readConfig = readConfigFromDisk();
        if (!readConfig) {
            ApplicationState.setWarning("TreeShow", "show is set to not read tree model from disk, model changes will be saved but not loaded on restart");
        }
        lx.engine.registerComponent("treeModelingTool", TreeModelingTool.getInstance(lx, readConfig));


        TreeModel tree = (TreeModel) (lx.model);
        TreeModelingTool modeler = TreeModelingTool.getInstance(lx);

        System.out.println("Number of branches: " + tree.getBranches().size());


        DebugPortMonitor debugPortMonitor = new DebugPortMonitor();
        debugPortMonitor.start();

        MachinePortMonitor machinePortMonitor = new MachinePortMonitor();
        machinePortMonitor.start();

        String ips[] = { "10.1.20.138",
            "10.128.24.140",
            "10.128.28.142",
            "10.128.38.19",
            "10.128.49.24",
            "10.128.9.132",
            "10.128.14.135",
            "10.0.13.6",
            "10.128.27.141",
            "10.128.33.144",
            "10.128.38.147",
            "10.128.42.21",
            "10.1.6.131",
            "10.129.39.19",
            "10.129.68.34",
            "10.129.5.2",
            "10.1.45.150",
            "10.129.27.13",
            "10.129.41.148",
            "10.129.53.26",
            "10.129.66.33",
            "10.129.69.162",
            "10.2.49.152",
            "10.2.53.154",
            "10.2.55.155",
            "10.2.7.131",
            "10.130.18.137",
            "10.130.31.15",
            "10.130.37.146",
            "10.130.41.148",
            "10.130.48.24",
            "10.130.53.26",
            "10.130.56.28",
            "10.130.57.156",
            "10.130.66.161",
            "10.130.67.33",
            "10.2.19.9",
            "10.2.20.138",
            "10.2.26.141",
            "10.2.38.19",
            "10.2.39.19",
            "10.2.47.151",
            "10.2.48.24",
            "10.2.54.27",
            "10.2.54.155",
            "10.2.57.28",
            "10.2.3.129",
            "10.2.58.29",
            "10.2.58.157",
            "10.2.60.158",
            "10.2.67.161",
            "10.2.4.130",
            "10.2.10.133",
            "10.130.18.9",
            "10.130.23.11",
            "10.130.26.141",
            "10.130.33.144",
            "10.130.39.19",
            "10.130.43.149",
            "10.130.44.150",
            "10.130.48.152",
            "10.130.54.27",
            "10.130.59.157",
            "10.130.60.30",
            "10.130.14.7",
            "10.130.15.7",
            "10.3.64.160",
            "10.3.10.133",
            "10.4.33.144",
            "10.4.49.24",
            "10.4.53.26",
            "10.4.5.130",
            "10.4.26.141",
            "10.4.10.133",
            "10.5.36.18",
            "10.5.52.154",
            "10.5.55.27",
            "10.5.73.164",
            "10.5.109.54" };

        int increment = 0;
        try {
            for (TreeModel.Branch branch : tree.getBranches()) {
                AssignableTenereController controller = new AssignableTenereController(lx, branch);
//                controller.brightness.setValue(0.7);
                controller.setIpAddress(ips[increment]);
                increment++;
                treeControllers.put(branch, controller);
                lx.addOutput(controller);
            }
        } catch (SocketException e) { }

        modeler.branchManipulator.ipAddress.addListener(param -> {
            AssignableTenereController controller = treeControllers.get(modeler.getSelectedBranch());
            controller.setIpAddress(modeler.branchManipulator.ipAddress.getString());
        });
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        super.setupUi(lx, ui);
        ui.preview.addComponent(new UITreeTrunk(SLStudio.applet));

        ui.preview.addComponent(UITreeModelAxes.getInstance(lx));

        UITreeModelAxes uiTreeModelAxes = UITreeModelAxes.getInstance(lx);
        ui.preview.addComponent(uiTreeModelAxes);

        UITreeModelingTool.instance = new UITreeModelingTool(
            ui, TreeModelingTool.getInstance(lx), 0, 0, ui.rightPane.model.getContentWidth());
        UITreeModelingTool.instance.addToContainer(ui.rightPane.model);
    }


    protected boolean readConfigFromDisk() {
        return true;
    }

    final TwigConfig[] BRANCH = new TwigConfig[]{
        new TwigConfig( -15.36f,  16.32f, 0.0f,  57.20f, 180.0f, 0.0f, 4),
        new TwigConfig(  -9.56f,  24.00f, 0.0f, -54.40f, 180.0f, 0.0f, 2),
        new TwigConfig( -15.36f,  33.60f, 0.0f,  36.00f, 180.0f, 0.0f, 3),
        new TwigConfig(  -8.64f,  44.12f, 0.0f,  39.60f, 180.0f, 0.0f, 1),
        new TwigConfig(    0.0f,  48.96f, 0.0f,    0.0f, 180.0f, 0.0f, 0),
        new TwigConfig(  12.48f,  43.20f, 0.0f, -43.20f, 180.0f, 0.0f, 7),
        new TwigConfig(  13.44f,  27.84f, 0.0f, -26.40f, 180.0f, 0.0f, 6),
        new TwigConfig(  16.32f,  20.16f, 0.0f, -64.80f, 180.0f, 0.0f, 5),
    };
    final BranchConfig[] LIMB_TYPE_SINGLE_BRANCH = new BranchConfig[] {
        new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH, true),
    };

    final BranchConfig[] LIMB_TYPE_L1 = new BranchConfig[] {
        new BranchConfig(false, -30.61f,  87.01f,  -0.44f,  60.0f, 0, 1.0f, BRANCH, true),
        new BranchConfig(false, -20.52f,  120.40f,  3.15f,  47.5f, 0, 20f,  BRANCH, true),
        new BranchConfig(false, -5.86f,   125.31f,  0.44f,  -30.0f,  0, 3.0f, BRANCH, true),
        new BranchConfig(false, 29.11f, 105.08f,  3.72f,  -61.1f,  0, 7.0f, BRANCH, true),
        new BranchConfig(false, 8.6f,   59.68f,   -3.37f, -60.0f,  0, 8.5f, BRANCH, true),
    };

    final BranchConfig[] LIMB_TYPE_L2 = new BranchConfig[] {
        new BranchConfig(false, 0.25f,   49.67f,  -10.52f, 0,  29,  15,  BRANCH, false),
        new BranchConfig(false, -8.62f,  61.81f,  -3.37f, 61,  5,   15,  BRANCH, false),
        new BranchConfig(false, -30.71f, 103.38f, 3.41f,  61,  -3,  -6,  BRANCH, false),
        new BranchConfig(false, 4.03f,   118.11f, 0.20f,  30,  0,   3,   BRANCH, false),
        new BranchConfig(false, 20.44f,  121.61f, 0.76f,  -45, 10,  20,  BRANCH, false),
        new BranchConfig(false, 29.94f,  87.55f,  -1.34f, -60, 10,  20,  BRANCH, false),

    };
    final BranchConfig[] LIMB_TYPE_L3 = new BranchConfig[] {
        new BranchConfig(false, -6.93f, 67.40f, -0.49f, 50,  5,  -5,  BRANCH, false),
        new BranchConfig(false, 16.84f, 92.66f, 2.95f,  15,  -7, -10, BRANCH, false),
        new BranchConfig(false, 31.99f, 88.76f, 0.56f,  -59, 10,  0,  BRANCH, false),
    };

    public SLModel buildModel() {
//        TwigConfig.setZEnabled(false);
//        TwigConfig.setElevationEnabled(false);
//        TwigConfig.setTiltEnabled(false);
//
        TreeConfig.createLimbType("Limb/Single Branch", LIMB_TYPE_SINGLE_BRANCH);
        TreeConfig.createLimbType("Limb/L3", LIMB_TYPE_L3);
        TreeConfig.createBranchType("Branch", BRANCH);
//
        TreeConfig config = new TreeConfig(new LimbConfig[] {
                // L6
//                new LimbConfig(false, 13.5f, 170, 45,   -90 + 18.5f, 0, LIMB_TYPE_L1),
                new LimbConfig(false, 13.5f, 170, 135,  -90 + 18.5f, 0, LIMB_TYPE_L1),
                new LimbConfig(false, 13.5f, 170, -135, -90 + 18.5f, 0, LIMB_TYPE_L1),
                new LimbConfig(false, 13.5f, 170, -45,  -90 + 18.5f, 0, LIMB_TYPE_L1),
            // L5
            new LimbConfig(false, 11.5f, 210, 0,   90 - 23.5f, 0, LIMB_TYPE_L2),
            new LimbConfig(false, 11.5f, 210, 90,  90 - 23.5f, 0, LIMB_TYPE_L2),
            new LimbConfig(false, 11.5f, 210, 180, 90 - 23.5f, 0, LIMB_TYPE_L2),
            new LimbConfig(false, 11.5f, 210, -90, 90 - 23.5f, 0, LIMB_TYPE_L2),

            // L4
            new LimbConfig(false, 9.5f, 250, 45,   90 - 23.5f, 0, LIMB_TYPE_L2),
            new LimbConfig(false, 9.5f, 250, 135,  90 - 23.5f, 0, LIMB_TYPE_L2),
            new LimbConfig(false, 9.5f, 250, -135, 90 - 23.5f, 0, LIMB_TYPE_L2),
            new LimbConfig(false, 9.5f, 250, -45,  90 - 23.5f, 0, LIMB_TYPE_L2),
            // L3
            new LimbConfig(false, 7.5f, 290, 0,   90 - 17f, 0, LIMB_TYPE_L3),
            new LimbConfig(false, 7.5f, 290, 90,  90 - 17f, 0, LIMB_TYPE_L3),
            new LimbConfig(false, 7.5f, 290, 180, 90 - 17f, 0, LIMB_TYPE_L3),
            new LimbConfig(false, 7.5f, 290, -90, 90 - 17f, 0, LIMB_TYPE_L3),

            // L2
            new LimbConfig(false, 7.5f, 290, -90, 90 - 17f, 0, LIMB_TYPE_L3),
            new LimbConfig(false, 14, 345, -90,  90 - 15,  0, LIMB_TYPE_SINGLE_BRANCH),
        });
//
        TreeModel tree = new TreeModel(SHOW_NAME, config);
        tree.rotateY(35);

        return tree;
    }
    @Override
    public String getShowName() {
        return SHOW_NAME;
    }
}
