package com.symmetrylabs.layouts.tree;

import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import processing.core.PApplet;


public class OsloLayout extends TreeLayout implements Layout {

    public OsloLayout() {
    }

    @Override
    public SLModel buildModel() {
        return new SLModel(); //new TreeModel();
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
