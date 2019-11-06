package com.symmetrylabs.slstudio.model;

import heronarts.lx.model.LXModel;

import java.util.Iterator;

// traverse a model
public class ModelEnumerant {
    SLModel model;
    public ModelEnumerant(SLModel model){
        this.model = model;
    }

    public void enumerateMappableFixture(){
        Iterator<? extends LXModel> children = model.getMappableFixtures();
        for (Iterator<? extends LXModel> it = children; it.hasNext(); ) {
            SLModel child = (SLModel) it.next();
        }
    }

}
