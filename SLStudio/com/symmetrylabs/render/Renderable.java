package com.symmetrylabs.render;

import heronarts.lx.model.LXPoint;

import java.util.List;

public interface Renderable {
    public void render(double deltaMs, List<LXPoint> points, int[] layer);
}
