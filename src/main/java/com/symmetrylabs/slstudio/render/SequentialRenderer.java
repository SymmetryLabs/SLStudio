package com.symmetrylabs.slstudio.render;

import heronarts.lx.model.LXFixture;

public class SequentialRenderer extends Renderer {
    private volatile boolean isRunning = false;

    public SequentialRenderer(LXFixture fixture, int[] colors, Renderable renderable) {
        super(fixture, colors, renderable);
    }

    @Override
    public void start() {
        isRunning = true;
    }

    @Override
    public void stop() {
        isRunning = false;
    }

    @Override
    public void run(double deltaMs) {
        if (isRunning) {
            renderable.render(deltaMs, points, colors);
        }
    }
}
