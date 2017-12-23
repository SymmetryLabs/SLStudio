package com.symmetrylabs.slstudio.pattern;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.lang.reflect.Modifier;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

import com.symmetrylabs.slstudio.model.Sun;
import com.symmetrylabs.slstudio.render.Renderer;
import com.symmetrylabs.slstudio.render.InterpolatingRenderer;
import com.symmetrylabs.slstudio.render.SequentialRenderer;
import com.symmetrylabs.slstudio.render.Renderable;

public abstract class PerSunPattern extends SunsPattern {
    protected List<Subpattern> subpatterns;

    private Renderer renderer;

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

        subpatterns = new ArrayList<Subpattern>(model.getSuns().size());

        createParameters();

        int sunIndex = 0;
        for (Sun sun : model.getSuns()) {
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

        renderer = new InterpolatingRenderer(lx.model, colors, renderable);
        //renderer = new SequentialRenderer(lx.model, colors, renderable);
    }

    @Override
    public void onActive() {
        super.onActive();

        renderer.start();
    }

    @Override
    public void onInactive() {
        super.onInactive();

        renderer.stop();
    }

    @Override
    public void run(final double deltaMs) {
        renderer.run(deltaMs);
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
