package com.symmetrylabs.shows.pilots;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.ADSREnvelope;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

public class AngleStrobe extends SLPattern<SLModel> {
    public static final String GROUP_NAME = PilotsShow.SHOW_NAME;

    private final BooleanParameter trigger = new BooleanParameter("trigger", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private final BooleanParameter reset = new BooleanParameter("reset", false).setMode(BooleanParameter.Mode.MOMENTARY);

    private final CompoundParameter normalXParam = new CompoundParameter("x", 0.9, 0, 1);
    private final CompoundParameter normalYParam = new CompoundParameter("y", 0.3, 0, 1);
    private final CompoundParameter normalZParam = new CompoundParameter("z", 0, 0, 1);
    private final CompoundParameter distParam = new CompoundParameter("dist", 96, 10, 600);
    private final CompoundParameter thickParam = new CompoundParameter("thick", 19, 0, 60);
    private final CompoundParameter offsetParam = new CompoundParameter("offset", 0, 0, 120);

    private CompoundParameter attack = new CompoundParameter("attack", 0, 100, 500);
    private CompoundParameter decay = new CompoundParameter("decay", 30, 0, 500);
    private CompoundParameter sustain = new CompoundParameter("sustain", 0.95, 0, 1);
    private CompoundParameter release = new CompoundParameter("release", 300, 0, 2000);

    private ADSREnvelope adsr = new ADSREnvelope("adsr", 0, 1, attack, decay, sustain, release);

    int generation = 0;

    public AngleStrobe(LX lx) {
        super(lx);
        addParameter(trigger);
        addParameter(reset);

        addParameter(normalXParam);
        addParameter(normalYParam);
        addParameter(normalZParam);
        addParameter(distParam);
        addParameter(thickParam);
        addParameter(offsetParam);

        addParameter(attack);
        addParameter(decay);
        addParameter(sustain);
        addParameter(release);

        addModulator(adsr);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == trigger && trigger.getValueb()) {
            generation++;
            adsr.attack();
        } else if (p == trigger) {
            adsr.release();
        }

        if (p == reset && reset.getValueb()) {
            generation = 0;
        }
    }

    @Override
    public void run(double elapsedMs) {
        for (int i = 0; i < colors.length; i++) {
            colors[i] = LXColor.BLACK;
        }

        LXVector normal = new LXVector(normalXParam.getValuef(), normalYParam.getValuef(), normalZParam.getValuef());
        normal.normalize();

        float dist = distParam.getValuef();
        float thick = thickParam.getValuef();
        float off = offsetParam.getValuef();
        LXVector negCenter = new LXVector(-model.cx, -model.cy, -model.cz);

        for (LXVector v : getVectors()) {
            LXVector cv = v.copy().add(negCenter);
            float proj = normal.dot(cv) + off;
            int count = 0;
            while (proj > dist) {
                proj -= dist;
                count++;
            }
            while (proj < 0) {
                proj += dist;
                count++;
            }

            if (proj < thick && count <= generation) {
                colors[v.index] = LXColor.gray(100 * adsr.getValue());
            }
        }
    }
}
