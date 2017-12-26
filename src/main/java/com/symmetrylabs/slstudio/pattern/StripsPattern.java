package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;

import com.symmetrylabs.slstudio.model.StripsModel;

public abstract class StripsPattern extends LXPattern {
    public final StripsModel<?> model;

    public StripsPattern(LX lx) {
        super(lx);

        if (lx.model instanceof StripsModel<?>) {
            this.model = (StripsModel<?>)lx.model;
        } else {
            this.model = new StripsModel.Empty();
        }
    }
}
