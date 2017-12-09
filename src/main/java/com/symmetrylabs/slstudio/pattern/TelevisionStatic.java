package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;

import java.util.Arrays;
import java.util.function.Consumer;

import static com.symmetrylabs.slstudio.util.Utils.random;


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

    public void run(double deltaMs) {
        final boolean d = direction.getValuef() > 5.0;
        Arrays.asList(model.points).parallelStream().forEach(new Consumer<LXPoint>() {
            @Override
            public void accept(final LXPoint p) {
                colors[p.index] = lx.hsb(palette.getHuef() + random(hueParameter.getValuef() * 360),
                    random(saturationParameter.getValuef() * 100),
                    random(brightParameter.getValuef() * 100)
                );
            }
        });
    }
}
