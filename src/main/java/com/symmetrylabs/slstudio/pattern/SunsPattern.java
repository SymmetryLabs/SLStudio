package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;

import com.symmetrylabs.slstudio.model.SunsModel;

public abstract class SunsPattern extends ModelSpecificPattern<SunsModel> {
    @Override
    protected SunsModel createEmptyModel() {
        return new SunsModel();
    }

    public SunsPattern(LX lx) {
        super(lx);
    }
}
