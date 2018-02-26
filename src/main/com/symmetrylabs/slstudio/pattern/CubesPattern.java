package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;

import com.symmetrylabs.slstudio.model.cubes.CubesModel;

public abstract class CubesPattern extends ModelSpecificPattern<CubesModel> {
    @Override
    protected CubesModel createEmptyModel() {
        return new CubesModel();
    }

    public CubesPattern(LX lx) {
        super(lx);
    }
}
