package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;

import com.symmetrylabs.slstudio.model.SLModel;

public abstract class SunsPattern extends LXPattern {
    public final SLModel model;

    public SunsPattern(LX lx) {
        super(lx);

        if (lx.model instanceof SLModel) {
            this.model = (SLModel)lx.model;
        } else {
            this.model = new SLModel();
        }
    }
}
