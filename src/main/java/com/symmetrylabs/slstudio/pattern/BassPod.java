package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.pattern.base.SLPattern;

import heronarts.lx.LX;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.audio.LXAudioInput;
import heronarts.lx.parameter.CompoundParameter;

import static processing.core.PApplet.*;

public class BassPod extends SLPattern {

    private LXAudioInput audioInput = lx.engine.audio.getInput();
    private GraphicMeter eq = new GraphicMeter(audioInput);

    private final CompoundParameter clr = new CompoundParameter("CLR", 0.5);

      private final CompoundParameter gain = new CompoundParameter("GAIN", 0.5);
        private final CompoundParameter range = new CompoundParameter("RANG", 0.2);
        private final CompoundParameter attack = new CompoundParameter("ATTK", 0.4);
        private final CompoundParameter release = new CompoundParameter("RLS", 0.4);
        private final CompoundParameter slope = new CompoundParameter("SLOP", 0.5);

    public BassPod(LX lx) {
        super(lx);

        eq.start();

        addParameter(clr);
        addParameter(gain);
        addParameter(range);
        addParameter(attack);
        addParameter(release);
        addParameter(slope);
        addModulator(eq).start();
    }

    // @Override
    // public void onActive() {
    //     super.onActive();
    // }

    public static final float HUE_RATE = 100;
    public static final float BRTNESS_RATE = 800;
    public static final float DESAT_RATE = 50;

    @Override
    public void run(double deltaMs) {
        eq.gain.setNormalized(gain.getValuef());
        eq.range.setNormalized(range.getValuef());
        eq.attack.setNormalized(attack.getValuef());
        eq.release.setNormalized(release.getValuef());
        eq.slope.setNormalized(release.getValuef());

        final float bassLevel = eq.getAveragef(0, 5);
        final float satBase = bassLevel * 480 * clr.getValuef();

        model.getPoints().parallelStream().forEach(p -> {
            int avgIndex = (int)constrain(1 + abs(p.x - model.cx) / model.xRange * (eq.numBands - 5), 0, eq.numBands - 5);
            float value = 0;
            for (int i = avgIndex; i < avgIndex + 5; ++i) {
                value += eq.getBandf(i) * gain.getValuef();
            }
            value /= 5.;

            float h = palette.getHuef() + HUE_RATE * (abs(p.y - model.cy) / model.yRange + abs(p.x - model.cx) / model.xRange);
            float b = constrain(BRTNESS_RATE * (value * model.yRange - abs(p.y - model.cy)) / model.yRange, 0, 100);
            float s = constrain(satBase - DESAT_RATE * dist(p.x, p.y, model.cx, model.cy) * 2f / (model.xRange + model.yRange), 0, 100);

            colors[p.index] = lx.hsb(h, s, b);
        });
    }
}
