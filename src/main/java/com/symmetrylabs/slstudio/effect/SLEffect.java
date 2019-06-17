package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.LXModelComponent;
import heronarts.lx.model.LXModel;
import java.lang.reflect.TypeVariable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;

import com.google.common.reflect.TypeToken;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;

public abstract class SLEffect<M extends SLModel> extends LXEffect {
    protected final SLStudioLX sllx;
    protected M model;

    public SLEffect(LX lx) {
        super(lx);

        this.sllx = lx instanceof SLStudioLX ? (SLStudioLX)lx : null;
        setModel(lx.model);
    }

    /** Gets the model class, M. */
    private Class<M> getModelClass() {
        String modelClassName = new TypeToken<M>(getClass()) {}.getType().getTypeName();
        String rawModelClassName = modelClassName.replaceAll("<.*", "");
        try {
            return (Class<M>) Class.forName(rawModelClassName);
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new RuntimeException(
                "Could not find a class for type token " + modelClassName + ": " + e.getMessage());
        }
    }

    public M getModel() {
        return model;
    }

    @Override
    public LXModelComponent setModel(LXModel model) {
        this.model = null;
        try {
            if (getModelClass().isAssignableFrom(model.getClass())) {
                this.model = (M) model;
            }
        } catch (ClassCastException e) {
            System.err.println(String.format(
                "effect %s not compatible with model type %s:",
                getClass().getSimpleName(), model.getClass()));
            e.printStackTrace();
        }
        return super.setModel(model);
    }
}
