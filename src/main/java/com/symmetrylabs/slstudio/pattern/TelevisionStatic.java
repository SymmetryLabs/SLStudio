package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.util.MathUtils;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;

import static heronarts.lx.PolyBuffer.Space.SRGB8;

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

    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);
        final boolean d = direction.getValuef() > 5.0;

        model.getPoints().parallelStream().forEach(p -> {
            colors[p.index] = LXColor.hsb(palette.getHuef() + MathUtils.random(hueParameter.getValuef() * 360),
                MathUtils.random(saturationParameter.getValuef() * 100),
                MathUtils.random(brightParameter.getValuef() * 100)
            );
        });
        markModified(SRGB8);
    }
}
