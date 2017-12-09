package com.symmetrylabs.pattern;

import com.symmetrylabs.model.Strip;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;

import java.util.function.Consumer;

import static processing.core.PApplet.*;


public class Swarm extends SLPattern {

    SawLFO offset = new SawLFO(0, 1, 1000);
    SinLFO rate = new SinLFO(350, 1200, 63000);
    SinLFO falloff = new SinLFO(15, 50, 17000);
    SinLFO fX = new SinLFO(model.xMin, model.xMax, 19000);
    SinLFO fY = new SinLFO(model.yMin, model.yMax, 11000);
    SinLFO hOffX = new SinLFO(model.xMin, model.xMax, 13000);

    public Swarm(LX lx) {
        super(lx);

        addModulator(offset).trigger();
        addModulator(rate).trigger();
        addModulator(falloff).trigger();
        addModulator(fX).trigger();
        addModulator(fY).trigger();
        addModulator(hOffX).trigger();
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

    public void run(double deltaMs) {
        float s = 0;
        model.strips.parallelStream().forEach(new Consumer<Strip>() {
            @Override
            public void accept(final Strip strip) {
                float s = model.strips.indexOf(strip);
                int i = 0;
                for (LXPoint p : strip.points) {
                    float fV = max(-1, 1 - dist(p.x / 2f, p.y, fX.getValuef() / 2f, fY.getValuef()) / 64f);
                    // println("fv: " + fV);
                    colors[p.index] = lx.hsb(
                        palette.getHuef() + 0.3f * abs(p.x - hOffX.getValuef()),
                        constrain(80 + 40 * fV, 0, 100),
                        constrain(100 -
                            (30 - fV * falloff.getValuef()) * modDist(
                                i + (s * 63) % 61,
                                offset.getValuef() * strip.metrics.numPoints,
                                strip.metrics.numPoints
                            ), 0, 100)
                    );
                    ++i;
                }
            }
        });
    }
}
