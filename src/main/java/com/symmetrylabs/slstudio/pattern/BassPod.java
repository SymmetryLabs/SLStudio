package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.audio.LXAudioInput;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

import static processing.core.PApplet.*;

public class BassPod extends SLPattern {

    private LXAudioInput audioInput = lx.engine.audio.getInput();
    private GraphicMeter eq = new GraphicMeter(audioInput);

    private final CompoundParameter clr = new CompoundParameter("CLR", 0.5);

    public BassPod(LX lx) {
        super(lx);
        eq.start();
        addParameter(clr);
//        addParameter(eq.gain);   //to-do can't add these, causes null pointer exception
//        addParameter(eq.range);
//        addParameter(eq.attack);
//        addParameter(eq.release);
//        addParameter(eq.slope);
        addModulator(eq).start();
    }

    public void onActive() {
        eq.range.setValue(36);
        eq.release.setValue(300);
        eq.gain.setValue(-6);
        eq.slope.setValue(6);
    }

    public void run(double deltaMs) {
        final float bassLevel = eq.getAveragef(0, 5);
        final float satBase = bassLevel * 480 * clr.getValuef();

        model.forEachPoint((start, end) -> {
            for (int pi=start; pi<end; pi++) {
                LXPoint p = model.points[pi];

                int avgIndex = (int) constrain(1 + abs(p.x - model.cx) / (model.cx) * (eq.numBands - 5), 0, eq.numBands - 5);
                float value = 0;
                for (int i = avgIndex; i < avgIndex + 5; ++i) {
                    value += eq.getBandf(i);
                }
                value /= 5.;

                float b = constrain(8 * (value * model.yMax - abs(p.y - model.yMax / 2f)), 0, 100);
                colors[p.index] = lx.hsb(
                    palette.getHuef() + abs(p.y - model.cy) + abs(p.x - model.cx),
                    constrain(satBase - .6f * dist(p.x, p.y, model.cx, model.cy), 0, 100),
                    b
                );
            }
        });
    }
}
