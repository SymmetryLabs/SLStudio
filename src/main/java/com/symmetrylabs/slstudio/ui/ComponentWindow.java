package com.symmetrylabs.slstudio.ui;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;

public class ComponentWindow {
    private final LX lx;
    private final LXComponent comp;
    private final String name;

    public ComponentWindow(LX lx, String name, LXComponent comp) {
        this.lx = lx;
        this.name = name;
        this.comp = comp;
    }

    public void draw() {
        UI.begin(name);
        for (LXParameter param : comp.getParameters()) {
            if (param instanceof BoundedParameter) {
                drawBoundedParam((BoundedParameter) param);
            }
        }
        UI.end();
    }

    public static void drawBoundedParam(BoundedParameter p) {
        float start = p.getValuef();
        float res = UI.sliderFloat(p.getLabel(), start, (float) p.range.v0, (float) p.range.v1);
        if (start != res) {
            p.setValue(res);
        }
    }

    protected boolean isEligibleControlParameter(LXParameter parameter) {
        return parameter instanceof BoundedParameter;
            /*
            parameter instanceof DiscreteParameter ||
            parameter instanceof BooleanParameter;
            */
    }
}
