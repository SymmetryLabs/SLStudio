package com.symmetrylabs.shows.usopen.patterns;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.transform.LXVector;

import static heronarts.lx.PolyBuffer.Space.SRGB8;
import static processing.core.PApplet.*;

public class USOpenSwarm extends SLPattern<StripsModel<Strip>> {
    private final SawLFO offset = new SawLFO(0, 1, 1000);
    private final SinLFO rate = new SinLFO(350, 1200, 63000);
    private final SinLFO falloff = new SinLFO(15, 50, 17000);
    private final SinLFO fX = new SinLFO(model.xMin, model.xMax, 19000);
    private final SinLFO fY = new SinLFO(model.yMin, model.yMax, 11000);
    private final SinLFO hOffX = new SinLFO(model.xMin, model.xMax, 13000);
    private final CompoundParameter hue = new CompoundParameter("hue", 0, 0, 360);
    private final CompoundParameter hueVariance = new CompoundParameter("hueVar", 0.3f);
    private final CompoundParameter sat = new CompoundParameter("sat", 100, 0, 100);

    public USOpenSwarm(LX lx) {
        super(lx);
        addModulator(offset).trigger();
        addModulator(rate).trigger();
        addModulator(falloff).trigger();
        addModulator(fX).trigger();
        addModulator(fY).trigger();
        addModulator(hOffX).trigger();
        addParameter(hue);
        addParameter(hueVariance);
        addParameter(sat);
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

        float s = 0;
        for (Strip strip : model.getStrips()) {
            int i = 0;
            for (LXVector p : getVectors(strip.points)) {
                float fV = max(-1, 1 - dist(p.x / 2.f, p.y, fX.getValuef() / 2.f, fY.getValuef()) / 64.f);
                // println("fv: " + fV);
                colors[p.index] = LXColor.hsb(
                    hue.getValuef() + 0.3f * abs(p.x - hueVariance.getValuef()),
                    sat.getValuef(),
                    constrain(100 -
                        (30 - fV * falloff.getValuef()) * modDist(i + (s * 63) % 61, offset.getValuef() * strip.metrics.numPoints, strip.metrics.numPoints), 0, 100)
                );
                ++i;
            }
            ++s;
        }
        markModified(SRGB8);
    }
}
