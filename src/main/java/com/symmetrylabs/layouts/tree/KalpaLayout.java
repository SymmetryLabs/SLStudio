package com.symmetrylabs.layouts.tree;

import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.layouts.tree.config.*;


public class KalpaLayout extends TreeLayout implements Layout {

    @Override
    public SLModel buildModel() {
        TreeConfig config = new TreeConfig(new LimbConfig[] {
            new LimbConfig(10, 8, 0, 0, 0, new BranchConfig[] {
                new BranchConfig("10.200.1.10", 0, 0, 0, 0, 0, 0, new TwigConfig[] {
                    new TwigConfig(0, 0, 0, 0, 0),
                    new TwigConfig(0, 0, 0, 0, 0),
                    new TwigConfig(0, 0, 0, 0, 0),
                    new TwigConfig(0, 0, 0, 0, 0),
                    new TwigConfig(0, 0, 0, 0, 0),
                    new TwigConfig(0, 0, 0, 0, 0),
                    new TwigConfig(0, 0, 0, 0, 0),
                    new TwigConfig(0, 0, 0, 0, 0) 
                }),
                new BranchConfig("10.200.1.11", 0, 50, 0, 0, 0, 0, new TwigConfig[] {
                    new TwigConfig(0, 0, 0, 0, 0),
                    new TwigConfig(0, 0, 0, 0, 0),
                    new TwigConfig(0, 0, 0, 0, 0),
                    new TwigConfig(0, 0, 0, 0, 0),
                    new TwigConfig(0, 0, 0, 0, 0),
                    new TwigConfig(0, 0, 0, 0, 0),
                    new TwigConfig(0, 0, 0, 0, 0),
                    new TwigConfig(0, 0, 0, 0, 0) 
                })
            })
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
