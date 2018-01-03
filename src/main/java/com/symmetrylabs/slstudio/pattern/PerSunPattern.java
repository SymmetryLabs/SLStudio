package com.symmetrylabs.slstudio.pattern;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.lang.reflect.Modifier;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.parameter.LXListenableParameter;
import heronarts.lx.parameter.LXParameterListener;

import com.symmetrylabs.slstudio.render.Renderer;
import com.symmetrylabs.slstudio.render.InterpolatingRenderer;
import com.symmetrylabs.slstudio.render.SequentialRenderer;
import com.symmetrylabs.slstudio.render.Renderable;

public abstract class PerSunPattern extends SectionalPattern {
    protected List<Subpattern> subpatterns;

    private Renderer renderer;

    private Renderable renderable = new Renderable() {
        @Override
        public void render(final double deltaMs, List<LXPoint> ignore, final int[] layer) {
            // System.out.println(1000 / deltaMs);
            subpatterns.parallelStream().forEach(subpattern -> {
                if (subpattern.enableParam.isOn()) {
                    subpattern.pattern.setBuffer(layer);
                    subpattern.pattern.loop(deltaMs);
                }
                else {
                    for (LXPoint point : subpattern.section.points) {
                        layer[point.index] = 0;
                    }
                }
            });
        }
    };

    protected abstract SLPattern createSubpattern(LXModel section, int sectionIndex);

    private void wrapChildParameter(LXParameter param) {
        String name = param.getLabel();
        if (parameters.containsKey(name))
            return;

        LXListenableParameter wrap;

        if (param instanceof CompoundParameter) {
            CompoundParameter src = (CompoundParameter)param;
            CompoundParameter dest = new CompoundParameter(name, param.getValue(),
                    src.range.min, src.range.max);
            wrap = dest;
        }
        else if (param instanceof BooleanParameter) {
            BooleanParameter src = (BooleanParameter)param;
            BooleanParameter dest = new BooleanParameter(name, src.getValueb());
            dest.setMode(src.getMode());
            wrap = dest;
        }
        else if (param instanceof DiscreteParameter) {
            DiscreteParameter src = (DiscreteParameter)param;
            DiscreteParameter dest = new DiscreteParameter(name, src.getValuei(),
                    src.getMinValue(), src.getMaxValue());
            if (src.getOptions() != null) {
                dest.setOptions(src.getOptions());
            }
            wrap = dest;
        }
        else if (param instanceof EnumParameter) {
            EnumParameter src = (EnumParameter)param;
            EnumParameter dest = new EnumParameter(name, src.getEnum());
            wrap = dest;
        }
        else {
            wrap = new MutableParameter(name);
        }

        wrap.setDescription(param.getDescription());

        addParameter(wrap);
    }

    @Override
    public void onParameterChanged(LXParameter param) {
        if (subpatterns == null)
            return;

        for (Subpattern subpattern : subpatterns) {
            LXParameter childParam = subpattern.pattern.getParameter(param.getLabel());
            if (childParam == null)
                continue;

            childParam.setValue(param.getValue());
        }
    }

    protected PerSunPattern(LX lx) {
        super(lx);

        subpatterns = new ArrayList<>(getSections().size());

        int sectionIndex = 0;
        for (LXModel section : getSections()) {
            try {
                Subpattern subpattern = new Subpattern(section, sectionIndex, createSubpattern(section, sectionIndex));

                subpattern.pattern.setManagedMode(true);

                for (LXParameter param : subpattern.pattern.getParameters()) {
                    wrapChildParameter(param);
                }

                addSubcomponent(subpattern.pattern);

                subpatterns.add(subpattern);
            }
            catch (Exception e) {
                System.err.println("Exception when creating subpattern: " + e.getLocalizedMessage());
                e.printStackTrace();
            }

            ++sectionIndex;
        }

        for (Subpattern subpattern : subpatterns) {
            addParameter(subpattern.enableParam);
        }

        renderer = new InterpolatingRenderer(model, colors, renderable);
        //renderer = new SequentialRenderer(model, colors, renderable);
    }

    @Override
    public void onActive() {
        super.onActive();

        for (Subpattern subpattern : subpatterns) {
            if (!subpattern.isActive) {
                subpattern.pattern.onActive();
                subpattern.isActive = true;
            }
        }

        renderer.start();
    }

    @Override
    public void onInactive() {
        super.onInactive();

        for (Subpattern subpattern : subpatterns) {
            if (subpattern.isActive) {
                subpattern.pattern.onInactive();
                subpattern.isActive = false;
            }
        }

        renderer.stop();
    }

    @Override
    public void run(final double deltaMs) {
        renderer.run(deltaMs);
    }

    private static class Subpattern {
        public final LXModel section;
        public final int sectionIndex;
        public final SLPattern pattern;
        public final BooleanParameter enableParam;

        public boolean isActive = false;

        public Subpattern(LXModel section, int sectionIndex, SLPattern pattern) {
            this.section = section;
            this.sectionIndex = sectionIndex;
            this.pattern = pattern;

            enableParam = new BooleanParameter("S" + (sectionIndex + 1), true);
        }
    }
}
