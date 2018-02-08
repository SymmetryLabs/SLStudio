package com.symmetrylabs.layouts.cubes.patterns;

import com.symmetrylabs.layouts.cubes.CubesModel;
import com.symmetrylabs.slstudio.pattern.base.ModelSpecificPattern;
import heronarts.lx.LX;

public abstract class CubesPattern extends ModelSpecificPattern<CubesModel> {
    @Override
    protected CubesModel createEmptyModel() {
        return new CubesModel();
    }

    public CubesPattern(LX lx) {
        super(lx);
    }
}
