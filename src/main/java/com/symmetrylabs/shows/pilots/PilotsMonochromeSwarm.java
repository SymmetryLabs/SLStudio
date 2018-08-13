package com.symmetrylabs.shows.pilots;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;

import static heronarts.lx.PolyBuffer.Space.SRGB8;
import static processing.core.PApplet.constrain;
import static processing.core.PApplet.min;

public class PilotsMonochromeSwarm extends SLPattern<StripsModel<Strip>> {
    private final CompoundParameter fVParameter = new CompoundParameter("fV", 0, -1, 1);
    private final SawLFO offset = new SawLFO(0, 1, 1000);
    private final SinLFO rate = new SinLFO(350, 1200, 63000);
    private final SinLFO falloff = new SinLFO(15, 35, 17000);

    public PilotsMonochromeSwarm(LX lx) {
        super(lx);
        addModulator(offset).trigger();
        addModulator(rate).trigger();
        addModulator(falloff).trigger();
        addParameter(fVParameter);

        offset.setPeriod(rate);
    }

    float modDist(float v1, float v2, float mod) {
        v1 = v1 % mod;
        v2 = v2 % mod;
        if (v2 > v1) {
            return min(v2 - v1, v1 + mod - v2);
        } else {
            return min(v1 - v2, v2 + mod - v1);
        }
    }

    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);
        float fV = fVParameter.getValuef();
        float s = 0;

        for (Strip strip : model.getStrips()) {
            int i = 0;
            for (LXVector p : getVectors(strip.points)) {
                float bright = 100 - (30 - fV * falloff.getValuef())
                                                         * modDist(i + (s * 63) % 61,
                                                                             offset.getValuef() * strip.metrics.numPoints,
                                                                             strip.metrics.numPoints);
                colors[p.index] = LXColor.hsb(0, 0, constrain(bright, 0, 100));
                ++i;
            }
            ++s;
        }
        markModified(SRGB8);
    }
}
