package com.symmetrylabs.slstudio.render;

import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;

import java.util.List;

public abstract class Renderer {
    protected final LXFixture fixture;
    protected final int[] colors;
    protected final Renderable renderable;
    protected final List<LXPoint> points;

    public Renderer(LXFixture fixture, int[] colors, Renderable renderable) {
        this.fixture = fixture;
        this.colors = colors;
        this.renderable = renderable;

        points = fixture.getPoints();
    }

    public void start() {
    }

    public void stop() {
    }

    public void run(double deltaMs) {
    }
}
