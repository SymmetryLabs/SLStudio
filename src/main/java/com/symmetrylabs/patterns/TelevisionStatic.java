package com.symmetrylabs.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.*;
import heronarts.lx.modulator.*;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.util.MathUtils;

public class TelevisionStatic extends LXPattern {
    CompoundParameter brightParameter = new CompoundParameter("BRIGHT", 1.0);
    CompoundParameter saturationParameter = new CompoundParameter("SAT", 1.0);
    CompoundParameter hueParameter = new CompoundParameter("HUE", 1.0);
    SinLFO direction = new SinLFO(0, 10, 3000);

    public TelevisionStatic(LX lx) {
        super(lx);

        addModulator(direction).trigger();
        addParameter(brightParameter);
        addParameter(saturationParameter);
        addParameter(hueParameter);
    }

    @Override
    public void run(double deltaMs) {
        boolean d = direction.getValuef() > 5.0;

        for (LXPoint p : model.points) {
            colors[p.index] = lx.hsb(palette.getHuef() + MathUtils.random(hueParameter.getValuef() * 360), MathUtils.random(saturationParameter.getValuef() * 100), MathUtils.random(brightParameter.getValuef() * 100));
        }
    }
}
