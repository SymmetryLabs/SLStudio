package com.symmetrylabs.slstudio.model;

import heronarts.lx.transform.LXTransform;

//An object placed in the world
public interface GlobalSpaceForm extends Form {
    LXTransform getTransform();
}
