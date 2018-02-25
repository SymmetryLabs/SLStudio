package com.symmetrylabs.slstudio.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import heronarts.lx.model.LXFixture;

import javax.xml.crypto.Data;

/**
 * A model with strips.
 */
public abstract class StripsModel<T extends Strip> extends SLModel {
    protected final List<T> strips = new ArrayList<>();
    protected final List<T> stripsUnmodifiable = Collections.unmodifiableList(strips);

    // This is a single data injection point.
    public final List<DataChannel> dataChannels = new ArrayList<>();

    protected StripsModel(LXFixture fixture) {
        super(fixture);
    }

    protected StripsModel(LXFixture[] fixtures) {
        super(fixtures);
    }

    public List<T> getStrips() {
        return stripsUnmodifiable;
    }

    public List<DataChannel> getChannels() {
        return dataChannels;
    }

    public static class Empty extends StripsModel {
        public Empty() {
            super(new LXFixture[0]);
        }
    }
}
