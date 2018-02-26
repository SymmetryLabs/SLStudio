package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;

public class PerSunBassPod extends PerSunPattern {
    public PerSunBassPod(LX lx) {
        super(lx);
    }

    @Override
    protected SLPattern createSubpattern(LXModel model, int modelIndex) {
        BassPod pattern = new BassPod(lx);
        pattern.setModel(model);
        return pattern;
    }
}
