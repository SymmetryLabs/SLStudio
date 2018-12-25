package com.symmetrylabs.slstudio.pattern.instruments;

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.transform.LXVector;

public interface Mark {
    void advance(double deltaSec, double intensity, boolean sustain, double bend);
    default void render(LXModel model, List<LXVector> vectors, PolyBuffer buffer) { }
    boolean isExpired();
    default List<LXModulator> getModulators() { return new ArrayList<>(); }
}
