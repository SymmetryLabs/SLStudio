package com.symmetrylabs.shows.banyan;

import com.symmetrylabs.slstudio.model.banyan.StarModel;
import com.symmetrylabs.slstudio.model.banyan.TipModel;

import com.symmetrylabs.slstudio.model.banyan.TipperModel;
import heronarts.lx.LX;

import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.shows.tree.*;
import com.symmetrylabs.shows.tree.config.*;
import com.symmetrylabs.shows.tree.ui.UITenereControllers;


public class BanyanShow extends TreeShow {
    public static final String SHOW_NAME = "banyan";

    public static final int NUM_BRANCHES = 80;

    final TwigConfig[] BRANCH = new TwigConfig[]{
        new TwigConfig(-22.367998f, 0.0f, 0.0f, 43.2f, 0.0f, 0.0f, 1),
        new TwigConfig(-19.487999f, 12.575999f, 0.0f, 30.599998f, 0.0f, 0.0f, 2),
        new TwigConfig(-12.479999f, 26.208f, 0.0f, 16.919998f, 0.0f, 0.0f, 3),
        new TwigConfig(-1.344f, 31.008f, 0.0f, 6.1199994f, 0.0f, 0.0f, 4),
        new TwigConfig(8.063999f, 31.104f, 0.0f, -10.799999f, 0.0f, 0.0f, 5),
        new TwigConfig(13.727999f, 20.159998f, 0.0f, -46.799995f, 0.0f, 0.0f, 6),
        new TwigConfig(18.24f, 8.9279995f, 0.0f, -45.359997f, 0.0f, 0.0f, 7),
        new TwigConfig(21.792f, 0.0f, 0.0f, -47.159996f, 0.0f, 0.0f, 8)
    };

    BranchConfig[] LIMB_TYPE = new BranchConfig[NUM_BRANCHES];

    public SLModel buildModel() {
        for (int i = 0; i < NUM_BRANCHES; i++) {
            LIMB_TYPE[i] = new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH, true);
        }

        LimbConfig.lengthEnabled = false;
        LimbConfig.heightEnabled = false;
        LimbConfig.azimuthEnabled = false;
        LimbConfig.elevationEnabled = false;

        TreeConfig.createLimbType("Limb 1", LIMB_TYPE);
        TreeConfig.createBranchType("Branch", BRANCH);

        TreeConfig config = new TreeConfig(new LimbConfig[] {
            // just one limb
            new LimbConfig(false, 0, 0, 0, 0, 0, LIMB_TYPE),
        });

//        TreeModel tree = new TreeModel(SHOW_NAME, config);

        return new StarModel("banyan");
    }

    public void setupLx(final LX lx) {
        super.setupLx(lx);
//        TreeModel tree = (TreeModel) (lx.model);
//        TreeModelingTool modeler = TreeModelingTool.getInstance(lx);

//        System.out.println("Number of branches: " + tree.getBranches().size());

//        lx.engine.addLoopTask(new LXLoopTask() {
//            @Override
//            public void loop(double v) {
//                if (lx.engine.framesPerSecond.getValuef() != 60) {
//                    lx.engine.framesPerSecond.setValue(60);
//                }
//            }
//        });

//        try {
//            for (TreeModel.Branch branch : tree.getBranches()) {
//                AssignableTenereController controller = new AssignableTenereController(lx, branch);
//                controller.brightness.setValue(0.7);
//                controllers.put(branch, controller);
//                lx.addOutput(controller);
//            }
//        } catch (SocketException e) { }

//        modeler.branchManipulator.ipAddress.addListener(param -> {
//            AssignableTenereController controller = controllers.get(modeler.getSelectedBranch());
//            controller.setIpAddress(modeler.branchManipulator.ipAddress.getString());
//        });
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        super.setupUi(lx, ui);
        //ui.preview.addComponent(new UITreeStructure((TreeModel) lx.model));

        new UITenereControllers(lx, ui, 0, 0, ui.rightPane.utility.getContentWidth()).addToContainer(ui.rightPane.model);
    }
    
    @Override
    public String getShowName() {
        return SHOW_NAME;
    }
}
