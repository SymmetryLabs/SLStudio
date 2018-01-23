package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;

public abstract class StripsEffect extends ModelSpecificEffect<StripsModel<Strip>> {
    @Override
    protected StripsModel createEmptyModel() {
        return new StripsModel();
    }

    public StripsEffect(LX lx) {
        super(lx);
    }
}
