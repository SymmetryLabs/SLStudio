package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;

import static processing.core.PApplet.*;

public class SpaceTime extends SLPattern<StripsModel<Strip>> {

    SinLFO pos = new SinLFO(0, 1, 3000);
    SinLFO rate = new SinLFO(1000, 9000, 13000);
    SinLFO falloff = new SinLFO(10, 70, 5000);
    float angle = 0;

    CompoundParameter rateParameter = new CompoundParameter("Speed", 0.5);
    CompoundParameter sizeParameter = new CompoundParameter("Size", 0.5);

    public SpaceTime(LX lx) {
        super(lx);

        addModulator(pos).trigger();
        addModulator(rate).trigger();
        addModulator(falloff).trigger();
        pos.setPeriod(rate);
        addParameter(rateParameter);
        addParameter(sizeParameter);
    }

    public void onParameterChanged(LXParameter parameter) {
        if (parameter == rateParameter) {
            rate.stop();
            rate.setValue(9000 - 8000 * parameter.getValuef());
        } else if (parameter == sizeParameter) {
            falloff.stop();
            falloff.setValue(70 - 60 * parameter.getValuef());
        }
    }

    public void run(double deltaMs) {
        angle += deltaMs * 0.0007;
        final float sVal1 = model.getStrips().size() * (0.5f + 0.5f * sin(angle));
        final float sVal2 = model.getStrips().size() * (0.5f + 0.5f * cos(angle));

        final float pVal = pos.getValuef();
        final float fVal = falloff.getValuef();

        model.getStrips().parallelStream().forEach(strip -> {
            int s = model.getStrips().indexOf(strip);

            int i = 0;
            for (LXPoint p : strip.points) {
                colors[p.index] = lx.hsb(
                    palette.getHuef() + 360 - p.x * .2f + p.y * .3f,
                    constrain(.4f * min(abs(s - sVal1), abs(s - sVal2)), 20, 100),
                    max(0, 100 - fVal * abs(i - pVal * (strip.metrics.numPoints - 1)))
                );

                ++i;
            }
        });
    }
}
