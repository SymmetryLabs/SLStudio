package com.symmetrylabs.slstudio.warp;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;

import com.symmetrylabs.slstudio.model.SLModel;
import static com.symmetrylabs.util.MathUtils.*;


public class Speckle extends SLWarp<SLModel> {

    private final float RANGE = 50;

    private CompoundParameter amount = new CompoundParameter("amt", .3f);

    public Speckle(LX lx) {
        super(lx);
        addParameter(amount);
    }

    public boolean run(double deltaMs, boolean inputVectorsChanged) {
        if (inputVectorsChanged || getAndClearParameterChangeDetectedFlag()) {
            float range = RANGE * amount.getValuef();

            for (int i = 0; i < inputVectors.length; i++) {
                LXVector iv = inputVectors[i];
                if (iv == null) {
                    outputVectors[i] = null;
                } else {
                    LXVector ov = new LXVector(iv);
                    ov.add(random(-range, range),
                             random(-range, range),
                             random(-range, range));
                    outputVectors[i] = ov;
                }
            }
            return true;
        }
        return false;
    }
}
