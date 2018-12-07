package com.symmetrylabs.shows.hhgarden;

import heronarts.lx.LX;

import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.FunctionalParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;

import static com.symmetrylabs.util.MathUtils.*;


public class FlowerTextureInOut extends FlowerTexturePattern {
    public final CompoundParameter speed = (CompoundParameter)
        new CompoundParameter("speed", 1000, 5000, 200)
        .setExponent(.5)
        .setDescription("Speed of the motion");

    private final LXModulator[] modulators = new LXModulator[FlowerModel.POINT_COUNT];
    private final int[] mask = new int[FlowerModel.POINT_COUNT];

    public FlowerTextureInOut(LX lx) {
        super(lx);
        addParameter(speed);
        for (int i = 0; i < modulators.length; ++i) {
            final int ii = i;
            modulators[i] = startModulator(
                new SinLFO(0, 1, new FunctionalParameter() {
                    public double getValue() {
                        return speed.getValue() * (1 + .13 * ii);
                    }
                }).randomBasis());
        }
    }

    public void run(double deltaMs) {
        for (int i = 0; i < modulators.length; i++) {
            mask[i] = LXColor.gray(100.f * modulators[i].getValuef());
        }
        setFlowerMask(mask);
    }
}
