package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;

import static com.symmetrylabs.slstudio.util.Utils.random;


public class TelevisionStatic extends SLPattern {
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

    public void run(double deltaMs) {
        final boolean d = direction.getValuef() > 5.0;

        model.forEachPoint((start, end) -> {
            for (int i=start; i<end; i++) {
                LXPoint p = model.points[i];

                colors[p.index] = lx.hsb(palette.getHuef() + random(hueParameter.getValuef() * 360),
                    random(saturationParameter.getValuef() * 100),
                    random(brightParameter.getValuef() * 100)
                );
            }
        });
    }
}
