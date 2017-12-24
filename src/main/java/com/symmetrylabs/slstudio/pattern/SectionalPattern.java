package com.symmetrylabs.slstudio.pattern;

import java.util.List;
import java.util.Collections;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXModel;

import com.symmetrylabs.slstudio.model.SectionalModel;

public abstract class SectionalPattern extends LXPattern {
    public SectionalPattern(LX lx) {
        super(lx);
    }

    public List<LXModel> getSections() {
        if (model instanceof SectionalModel)
            return ((SectionalModel)model).getSections();

        return Collections.emptyList();
    }
}
