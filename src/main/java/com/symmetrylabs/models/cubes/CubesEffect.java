package com.symmetrylabs.models.cubes;

import com.symmetrylabs.slstudio.effect.ModelSpecificEffect;
import heronarts.lx.LX;

import com.symmetrylabs.models.cubes.CubesModel;

public abstract class CubesEffect extends ModelSpecificEffect<CubesModel> {
    @Override
    protected CubesModel createEmptyModel() {
        return new CubesModel();
    }

    public CubesEffect(LX lx) {
        super(lx);
    }
}
