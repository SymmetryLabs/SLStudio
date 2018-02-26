package com.symmetrylabs.slstudio.pattern;


    import com.symmetrylabs.slstudio.model.nissan.NissanModel;
    import heronarts.lx.LX;

public abstract class NissanPattern extends ModelSpecificPattern<NissanModel> {
    @Override
    protected NissanModel createEmptyModel() {
        return new NissanModel();
    }

    public NissanPattern(LX lx) {
        super(lx);
    }
}
