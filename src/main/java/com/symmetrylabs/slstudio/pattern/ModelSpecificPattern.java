package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.LXModelComponent;
import heronarts.lx.model.LXModel;

import com.symmetrylabs.slstudio.model.SLModel;

public abstract class ModelSpecificPattern<T extends LXModel> extends SLPattern {
    protected T model;

    protected abstract T createEmptyModel();

    public T getModel() {
        return model;
    }

    @Override
    public LXModelComponent setModel(LXModel model) {
        try {
            this.model = (T)model;
        } catch (ClassCastException e) {
            this.model = createEmptyModel();
        }

        return super.setModel(model);
    }

    public ModelSpecificPattern(LX lx) {
        super(lx);

        setModel(lx.model);
    }
}
