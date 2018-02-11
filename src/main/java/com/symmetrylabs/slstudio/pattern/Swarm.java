package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.Strip;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;

import java.util.function.Consumer;

import static processing.core.PApplet.*;


public class Swarm extends StripsPattern {
    public final CompoundParameter rate = new CompoundParameter("RATE", 500, 1500, 100);
    public final CompoundParameter hOffX = new CompoundParameter("HUEOFF", model.xMin*0.3, model.xMin, model.xMax);
    public final SawLFO offset = new SawLFO(0, 1, 1000);
    public final SinLFO falloff = new SinLFO(15, 50, 17000);
    public final SinLFO fX = new SinLFO(model.xMin, model.xMax, 19000);
    public final SinLFO fY = new SinLFO(model.yMin, model.yMax, 11000);

    public Swarm(LX lx) {
        super(lx);
        addParameter(rate);
        addParameter(hOffX);
        addModulator(offset).trigger();
        addModulator(falloff).trigger();
        addModulator(fX).trigger();
        addModulator(fY).trigger();
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
        model.getStrips().parallelStream().forEach(new Consumer<Strip>() {
            @Override
            public void accept(final Strip strip) {
                float s = model.getStrips().indexOf(strip);
                int i = 0;
                for (LXPoint p : strip.points) {
                    float fV = max(-1, 1 - dist(p.x / 2f, p.y, fX.getValuef() / 2f, fY.getValuef()) / 64f);
                    // println("fv: " + fV);
                    colors[p.index] = lx.hsb(
                        palette.getHuef() + 0.05f * abs(p.x - hOffX.getValuef()),
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
