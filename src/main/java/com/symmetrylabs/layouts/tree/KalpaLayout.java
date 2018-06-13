package com.symmetrylabs.layouts.tree;

import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.layouts.tree.config.*;
import static com.symmetrylabs.util.DistanceConstants.*;


public class KalpaLayout extends TreeLayout implements Layout {

    final TwigConfig[] BRANCH_TYPE_A = new TwigConfig[] {
        new TwigConfig(-22, 15, 0,  45, 0),
        new TwigConfig(-20, 37, 0,  45, 0),
        new TwigConfig(-14, 44, 0,  10, 0),
        new TwigConfig(  0, 46, 0, -33, 0),
        new TwigConfig( -1, 21, 0,  17, 0),
        new TwigConfig( 17, 31, 0,  17, 0),
        new TwigConfig( 22, 32, 0, -25, 0),
        new TwigConfig( 22, 15, 0, -45, 0) 
    };

    // (todo) final TwigConfig[] BRANCH_TYPE_B

    final BranchConfig[] LIMB_TYPE_A = new BranchConfig[] {
        new BranchConfig(-3.5f*FEET,  7.5f*FEET, 0.0f*FEET, 24,  65,  0, BRANCH_TYPE_A),
        new BranchConfig(-2.0f*FEET,  8.5f*FEET, 0.0f*FEET, 24,  30,  0, BRANCH_TYPE_A),
        new BranchConfig(   0f*FEET, 10.0f*FEET, 0.0f*FEET, 24,   0,  0, BRANCH_TYPE_A),
        new BranchConfig( 1.5f*FEET,  9.0f*FEET, 0.0f*FEET, 24, -20,  0, BRANCH_TYPE_A),
        new BranchConfig( 2.5f*FEET, 7.50f*FEET, 0.0f*FEET, 25, -40,  0, BRANCH_TYPE_A),
        new BranchConfig(-2.0f*FEET,  8.5f*FEET, 1.0f*FEET, 20,  40, 20, BRANCH_TYPE_A),
        new BranchConfig( 1.5f*FEET,  9.0f*FEET, 1.0f*FEET, 20, -30, 20, BRANCH_TYPE_A),
    };

    @Override
    public SLModel buildModel() {
        TreeConfig config = new TreeConfig(new LimbConfig[] {
            // bottom 
            new LimbConfig(0, 6*FEET, 0*45, -90, LIMB_TYPE_A),
            new LimbConfig(0, 6*FEET, 1*45, -90, LIMB_TYPE_A),
            new LimbConfig(0, 6*FEET, 2*45, -90, LIMB_TYPE_A),
            new LimbConfig(0, 6*FEET, 3*45, -90, LIMB_TYPE_A),
            new LimbConfig(0, 6*FEET, 4*45, -90, LIMB_TYPE_A),
            new LimbConfig(0, 6*FEET, 5*45, -90, LIMB_TYPE_A),
            new LimbConfig(0, 6*FEET, 6*45, -90, LIMB_TYPE_A),
            new LimbConfig(0, 6*FEET, 7*45, -90, LIMB_TYPE_A),

            // middle
            new LimbConfig(0, 6*FEET, 0*120, -65, LIMB_TYPE_A),
            new LimbConfig(0, 6*FEET, 1*120, -65, LIMB_TYPE_A),
            new LimbConfig(0, 6*FEET, 2*120, -65, LIMB_TYPE_A),

            // top
            new LimbConfig(0, 5*FEET, 0*120+60, -35, LIMB_TYPE_A),
            new LimbConfig(0, 5*FEET, 1*120+60, -35, LIMB_TYPE_A),
            new LimbConfig(0, 5*FEET, 2*120+60, -35, LIMB_TYPE_A),

            new LimbConfig(0, 5*FEET, 0*120, -25, LIMB_TYPE_A),
            new LimbConfig(0, 5*FEET, 1*120, -25, LIMB_TYPE_A),
            new LimbConfig(0, 5*FEET, 2*120, -25, LIMB_TYPE_A),
        });

        return new TreeModel(config);
    }

    @Override
    public void setupLx(SLStudioLX lx) {
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
