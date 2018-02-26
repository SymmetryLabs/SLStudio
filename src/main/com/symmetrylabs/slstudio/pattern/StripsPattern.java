package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;

public abstract class StripsPattern extends ModelSpecificPattern<StripsModel<Strip>> {
    @Override
    protected StripsModel createEmptyModel() {
        return new StripsModel.Empty();
    }

    public StripsPattern(LX lx) {
        super(lx);
    }
}
