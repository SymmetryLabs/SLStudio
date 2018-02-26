package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;

import com.symmetrylabs.slstudio.model.SLModel;

public abstract class SLModelEffect extends ModelSpecificEffect<SLModel> {
    @Override
    protected SLModel createEmptyModel() {
        return new SLModel();
    }

    public SLModelEffect(LX lx) {
        super(lx);
    }
}
