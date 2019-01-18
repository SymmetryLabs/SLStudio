package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;


public class ColorFilter extends LXEffect {

    public final CompoundParameter hue = new CompoundParameter("Hue", 0, 0, 360);

    public final CompoundParameter saturation = new CompoundParameter("Sat", 100, 0, 100);
    public final BooleanParameter alpha = new BooleanParameter("Alpha", false);

    public ColorFilter(LX lx) {
        super(lx);
        addParameter(hue);
        addParameter(saturation);
        addParameter(alpha);
    }

    @Override
    public void run(double deltaMs, double amount) {
        boolean useAlpha = alpha.getValueb();
        for (LXVector v : getVectors()) {
            int c = colors[v.index];
            float brightness = LXColor.b(c);
            colors[v.index] = 
                (useAlpha ? c & LXColor.ALPHA_MASK : LXColor.ALPHA_MASK) |
                (~LXColor.ALPHA_MASK & lx.hsb(hue.getValuef(), saturation.getValuef(), brightness));
        }
    }
}
