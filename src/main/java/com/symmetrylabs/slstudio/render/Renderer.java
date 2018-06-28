package com.symmetrylabs.slstudio.render;

import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;

import java.util.ArrayList;
import java.util.List;

public abstract class Renderer {
    protected final LXFixture fixture;
    protected final int[] colors;
    protected final Renderable renderable;
    protected final List<LXVector> vectors;

    public Renderer(LXFixture fixture, int[] colors, Renderable renderable) {
        this.fixture = fixture;
        this.colors = colors;
        this.renderable = renderable;

        vectors = new ArrayList<>();
        for (LXPoint point : fixture.getPoints()) {
            vectors.add(new LXVector(point));
        }
    }

    public void start() { }
    public void stop() { }
    public void run(double deltaMs) { }
}
