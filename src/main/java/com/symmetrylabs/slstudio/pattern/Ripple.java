package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;
import heronarts.lx.modulator.SinLFO;

public class Ripple extends SLPattern<SLModel> {
    private final CompoundParameter speedParam = new CompoundParameter("speed", 12, -500, 500);
    private final CompoundParameter distParam = new CompoundParameter("dist", 120, 10, 600);
    private final CompoundParameter normalXParam = new CompoundParameter("x", 0.9, -1, 1);
    private final CompoundParameter normalZParam = new CompoundParameter("z", 0.315, -1, 1);
    private final CompoundParameter amplParam = new CompoundParameter("ampl", 40, 0, 120);
    private final CompoundParameter wavelParam = new CompoundParameter("wavel", 40, 0, model.rRange);
    private final CompoundParameter periodParam = new CompoundParameter("period", 8000, 100, 20_000);
    private final SinLFO lfo = new SinLFO(0, 8.f * Math.PI, periodParam);

    boolean flipSpeed = false;
    float off;

    public Ripple(LX lx) {
        super(lx);
        addParameter(speedParam);
        addParameter(distParam);
        addParameter(normalXParam);
        addParameter(normalZParam);
        addParameter(amplParam);
        addParameter(wavelParam);
        addParameter(periodParam);
        startModulator(lfo);

        off = 0;
    }

    @Override
    public void run(double elapsedMs) {
        int black = LXColor.gray(0);
        for (int i = 0; i < colors.length; i++) {
            colors[i] = black;
        }

        LXVector normal = new LXVector(normalXParam.getValuef(), 0, normalZParam.getValuef());
        normal.normalize();
        LXVector wave = normal.copy().cross(0, 1, 0);

        float dist = distParam.getValuef();
        off += (flipSpeed ? -1 : 1) * speedParam.getValuef() / 1000f * (float) elapsedMs;
        off = off % dist;

        for (LXVector vx : getVectors()) {
            LXVector v = normal.copy().mult(
                (float) (Math.cos(2 * Math.PI * wave.dot(vx) / wavelParam.getValue() + lfo.getValuef()) * amplParam.getValue())).add(vx);
            float proj = (float) Math.cos(2 * Math.PI * ((normal.dot(v) + off) % dist) / dist) / 2.f + 0.5f;
            colors[vx.index] = LXColor.gray(100.f * proj);
        }
    }
}
