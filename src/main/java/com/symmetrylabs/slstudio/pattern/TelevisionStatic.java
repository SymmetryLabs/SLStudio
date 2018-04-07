package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.util.MathUtils;

public class TelevisionStatic extends LXPattern {
    CompoundParameter brightParameter = new CompoundParameter("Bright", 1.0);
    CompoundParameter saturationParameter = new CompoundParameter("Sat", 1.0);
    CompoundParameter hueParameter = new CompoundParameter("Hue", 1.0);
    SinLFO direction = new SinLFO(0, 10, 3000);

    public TelevisionStatic(LX lx) {
        super(lx);
        addModulator(direction).trigger();
        addParameter(brightParameter);
        addParameter(saturationParameter);
        addParameter(hueParameter);
    }

    public void run(double deltaMs) {
        final boolean d = direction.getValuef() > 5.0;

        model.getPoints().parallelStream().forEach(p -> {
            colors[p.index] = lx.hsb(palette.getHuef() + MathUtils.random(hueParameter.getValuef() * 360),
                MathUtils.random(saturationParameter.getValuef() * 100),
                MathUtils.random(brightParameter.getValuef() * 100)
            );
        });
    }
}
