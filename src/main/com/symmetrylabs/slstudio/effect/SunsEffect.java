package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;

import com.symmetrylabs.slstudio.model.SunsModel;

public abstract class SunsEffect extends ModelSpecificEffect<SunsModel> {
    @Override
    protected SunsModel createEmptyModel() {
        return new SunsModel();
    }

    public SunsEffect(LX lx) {
        super(lx);
    }
}
