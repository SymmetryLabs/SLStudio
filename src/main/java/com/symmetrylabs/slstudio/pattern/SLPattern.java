package com.symmetrylabs.slstudio.pattern;

import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.LXBuffer;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;

import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.render.Renderer;
import com.symmetrylabs.slstudio.render.InterpolatingRenderer;
import com.symmetrylabs.slstudio.render.SequentialRenderer;
import com.symmetrylabs.slstudio.render.Renderable;


public abstract class SLPattern extends LXPattern implements Renderable {

    protected final SLStudioLX lx;

    private Renderer renderer;
    private ReusableBuffer reusableBuffer = new ReusableBuffer();
    private boolean isManaged = false;

    protected void createParameters() { }

    public SLPattern(LX lx) {
        super(lx);

        this.lx = (SLStudioLX)lx;

        renderer = new InterpolatingRenderer(lx.model, colors, this);

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
    public void dispose() {
        super.dispose();

        onInactive();
    }

    @Override
    protected void run(double deltaMs) {
        if (!isManaged) {
            renderer.run(deltaMs);
        }
        else {
            render(deltaMs, model.getPoints(), colors);
        }
    }

    @Override
    public void render(double deltaMs, List<LXPoint> points, int[] layer) { }

    protected <T extends LXParameter> T addParam(T param) {
        addParameter(param);
        return param;
    }

    protected BooleanParameter booleanParam(String name) {
        return addParam(new BooleanParameter(name));
    }

    protected BooleanParameter booleanParam(String name, boolean value) {
        return addParam(new BooleanParameter(name, value));
    }

    protected CompoundParameter compoundParam(String name, double value, double min, double max) {
        return addParam(new CompoundParameter(name, value, min, max));
    }

    protected DiscreteParameter discreteParameter(String name, int value, int min, int max) {
        return addParam(new DiscreteParameter(name, value, min, max));
    }

    public SLPattern setBuffer(int[] buffer) {
        reusableBuffer.setArray(buffer);
        return (SLPattern)setBuffer(reusableBuffer);
    }

    private class ReusableBuffer implements LXBuffer {
        private int[] layer;

        @Override
        public int[] getArray() {
            return layer;
        }

        public void setArray(int[] layer) {
            this.layer = layer;
        }
    };
}
