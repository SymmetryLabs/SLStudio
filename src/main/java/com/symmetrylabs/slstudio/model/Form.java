package com.symmetrylabs.slstudio.model;

import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;

// The notion of an object with no location/orientation info
/*
Symmetry labs wrapper for LXAbstractFixture for defining shapes and such
 */
public abstract class Form extends LXAbstractFixture {
    public final String name;

    public Form(String name){
        this.name = name;
    }
}
