package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.pattern.base.StripsPattern;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;

import static processing.core.PApplet.*;

public class Swarm extends StripsPattern {
    private final SawLFO offset = new SawLFO(0, 1, 1000);
    private final SinLFO rate = new SinLFO(350, 1200, 63000);
    private final SinLFO falloff = new SinLFO(15, 50, 17000);
    private final SinLFO fX = new SinLFO(model.xMin, model.xMax, 19000);
    private final SinLFO fY = new SinLFO(model.yMin, model.yMax, 11000);
    private final SinLFO hOffX = new SinLFO(model.xMin, model.xMax, 13000);

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
        for (Strip strip : model.getStrips()) {
          int i = 0;
          for (LXPoint p : strip.points) {
            float fV = max(-1, 1 - dist(p.x/2.f, p.y, fX.getValuef()/2.f, fY.getValuef()) / 64.f);
           // println("fv: " + fV);
            colors[p.index] = lx.hsb(
            palette.getHuef() + 0.3f * abs(p.x - hOffX.getValuef()),
            constrain(80 + 40 * fV, 0, 100),
            constrain(100 -
              (30 - fV * falloff.getValuef()) * modDist(i + (s*63)%61, offset.getValuef() * strip.metrics.numPoints, strip.metrics.numPoints), 0, 100)
              );
            ++i;
          }
          ++s;
        }
    }
}
