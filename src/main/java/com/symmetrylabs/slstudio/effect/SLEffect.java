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
    public Class getModelClass() {
        return getEmptyModel().getClass();
    }

    /** Gets an empty instance of the model class, M. */
    private M getEmptyModel() {
        TypeToken tt = new TypeToken<M>(getClass()) {};
        /* if it's a type variable, then SLEffect was instantiated without a type parameter,
             so we just assume the effect works on all SLModels. */
        if (tt.getType() instanceof TypeVariable) {
            return (M) SLModel.getModelWithoutModelIDForTypeTesting();
        }

        String modelClassName = tt.getType().getTypeName();
        String rawModelClassName = modelClassName.replaceAll("<.*", "");
        M emptyModel;
        try {
            emptyModel = (M) Class.forName(rawModelClassName).getConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                         InstantiationException | InvocationTargetException | ClassCastException e) {
            throw new RuntimeException(
                "Could not find a public default constructor for " + modelClassName + ": " + e);
        }
        return emptyModel;
    }

    public M getModel() {
        return model;
    }

    @Override
    public LXModelComponent setModel(LXModel model) {
        M emptyModel = getEmptyModel();

        try {
            if (emptyModel.getClass().isAssignableFrom(model.getClass())) {
                this.model = (M) model;
            }
            else {
                this.model = emptyModel;
            }
        } catch (ClassCastException e) {
            this.model = emptyModel;
        }

        return super.setModel(model);
    }
}
