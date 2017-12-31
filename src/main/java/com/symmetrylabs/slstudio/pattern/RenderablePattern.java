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

public abstract class RenderablePattern extends SLPattern implements Renderable {
    private Renderer renderer;
    private boolean isManaged = false;

    protected RenderablePattern(LX lx) {
        super(lx);

        renderer = new InterpolatingRenderer(lx.model, colors, this);
        //renderer = new SequentialRenderer(lx.model, colors, this);

        createParameters();
    }

    public Renderer getRenderer() {
        return renderer;
    }
    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    public void setManagedMode(boolean isManaged) {
        boolean wasManaged = this.isManaged;
        this.isManaged = isManaged;

        if (wasManaged && !isManaged) {
            onActive();
        }
        else if (!wasManaged && isManaged) {
            onInactive();
        }
    }

    @Override
    public void onActive() {
        super.onActive();

        if (!isManaged) {
            renderer.start();
        }
    }

    @Override
    public void onInactive() {
        super.onInactive();

        renderer.stop();
    }

    @Override
    public void run(final double deltaMs) {
        if (!isManaged) {
            renderer.run(deltaMs);
        }
        else {
            render(deltaMs, model.getPoints(), colors);
        }
    }

    protected void createParameters() { }

    public abstract void render(double deltaMs, List<LXPoint> points, int[] layer);
}
