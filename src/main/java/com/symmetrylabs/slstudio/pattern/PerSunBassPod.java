package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;

import com.symmetrylabs.slstudio.model.Sun;

public class PerSunBassPod extends PerSunPattern {
    public PerSunBassPod(LX lx) {
        super(lx);
    }

    @Override
    protected RenderablePattern createSubpattern(LXModel model, int sunIndex) {
        BassPod pattern = new BassPod(lx);
        pattern.setModel(model);
        return pattern;
    }
}
