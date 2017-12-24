package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;

import com.symmetrylabs.slstudio.model.Sun;

public class PerSunWasps extends PerSunPattern {
    public PerSunWasps(LX lx) {
        super(lx);
    }

    @Override
    protected RenderablePattern createSubpattern(Sun sun, int sunIndex) {
        Wasps pattern = new Wasps(lx);
        pattern.setModel(sun);
        return pattern;
    }
}
