package com.symmetrylabs.slstudio.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import heronarts.lx.model.LXFixture;

/**
 * A model with strips.
 */
public abstract class StripsModel<T extends Strip> extends SLModel {
    public final List<T> strips = new ArrayList<>();
    protected final List<T> stripsUnmodifiable = Collections.unmodifiableList(strips);

    protected StripsModel(LXFixture fixture) {
        super(fixture);
    }

    protected StripsModel(LXFixture[] fixtures) {
        super(fixtures);
    }

    public List<T> getStrips() {
        return stripsUnmodifiable;
    }

    public static class Empty extends StripsModel {
        public Empty() {
            super(new LXFixture[0]);
        }
    }
}
