package com.symmetrylabs.patterns;

import heronarts.lx.LX;
import heronarts.lx.parameter.*;
import heronarts.lx.modulator.*;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.CubesModel;
import com.symmetrylabs.util.MathUtils;

public class SpaceTime extends CubesPattern {

    SinLFO pos = new SinLFO(0, 1, 3000);
    SinLFO rate = new SinLFO(1000, 9000, 13000);
    SinLFO falloff = new SinLFO(10, 70, 5000);
    float angle = 0;

    CompoundParameter rateParameter = new CompoundParameter("RATE", 0.5);
    CompoundParameter sizeParameter = new CompoundParameter("SIZE", 0.5);

    public SpaceTime(LX lx) {
        super(lx);

        addModulator(  pos).trigger();
        addModulator(rate).trigger();
        addModulator(falloff).trigger();
        pos.setPeriod(rate);
        addParameter(rateParameter);
        addParameter(sizeParameter);
    }

    @Override
    public void onParameterChanged(LXParameter parameter) {
        if (parameter == rateParameter) {
            rate.stop();
            rate.setValue(9000 - 8000*parameter.getValuef());
        }  else if (parameter == sizeParameter) {
            falloff.stop();
            falloff.setValue(70 - 60*parameter.getValuef());
        }
    }

    @Override
    public void run(double deltaMs) {
        angle += deltaMs * 0.0007f;
        float sVal1 = model.strips.size() * (0.5f + 0.5f * (float)Math.sin(angle));
        float sVal2 = model.strips.size() * (0.5f + 0.5f * (float)Math.cos(angle));

        float pVal = pos.getValuef();
        float fVal = falloff.getValuef();

        int s = 0;
        for (CubesModel.Strip strip : model.strips) {
            int i = 0;
            for (LXPoint p : strip.points) {
                colors[p.index] = lx.hsb(
                    palette.getHuef() + 360 - p.x * .2f + p.y * .3f,
                    MathUtils.constrain(.4f * Math.min((float)Math.abs(s - sVal1), (float)Math.abs(s - sVal2)), 20, 100),
                    Math.max(0, 100 - fVal * (float)Math.abs(i - pVal*(strip.metrics.numPoints - 1)))
                );
                ++i;
            }
            ++s;
        }
    }
}
