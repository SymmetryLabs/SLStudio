package com.symmetrylabs.slstudio.pattern.instruments;

import java.util.List;

import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import heronarts.lx.transform.LXVector;

public interface Emitter {
    Mark emit(Instrument.ParameterSet paramSet, int pitch, double intensity);
    default int getMaxCount() { return 20; }
    default void run(double deltaSec, Instrument.ParameterSet paramSet) { }
    default void render(LXModel model, List<LXVector> vectors, PolyBuffer buffer) { }
}
