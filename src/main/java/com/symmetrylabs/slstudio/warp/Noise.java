package com.symmetrylabs.slstudio.warp;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;
import heronarts.lx.warp.LXWarp;

import static com.symmetrylabs.util.MathUtils.*;


public class Noise extends LXWarp {

    private final float RANGE = 75;

    private CompoundParameter range = new CompoundParameter("range", .3f);
    private CompoundParameter density = new CompoundParameter("dens", .3f, .01f, 1);

    public Noise(LX lx) {
        super(lx);
        addParameter(range);
        addParameter(density);
    }

    public boolean run(double deltaMs, boolean inputVectorsChanged) {
        if (range.getValuef() > 0) {
            float rangeVal = RANGE * range.getValuef();

            for (int i = 0; i < inputVectors.length; i++) {
                LXVector iv = inputVectors[i];
                if (iv == null) {
                    outputVectors[i] = null;
                } else {
                    LXVector ov = new LXVector(iv);
                    ov.add(random(-rangeVal, rangeVal),
                        random(-rangeVal, rangeVal),
                        random(-rangeVal, rangeVal));
                    outputVectors[i] = random(1) < density.getValuef() ? ov : null;
                }
            }
        }
        return true;
    }
}
