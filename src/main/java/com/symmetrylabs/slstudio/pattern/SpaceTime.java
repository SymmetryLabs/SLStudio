package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.Strip;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;

import java.util.function.Consumer;

import static processing.core.PApplet.*;


public class SpaceTime extends SLPattern {

    SinLFO pos = new SinLFO(0, 1, 3000);
    SinLFO rate = new SinLFO(1000, 9000, 13000);
    SinLFO falloff = new SinLFO(10, 70, 5000);
    float angle = 0;

    CompoundParameter rateParameter = new CompoundParameter("RATE", 0.5);
    CompoundParameter sizeParameter = new CompoundParameter("SIZE", 0.5);

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
        final float sVal1 = model.strips.size() * (0.5f + 0.5f * sin(angle));
        final float sVal2 = model.strips.size() * (0.5f + 0.5f * cos(angle));

        final float pVal = pos.getValuef();
        final float fVal = falloff.getValuef();

        model.strips.parallelStream().forEach(new Consumer<Strip>() {
            @Override
            public void accept(final Strip strip) {
                int s = model.strips.indexOf(strip);
                int i = 0;
                for (LXPoint p : strip.points) {
                    colors[p.index] = lx.hsb(
                        palette.getHuef() + 360 - p.x * .2f + p.y * .3f,
                        constrain(.4f * min(abs(s - sVal1), abs(s - sVal2)), 20, 100),
                        max(0, 100 - fVal * abs(i - pVal * (strip.metrics.numPoints - 1)))
                    );
                    ++i;
                }
            }
        });
    }
}
