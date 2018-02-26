package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;

import com.symmetrylabs.slstudio.model.cubes.CubesModel;

public abstract class CubesEffect extends ModelSpecificEffect<CubesModel> {
    @Override
    protected CubesModel createEmptyModel() {
        return new CubesModel();
    }

    public CubesEffect(LX lx) {
        super(lx);
    }
}
