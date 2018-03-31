package com.symmetrylabs.slstudio.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import heronarts.lx.model.LXFixture;

/**
 * A model with strips.
 */
public class StripsModel<T extends Strip> extends SLModel {
    protected final List<T> strips = new ArrayList<>();
    protected final List<T> stripsUnmodifiable = Collections.unmodifiableList(strips);
    protected final Map<String, Strip> stripTable = new HashMap<>();

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

    public Strip getStripById(String id) {
      return this.stripTable.get(id);
    }
}
