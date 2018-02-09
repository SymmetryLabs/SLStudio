package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

public class ColorFilter extends LXEffect {

    public final CompoundParameter hue = new CompoundParameter("hue", 0, 0, 360);

    public final CompoundParameter saturation = new CompoundParameter("sat", 100, 0, 100);

    public ColorFilter(LX lx) {
        super(lx);
        addParameter(hue);
        addParameter(saturation);
    }

    @Override
    public void run(double deltaMs, double amount) {
        for (LXPoint p : model.points) {
            float brightness = LXColor.b(colors[p.index]);
            colors[p.index] = lx.hsb(
                hue.getValuef(),
                saturation.getValuef(),
                brightness
            );
        }
    }
}