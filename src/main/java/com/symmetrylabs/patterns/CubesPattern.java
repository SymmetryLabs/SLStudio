package com.symmetrylabs.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;

import com.symmetrylabs.CubesModel;

public abstract class CubesPattern extends LXPattern {
    public final CubesModel model;

    public CubesPattern(LX lx) {
        super(lx);
        if (lx.model instanceof CubesModel) {
            this.model = (CubesModel)lx.model;
        } else {
            this.model = new CubesModel();
        }
    }
}
