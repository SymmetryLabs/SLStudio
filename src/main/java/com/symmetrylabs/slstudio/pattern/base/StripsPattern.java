package com.symmetrylabs.slstudio.pattern.base;

import com.symmetrylabs.slstudio.pattern.base.ModelSpecificPattern;
import heronarts.lx.LX;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;

public abstract class StripsPattern extends ModelSpecificPattern<StripsModel<Strip>> {
    @Override
    protected StripsModel createEmptyModel() {
        return new StripsModel();
    }

    public StripsPattern(LX lx) {
        super(lx);
    }
}
