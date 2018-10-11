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
    private final BooleanParameter wipe = new BooleanParameter("wipe", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private final BooleanParameter hit = new BooleanParameter("hit", false).setMode(BooleanParameter.Mode.MOMENTARY);

    private final CompoundParameter normalXParam = new CompoundParameter("x", 0.9, 0, 1);
    private final CompoundParameter normalYParam = new CompoundParameter("y", 0.3, 0, 1);
    private final CompoundParameter normalZParam = new CompoundParameter("z", 0, 0, 1);
    private final CompoundParameter distParam = new CompoundParameter("dist", 96, 10, 600);
    private final CompoundParameter thickParam = new CompoundParameter("thick", 19, 0, 60);
    private final CompoundParameter offsetParam = new CompoundParameter("offset", 0, 0, 120);
    private final CompoundParameter wipeTime = new CompoundParameter("twipe", 350, 0, 1500);

    private CompoundParameter attack = new CompoundParameter("attack", 0, 10, 500);
    private CompoundParameter decay = new CompoundParameter("decay", 30, 0, 500);
    private CompoundParameter sustain = new CompoundParameter("sustain", 0.95, 0, 1);
    private CompoundParameter release = new CompoundParameter("release", 300, 0, 2000);

    private ADSREnvelope adsr = new ADSREnvelope("adsr", 0, 1, attack, decay, sustain, release);

    int generation = 0;
    boolean wiping = false;
    double wipeAge = 0;

    public AngleStrobe(LX lx) {
        super(lx);
        addParameter(trigger);
        addParameter(reset);
        addParameter(wipe);
        addParameter(hit);

        addParameter(normalXParam);
        addParameter(normalYParam);
        addParameter(normalZParam);
        addParameter(distParam);
        addParameter(thickParam);
        addParameter(offsetParam);
        addParameter(wipeTime);

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

        if (p == hit && hit.getValueb()) {
            adsr.attack();
        } else if (p == hit) {
            adsr.release();
        }

        if (p == wipe && wipe.getValueb()) {
            wiping = true;
            wipeAge = 0;
        }

        if (p == reset && reset.getValueb()) {
            generation = -1;
        }
    }

    @Override
    public void run(double elapsedMs) {
        for (int i = 0; i < colors.length; i++) {
            colors[i] = LXColor.BLACK;
        }

        float xyRad = model.rMax;
        if (wiping) {
            wipeAge += elapsedMs;
            if (wipeAge > wipeTime.getValue()) {
                wiping = false;
                generation = -1;
            }
            xyRad *= Double.max(0, 1 - wipeAge / wipeTime.getValue());
        }
        double xyRadSq = Math.pow(xyRad, 2);

        LXVector normal = new LXVector(normalXParam.getValuef(), normalYParam.getValuef(), normalZParam.getValuef());
        normal.normalize();

        float dist = distParam.getValuef();
        float thick = thickParam.getValuef();
        float off = offsetParam.getValuef();
        LXVector negCenter = new LXVector(-model.cx, -model.cy, -model.cz);

        for (LXVector v : getVectors()) {
            LXVector cv = v.copy().add(negCenter);
            double xy = Math.pow(cv.x, 2) + Math.pow(cv.y, 2);
            if (xy > xyRadSq) {
                continue;
            }

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
