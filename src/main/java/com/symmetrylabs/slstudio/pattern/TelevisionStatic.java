package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.LXColor16;
import heronarts.lx.PolyBuffer;


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

    public void run(double deltaMs, PolyBuffer.Space space) {

        Object array = getArray(space);
        final int[] intColors = (space == PolyBuffer.Space.RGB8) ? (int[]) array : null;
        final long[] longColors = (space == PolyBuffer.Space.RGB16) ? (long[]) array : null;
        final boolean d = direction.getValuef() > 5.0;


            // model.getPoints().parallelStream().forEach(p -> {
            // colors[p.index] = lx.hsb(palette.getHuef() + MathUtils.random(hueParameter.getValuef() * 360),
            //     MathUtils.random(saturationParameter.getValuef() * 100),
            //     MathUtils.random(brightParameter.getValuef() * 100)
            // );


            model.getPoints().parallelStream().forEach(p -> {
                float    h = palette.getHuef() + MathUtils.random(hueParameter.getValuef() * 360);
                float    s = MathUtils.random(saturationParameter.getValuef() * 100);
                float    b = MathUtils.random(brightParameter.getValuef() * 100);

                if (space == PolyBuffer.Space.RGB8) {
                    intColors[p.index] = LXColor.hsb(h, s, b);
                }

                else if (space == PolyBuffer.Space.RGB16) {
                    longColors[p.index] = LXColor16.hsb(h, s, b);
                }

        });

        markModified(space);

    }
}


