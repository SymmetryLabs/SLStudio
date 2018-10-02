package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;


public class GlobalColorPalette extends LXEffect {

    public GlobalColorPalette(LX lx) {
        super(lx);
    }

    @Override
    public void run(double deltaMs, double amount) {
        for (LXVector v : getVectors()) {
            colors[v.index] = lx.hsb(
                palette.getHuef(),
                palette.getSaturationf(),
                LXColor.b(colors[v.index])
            );
        }
    }
}
