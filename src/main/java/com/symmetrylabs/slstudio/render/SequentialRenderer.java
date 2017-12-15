package com.symmetrylabs.slstudio.render;

import heronarts.lx.model.LXFixture;

public class SequentialRenderer extends Renderer {
    public SequentialRenderer(LXFixture fixture, int[] colors, Renderable renderable) {
        super(fixture, colors, renderable);
    }

    @Override
    public void run(double deltaMs) {
        renderable.render(deltaMs, points, colors);
    }
}
