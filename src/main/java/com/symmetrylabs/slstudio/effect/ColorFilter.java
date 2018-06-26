package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;


public class ColorFilter extends LXEffect {

    public final CompoundParameter hue = new CompoundParameter("Hue", 0, 0, 360);

    public final CompoundParameter saturation = new CompoundParameter("Sat", 100, 0, 100);

    public ColorFilter(LX lx) {
        super(lx);
        addParameter(hue);
        addParameter(saturation);
    }

    @Override
    public void run(double deltaMs, double amount) {
        for (LXVector v : getVectorList()) {
            float brightness = LXColor.b(colors[v.index]);
            colors[v.index] = lx.hsb(
                hue.getValuef(),
                saturation.getValuef(),
                brightness
            );
        }
    }
}
