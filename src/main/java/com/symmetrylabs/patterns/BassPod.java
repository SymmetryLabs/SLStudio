package com.symmetrylabs.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.audio.*;
import heronarts.lx.parameter.*;
import heronarts.lx.modulator.*;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;

import com.symmetrylabs.util.MathUtils;

public class BassPod extends LXPattern {

    private LXAudioInput audioInput = lx.engine.audio.getInput();
    private GraphicMeter eq = new GraphicMeter(audioInput);

    private final CompoundParameter clr = new CompoundParameter("CLR", 0.5);

    public BassPod(LX lx) {
        super(lx);
        addParameter(clr);
        // addParameter(eq.gain);
        // addParameter(eq.range);
        // addParameter(eq.attack);
        // addParameter(eq.release);
        // addParameter(eq.slope);
        addModulator(eq).start();
    }

    @Override
    public void onActive() {
        eq.range.setValue(36);
        eq.release.setValue(300);
        eq.gain.setValue(-6);
        eq.slope.setValue(6);
    }

    @Override
    public void run(double deltaMs) {
        float bassLevel = eq.getAveragef(0, 5);
        float satBase = bassLevel*480*clr.getValuef();

        for (LXPoint p : model.points) {
            int avgIndex = (int)MathUtils.constrain(1 + (float)Math.abs(p.x - model.cx) / (model.cx) * (eq.numBands - 5), 0, eq.numBands - 5);
            float value = 0;
            for (int i = avgIndex; i < avgIndex + 5; ++i) {
                value += eq.getBandf(i);
            }
            value /= 5.;

            float b = (int)MathUtils.constrain(8 * (value * model.yMax - (float)Math.abs(p.y - model.yMax / 2f)), 0, 100);
            colors[p.index] = lx.hsb(
                palette.getHuef() + (float)Math.abs(p.y - model.cy) + (float)Math.abs(p.x - model.cx),
                MathUtils.constrain(satBase - .6f * MathUtils.dist(p.x, p.y, model.cx, model.cy), 0, 100),
                b
            );
        }
    }
}
