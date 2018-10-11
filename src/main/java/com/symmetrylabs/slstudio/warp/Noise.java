package com.symmetrylabs.slstudio.warp;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;
import heronarts.lx.warp.LXWarp;

import static com.symmetrylabs.util.MathUtils.*;


public class Noise extends LXWarp {

    private final float RANGE = 75;

    private CompoundParameter amount = new CompoundParameter("amt", .3f);

    public Noise(LX lx) {
        super(lx);
        addParameter(amount);
    }

    public boolean run(double deltaMs, boolean inputVectorsChanged) {
        float range = RANGE * amount.getValuef();

        for (int i = 0; i < inputVectors.length; i++) {
            LXVector iv = inputVectors[i];
            if (iv == null) {
                outputVectors[i] = null;
            } else {
                LXVector ov = new LXVector(iv);
                ov.add(random(-range, range),
                    random(-range, range),
                outputVectors[i] = ov;
            }
        }
        return true;
    }
}
