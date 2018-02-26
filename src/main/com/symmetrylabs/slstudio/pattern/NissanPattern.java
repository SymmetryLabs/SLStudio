package com.symmetrylabs.slstudio.pattern;


    import com.symmetrylabs.slstudio.model.NissanModel;
    import heronarts.lx.LX;

    import com.symmetrylabs.slstudio.model.SunsModel;

public abstract class NissanPattern extends ModelSpecificPattern<NissanModel> {
    @Override
    protected NissanModel createEmptyModel() {
        return new NissanModel();
    }

    public NissanPattern(LX lx) {
        super(lx);
    }
}
