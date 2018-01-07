package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXModelComponent;
import heronarts.lx.model.LXModel;

public abstract class ModelSpecificPattern<T extends LXModel> extends SLPattern {
    protected T model;

    protected abstract T createEmptyModel();

    public T getModel() {
        return model;
    }

    @Override
    public LXModelComponent setModel(LXModel model) {
        T emptyModel = createEmptyModel();

        try {
            if (emptyModel.getClass().isAssignableFrom(model.getClass())) {
                this.model = (T)model;
            }
            else {
                this.model = emptyModel;
            }
        } catch (ClassCastException e) {
            this.model = emptyModel;
        }

        return super.setModel(model);
    }

    public ModelSpecificPattern(LX lx) {
        super(lx);

        setModel(lx.model);
    }
}
