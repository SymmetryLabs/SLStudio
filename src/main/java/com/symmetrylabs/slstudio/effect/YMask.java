package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

public class YMask extends LXEffect {
    private CompoundParameter minY = new CompoundParameter("minY",  model.yMin, model.yMin, model.yMax);
    private CompoundParameter maxY = new CompoundParameter("maxY",  model.yMax, model.yMin, model.yMax);


    public YMask(LX lx) {
        super(lx);
        addParameter(minY);
        addParameter(maxY);
    }

    @Override
    public void run(double deltaMs, double amount) {
        for (LXVector v : getVectors()) {
            if (v.y > maxY.getValuef() | v.y < minY.getValuef()) {
                colors[v.index] = LXColor.BLACK;
            }
        }
    }
}
