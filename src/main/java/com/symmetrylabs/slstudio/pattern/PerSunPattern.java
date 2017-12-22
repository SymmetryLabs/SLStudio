package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.Sun;
import com.symmetrylabs.slstudio.render.InterpolatingRenderer;
import com.symmetrylabs.slstudio.render.Renderable;
import com.symmetrylabs.slstudio.render.Renderer;
import com.symmetrylabs.slstudio.render.SequentialRenderer;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public abstract class PerSunPattern extends SLPattern {
    protected List<Subpattern> subpatterns;

    private Object rendererLock = new Object();
    private Renderer renderer;

    public static enum RendererChoices {
        SEQUENTIAL, INTERPOLATING
    }

    EnumParameter<RendererChoices> chooseRenderer;

    private Renderable renderable = new Renderable() {
        @Override
        public void render(final double deltaMs, List<LXPoint> ignore, final int[] layer) {
            // System.out.println(1000 / deltaMs);
            subpatterns.parallelStream().forEach(subpattern -> {
                if (subpattern.enableParam.getValueb()) {
                    subpattern.run(deltaMs, subpattern.sun.getPoints(), layer);
                }
                else {
                    for (LXPoint point : subpattern.sun.points) {
                        layer[point.index] = 0;
                    }
                }
            });
        }
    };

    protected void createParameters() { }
    protected abstract Subpattern createSubpattern(Sun sun, int sunIndex);

    protected PerSunPattern(LX lx) {
        super(lx);

        subpatterns = new ArrayList<Subpattern>(model.suns.size());

        createParameters();

        int sunIndex = 0;
        for (Sun sun : model.suns) {
            try {
                Subpattern subpattern = createSubpattern(sun, sunIndex);
                addParameter(subpattern.enableParam);
                subpatterns.add(subpattern);
            }
            catch (Exception e) {
                System.err.println("Exception when creating subpattern: " + e.getLocalizedMessage());
                e.printStackTrace();
            }

            ++sunIndex;
        }

        addParameter(chooseRenderer = new EnumParameter<RendererChoices>("renderer", RendererChoices.SEQUENTIAL));

        renderer = new SequentialRenderer(lx.model, colors, renderable);

        chooseRenderer.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter ignore) {
                synchronized (rendererLock) {
                    renderer.stop();

                    switch (chooseRenderer.getEnum()) {
                        case SEQUENTIAL:
                            renderer = new SequentialRenderer(lx.model, colors, renderable);
                            break;
                        case INTERPOLATING:
                            renderer = new InterpolatingRenderer(lx.model, colors, renderable);
                            break;
                    }

                    renderer.start();
                }
            }
        });
    }

    @Override
    public void onActive() {
        super.onActive();

        synchronized (rendererLock) {
            renderer.start();
        }
    }

    @Override
    public void onInactive() {
        super.onInactive();

        synchronized (rendererLock) {
            renderer.stop();
        }
    }

    @Override
    public void run(final double deltaMs) {
        renderer.run(deltaMs);
      /*
        subpatterns.parallelStream().forEach(new Consumer<Subpattern>() {
            public void accept(Subpattern subpattern) {
                if (subpattern.enableParam.getValueb()) {
                    subpattern.run(deltaMs, subpattern.sun.getPoints(), colors);
                }
                else {
                    for (LXPoint point : subpattern.sun.points) {
                        colors[point.index] = 0;
                    }
                }
            }
        });
        */
    }

    public static abstract class Subpattern {
        protected final Sun sun;
        protected final int sunIndex;
        protected final BooleanParameter enableParam;

        public Subpattern(Sun sun, int sunIndex) {
            this.sun = sun;
            this.sunIndex = sunIndex;

            enableParam = new BooleanParameter("SUN" + (sunIndex + 1), true);
        }

        protected abstract void run(double deltaMs, List<LXPoint> points, int[] layer);
    }
}
