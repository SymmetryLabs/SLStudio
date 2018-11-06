package com.symmetrylabs.slstudio.ui.gdx;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import java.util.HashSet;

public class ComponentWindow extends CloseableWindow {
    private final LX lx;
    private final LXComponent comp;
    /* some parameters add other sub-parameters to be "helpful"; ColorParameter
         adds three child parameters for H/S/B when you add it. We just want to draw
         a rich UI for the parent pattern instead, so we detect and hide those
         sub-parameters. */
    private final HashSet<LXParameter> blacklist;

    public ComponentWindow(LX lx, String name, LXComponent comp) {
        super(name);
        this.lx = lx;
        this.comp = comp;

        this.blacklist = new HashSet<>();
        for (LXParameter param : comp.getParameters()) {
            if (param instanceof ColorParameter) {
                ColorParameter cp = (ColorParameter) param;
                blacklist.add(cp.hue);
                blacklist.add(cp.saturation);
                blacklist.add(cp.brightness);
            }
        }
    }

    @Override
    protected void windowSetup() {
        UI.setNextWindowDefaultToCursor(UI.DEFAULT_WIDTH, 350);
    }

    @Override
    protected void drawContents() {
        for (LXParameter param : comp.getParameters()) {
            if (blacklist.contains(param)) {
                continue;
            }
            if (param instanceof BoundedParameter) {
                ParameterUI.draw(lx, (BoundedParameter) param);
            } else if (param instanceof DiscreteParameter) {
                ParameterUI.draw(lx, (DiscreteParameter) param);
            } else if (param instanceof BooleanParameter) {
                ParameterUI.draw(lx, (BooleanParameter) param);
            } else if (param instanceof ColorParameter) {
                ParameterUI.draw(lx, (ColorParameter) param);
            }
        }
    }

    protected boolean isEligibleControlParameter(LXParameter parameter) {
        return parameter instanceof BoundedParameter
            || parameter instanceof DiscreteParameter
            || parameter instanceof BooleanParameter
            || parameter instanceof ColorParameter;
    }
}
