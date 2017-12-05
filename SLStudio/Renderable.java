package com.symmetrylabs.render;

import java.util.List;

import heronarts.lx.model.LXPoint;

public interface Renderable {
    public void render(double deltaMs, List<LXPoint> points, int[] layer);
}
