package com.symmetrylabs.shows.oslo;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.base.SLShow;
import com.symmetrylabs.shows.base.UIOutputs;
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
import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.LX;
import heronarts.p3lx.ui.UI2dScrollContext;

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
//    public void setupLx(LX lx) {
//        boolean readConfig = readConfigFromDisk();
//        if (!readConfig) {
//            ApplicationState.setWarning("TreeShow", "show is set to not read tree model from disk, model changes will be saved but not loaded on restart");
//        }
//        lx.engine.registerComponent("treeModelingTool", TreeModelingTool.getInstance(lx, readConfig));
//    }
//
//    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
//        //ui.preview.addComponent(new UITreeTrunk(SLStudio.applet));
//
//        ui.preview.addComponent(UITreeModelAxes.getInstance(lx));
//
//        UITreeModelAxes uiTreeModelAxes = UITreeModelAxes.getInstance(lx);
//        ui.preview.addComponent(uiTreeModelAxes);
//
//        UITreeModelingTool.instance = new UITreeModelingTool(
//            ui, TreeModelingTool.getInstance(lx), 0, 0, ui.rightPane.model.getContentWidth());
//        UITreeModelingTool.instance.addToContainer(ui.rightPane.model);
//    }
//
//
//    protected boolean readConfigFromDisk() {
//        return true;
//    }

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
