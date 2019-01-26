package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import java.util.HashSet;
import java.util.List;
import heronarts.lx.parameter.BooleanParameter.Mode;
import java.util.ArrayList;

public class ComponentWindow extends CloseableWindow {
    private final ComponentUI ui;

    public ComponentWindow(LX lx, String name, LXComponent comp) {
        super(name);
        this.ui = new ComponentUI(lx, comp);
    }

    @Override
    protected void windowSetup() {
        UI.setNextWindowDefaultToCursor(UI.DEFAULT_WIDTH, 350);
    }

    @Override
    protected void drawContents() {
        ui.draw();
    }
}
