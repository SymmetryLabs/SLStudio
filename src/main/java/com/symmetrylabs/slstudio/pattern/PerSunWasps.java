package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;

public class PerSunWasps extends PerSunPattern {
    public PerSunWasps(LX lx) {
        super(lx);
    }

    @Override
    protected SLPattern createSubpattern(LXModel section, int sectionIndex) {
        Wasps pattern = new Wasps(lx);
        pattern.setModel(section);
        return pattern;
    }
}
