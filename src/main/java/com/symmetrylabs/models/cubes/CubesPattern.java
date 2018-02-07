package com.symmetrylabs.models.cubes;

import com.symmetrylabs.slstudio.pattern.base.ModelSpecificPattern;
import heronarts.lx.LX;

import com.symmetrylabs.models.cubes.CubesModel;

public abstract class CubesPattern extends ModelSpecificPattern<CubesModel> {
    @Override
    protected CubesModel createEmptyModel() {
        return new CubesModel();
    }

    public CubesPattern(LX lx) {
        super(lx);
    }
}
