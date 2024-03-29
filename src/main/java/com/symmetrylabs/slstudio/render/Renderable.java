package com.symmetrylabs.slstudio.render;

import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;

import java.util.List;

public interface Renderable {
    void render(double deltaMs, List<LXVector> vectors, int[] layer);
}
