package com.symmetrylabs.slstudio.pattern.base;

import com.symmetrylabs.slstudio.pattern.base.ModelSpecificPattern;
import heronarts.lx.LX;

import com.symmetrylabs.slstudio.model.SLModel;

public abstract class SLModelPattern extends ModelSpecificPattern<SLModel> {
    @Override
    protected SLModel createEmptyModel() {
        return new SLModel();
    }

    public SLModelPattern(LX lx) {
        super(lx);
    }
}
