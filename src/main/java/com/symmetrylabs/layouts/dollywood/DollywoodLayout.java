package com.symmetrylabs.layouts.dollywood;

import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import processing.core.PApplet;

import com.symmetrylabs.layouts.oslo.TreeModel;
import com.symmetrylabs.layouts.oslo.UITreeControls;
import com.symmetrylabs.layouts.oslo.UITreeGround;
import com.symmetrylabs.layouts.oslo.UITreeLeaves;
import com.symmetrylabs.layouts.oslo.UITreeStructure;

public class DollywoodLayout implements Layout {
    private final PApplet applet;
    private final TreeModel.ModelMode modelMode;

    public DollywoodLayout(PApplet applet, TreeModel.ModelMode modelMode) {
        this.applet = applet;
        this.modelMode = modelMode;
    }

    @Override
    public SLModel buildModel() {
        return new TreeModel(applet, modelMode);
    }

    @Override
    public void setupLx(SLStudioLX lx) {
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        ui.preview.addComponent(new UITreeGround(applet));
        UITreeStructure uiTreeStructure = new UITreeStructure((TreeModel) lx.model);
        ui.preview.addComponent(uiTreeStructure);
        UITreeLeaves uiTreeLeaves = new UITreeLeaves(lx, applet, (TreeModel) lx.model);
        ui.preview.addComponent(uiTreeLeaves);
        new UITreeControls(ui, uiTreeStructure, uiTreeLeaves).setExpanded(false).addToContainer(ui.leftPane.global);
    }
}
