package com.symmetrylabs.shows.firefly;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.BooleanParameter;
import com.symmetrylabs.slstudio.effect.SLEffect;
import art.lookingup.KaledoscopeModel;
import art.lookingup.LUButterfly;
import art.lookingup.LUFlower;

public class ButterflyFlutterEffect extends SLEffect<KaledoscopeModel> {

    public final CompoundParameter durationParam, pauseParam, baselineParam,
           lengthParam, flutterParam;
    public final BooleanParameter fooParam;

    private float[] phases;

    public ButterflyFlutterEffect(LX lx) {
        super(lx);

        phases = new float[KaledoscopeModel.allButterflies.size()];

        addParameter(baselineParam = new CompoundParameter("Baseline", 0.15, 0, 1));
        addParameter(flutterParam = new CompoundParameter("Flutter", 0.13, 0, 1));
        addParameter(durationParam = new CompoundParameter("Duration", 0.83, 0, 1));
        addParameter(pauseParam = new CompoundParameter("Pause", 1, 0, 5));
        addParameter(fooParam = new BooleanParameter("Foo", true));
        addParameter(lengthParam = new CompoundParameter("Length", 0.25, 0, 1));

        reset();
    }

    private void reset() {
        for (int i = 0; i < phases.length; ++i) {
            phases[i] = (float)Math.random();
        }
    }

    private float easeSlow(float x) {
        return (float)(x >= 1 ? 1 : 1 - Math.pow(2, -10 * x));
    }

    private float easeFast(float x) {
        return x;
    }

    @Override
    public void run(double deltaMs, double amount) {
        float flutter = flutterParam.getValuef();
        float duration = durationParam.getValuef();
        float pause = 2 * pauseParam.getValuef();
        float period = flutter + duration;
        for (int i = 0; i < phases.length; ++i) {
            phases[i] += (float)deltaMs / 1000 / duration;
            if (phases[i] > period) {
                phases[i] = -pause * (float)Math.random();
            }
        }

        float baseline = baselineParam.getValuef();
        for (int i = 0; i < KaledoscopeModel.allButterflies.size(); ++i) {
            boolean isFlutter = true;
            float r = phases[i] / flutter;
            if (phases[i] >= flutter) {
                isFlutter = false;
                r = (phases[i] - flutter) / duration;
            }

            LUButterfly butterfly = KaledoscopeModel.allButterflies.get(i);
            for (int j = 0; j < butterfly.pointsByRow.size(); j += 2) {
                LXPoint pl = butterfly.pointsByRow.get(j);
                LXPoint pr = butterfly.pointsByRow.get(j + 1);
                float s = 0;
                if (r <= 1) {
                    if (fooParam.isOn()) {
                        float d = (isFlutter ? easeSlow(r) : easeSlow(r)) - j / (float)(butterfly.pointsByRow.size() - 2);
                        if (d > 0) {
                            s = 1;
                        }
                    }
                    else {
                        float d = Math.abs((isFlutter ? easeSlow(r) : easeSlow(r)) - j / (float)(butterfly.pointsByRow.size() - 2));
                        if (d < lengthParam.getValuef()) {
                            s = 1 - d  / lengthParam.getValuef();
                        }
                    }
                }
                if (s < baseline) {
                    s = baseline;
                }
                setColor(pl.index, LXColor.scaleBrightness(colors[pl.index], s));
                setColor(pr.index, LXColor.scaleBrightness(colors[pr.index], s));
            }
        }
    }
}
