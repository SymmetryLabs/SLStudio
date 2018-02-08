package com.symmetrylabs.layouts.cubes;

import com.symmetrylabs.slstudio.effect.ModelSpecificEffect;
import heronarts.lx.LX;

public abstract class CubesEffect extends ModelSpecificEffect<CubesModel> {
    @Override
    protected CubesModel createEmptyModel() {
        return new CubesModel();
    }

    public CubesEffect(LX lx) {
        super(lx);
    }
}
