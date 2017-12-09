package com.symmetrylabs.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.audio.LXAudioInput;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class BassPod extends LXPattern {

    private LXAudioInput audioInput = lx.engine.audio.getInput();
    private GraphicMeter eq = new GraphicMeter(audioInput);

    private final CompoundParameter clr = new CompoundParameter("CLR", 0.5);

    public BassPod(LX lx) {
        super(lx);
        addParameter(clr);
        addParameter(eq.gain);
        addParameter(eq.range);
        addParameter(eq.attack);
        addParameter(eq.release);
        addParameter(eq.slope);
        addModulator(eq).start();
    }

    void onActive() {
        eq.range.setValue(36);
        eq.release.setValue(300);
        eq.gain.setValue(-6);
        eq.slope.setValue(6);
    }

    public void run(double deltaMs) {
        final float bassLevel = eq.getAveragef(0, 5);
        final float satBase = bassLevel * 480 * clr.getValuef();

        Arrays.asList(model.points).parallelStream().forEach(new Consumer<LXPoint>() {
            @Override
            public void accept(final LXPoint p) {
                int avgIndex = (int) constrain(1 + abs(p.x - model.cx) / (model.cx) * (eq.numBands - 5), 0, eq.numBands - 5);
                float value = 0;
                for (int i = avgIndex; i < avgIndex + 5; ++i) {
                    value += eq.getBandf(i);
                }
                value /= 5.;

                float b = constrain(8 * (value * model.yMax - abs(p.y - model.yMax / 2.)), 0, 100);
                colors[p.index] = lx.hsb(
                    palette.getHuef() + abs(p.y - model.cy) + abs(p.x - model.cx),
                    constrain(satBase - .6 * dist(p.x, p.y, model.cx, model.cy), 0, 100),
                    b
                );
            }
        });
    }
}
