package com.symmetrylabs.slstudio.pattern;

import java.util.List;
import java.util.Collections;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.LXParameter;

import com.symmetrylabs.slstudio.render.Renderer;
import com.symmetrylabs.slstudio.render.Renderable;
import com.symmetrylabs.slstudio.render.InterpolatingRenderer;
import com.symmetrylabs.slstudio.render.SequentialRenderer;

public abstract class SubmodelPattern extends SLPattern implements Renderable {
    private Renderer renderer;

    protected SubmodelPattern(LX lx) {
        super(lx);

        //renderer = new InterpolatingRenderer(lx.model, colors, this);
        renderer = new SequentialRenderer(lx.model, colors, this);

        createParameters();
    }

    public void onUIStart() { }
    public void onUIEnd() { }

    @Override
    public void onActive() {
        super.onActive();

        onUIStart();

        renderer.start();
    }

    @Override
    public void onInactive() {
        super.onInactive();

        onUIEnd();

        renderer.stop();
    }

    @Override
    public void run(final double deltaMs) {
        renderer.run(deltaMs);
    }

    protected void createParameters() { }

    public abstract void render(double deltaMs, List<LXPoint> points, int[] layer);
}
