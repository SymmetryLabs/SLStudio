package com.symmetrylabs.performance;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BoundedParameter;


public class ColorShiftEffect extends LXEffect {
    public BoundedParameter shift = new BoundedParameter("shift", 0, 360);

    public ColorShiftEffect(LX lx) {
        super(lx);

        addParameter(shift);
    }

    public String getLabel() {
        return "ColorShift";
    }

    public void run(double deltaMs, double enabledAmount) {
        for (LXPoint p : model.points) {
            int o = colors[p.index];
            float h = LXColor.h(o);
            float s = LXColor.s(o);
            float b = LXColor.b(o);
            colors[p.index] = LXColor.hsb(h + shift.getValuef(), s, b);
        }
    }
}
