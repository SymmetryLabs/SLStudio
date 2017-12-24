package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;

import com.symmetrylabs.slstudio.model.SLModel;

public abstract class SunsEffect extends ModelSpecificEffect<SLModel> {
    @Override
    protected SLModel createEmptyModel() {
        return new SLModel();
    }

    public SunsEffect(LX lx) {
        super(lx);
    }
}
