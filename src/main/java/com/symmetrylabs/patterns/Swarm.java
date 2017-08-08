package com.symmetrylabs.patterns;

import heronarts.lx.LX;
import heronarts.lx.parameter.*;
import heronarts.lx.modulator.*;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.CubesModel;
import com.symmetrylabs.util.MathUtils;

public class Swarm extends CubesPattern {

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
            return Math.min(v2-v1, v1+mod-v2);
        }
        else {
            return Math.min(v1-v2, v2+mod-v1);
        }
    }

    @Override
    public void run(double deltaMs) {
        float s = 0;
        for (CubesModel.Strip strip : model.strips) {
            int i = 0;
            for (LXPoint p : strip.points) {
                float fV = Math.max(-1, 1 - MathUtils.dist(p.x / 2f, p.y, fX.getValuef() / 2f, fY.getValuef()) / 64f);
             // println("fv: " + fV);
                colors[p.index] = lx.hsb(
                    palette.getHuef() + 0.3f * (float)Math.abs(p.x - hOffX.getValuef()),
                    MathUtils.constrain(80 + 40 * fV, 0, 100),
                    MathUtils.constrain(100 - (30 - fV * falloff.getValuef()) * modDist(i + (s * 63) % 61, offset.getValuef() * strip.metrics.numPoints, strip.metrics.numPoints), 0, 100)
                );
                ++i;
            }
            ++s;
        }
    }
}
