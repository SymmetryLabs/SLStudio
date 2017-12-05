package com.symmetrylabs.render;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.concurrent.Semaphore;

import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXFixture;
import heronarts.lx.color.LXColor;

public class SequentialRenderer extends Renderer {
    public SequentialRenderer(LXFixture fixture, int[] colors, Renderable renderable) {
        super(fixture, colors, renderable);
    }

    @Override
    public void run(double deltaMs) {
        renderable.render(deltaMs, points, colors);
    }
}
