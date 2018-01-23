package com.symmetrylabs.slstudio.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import heronarts.lx.model.LXFixture;

/**
 * A model with strips.
 */
public class StripsModel<T extends Strip> extends SLModel {
    protected final List<T> strips = new ArrayList<>();
    protected final List<T> stripsUnmodifiable = Collections.unmodifiableList(strips);

    public StripsModel() {
        super();
    }

    protected StripsModel(LXFixture fixture) {
        super(fixture);
    }

    protected StripsModel(LXFixture[] fixtures) {
        super(fixtures);
    }

    public List<T> getStrips() {
        return stripsUnmodifiable;
    }
}
