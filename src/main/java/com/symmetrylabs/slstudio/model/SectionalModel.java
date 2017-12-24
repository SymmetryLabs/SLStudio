package com.symmetrylabs.slstudio.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXFixture;

import static com.symmetrylabs.slstudio.util.MathUtils.floor;

/**
 * A model with multiple disconnected parts.
 */
public interface SectionalModel<T extends LXModel> extends LXFixture {
    public List<T> getSections();
}
