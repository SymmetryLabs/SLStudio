package com.symmetrylabs.slstudio.ui;

import heronarts.lx.LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIIntegerBox;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UINumberBox;
import heronarts.p3lx.ui.component.UISlider;


public class UILayer extends UI2dContainer {
    public UILayer(UI ui, final LX lx, float x, float y, float w) {
        super(x, y, w, 20);
        setBackgroundColor(0xff404040); //ui.theme.getDeviceBackgroundColor()
        setBorderRounding(4);

        new UILabel(5, 2, 50, 12)
            .setLabel("LAYER")
            .addToContainer(this);

        new UIIntegerBox(45, 0, 130, 20)
            .setParameter(lx.engine.layer)
            .addToContainer(this);
    }
}
