package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;

import com.symmetrylabs.slstudio.model.SLModel;

public abstract class SunsPattern extends ModelSpecificPattern<SLModel> {
    @Override
    protected SLModel createEmptyModel() {
        return new SLModel();
    }

    public SunsPattern(LX lx) {
        super(lx);
    }
}
