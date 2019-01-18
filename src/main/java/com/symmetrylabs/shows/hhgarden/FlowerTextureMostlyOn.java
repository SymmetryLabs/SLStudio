package com.symmetrylabs.shows.hhgarden;

import heronarts.lx.LX;

import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.FunctionalParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;

import static com.symmetrylabs.util.MathUtils.*;


public class FlowerTextureMostlyOn extends FlowerTexturePattern {
    public final CompoundParameter p1 =
        new CompoundParameter("p1", 910, 100, 10000);
    public final CompoundParameter p2 =
        new CompoundParameter("p2", 1100, 100, 10000);

    private final int[] mask = new int[FlowerModel.POINT_COUNT];
    private final LXModulator mod1;
    private final LXModulator mod2;
    private int midx1 = 0;
    private double swap1 = 0;
    private int midx2 = 3;
    private double swap2 = 0;

    public FlowerTextureMostlyOn(LX lx) {
        super(lx);
        addParameter(p1);
        addParameter(p2);
        mod1 = startModulator(new SinLFO(0, 100, p1));
        mod2 = startModulator(new SinLFO(0, 100, p2));
    }

    public void run(double deltaMs) {
        swap1 += deltaMs;
        if (swap1 > p1.getValuef()) {
            midx1 = (midx1 + 1) % FlowerModel.POINT_COUNT;
            swap1 = 0;
        }
        swap2 += deltaMs;
        if (swap2 > p2.getValuef()) {
            midx2 = (midx2 + 1) % FlowerModel.POINT_COUNT;
            swap2 = 0;
        }
        float v1 = mod1.getValuef();
        float v2 = mod2.getValuef();
        for (int i = 0; i < FlowerModel.POINT_COUNT; i++) {
            mask[i] = LXColor.gray(
                i == midx1 && i == midx2 ? 100.f - Float.min(v1, v2) :
                i == midx1 ? 100.f - v1 :
                i == midx2 ? 100.f - v2 :
                100.f);
        }
        setFlowerMask(mask);
    }
}
