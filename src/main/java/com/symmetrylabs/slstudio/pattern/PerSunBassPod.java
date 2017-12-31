package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;

import com.symmetrylabs.slstudio.model.Sun;

public class PerSunBassPod extends PerSunPattern {
    public PerSunBassPod(LX lx) {
        super(lx);
    }

    @Override
    protected SLPattern createSubpattern(Sun sun, int sunIndex) {
        BassPod pattern = new BassPod(lx);
        pattern.setModel(sun);
        return pattern;
    }
}
