package com.symmetrylabs.patterns;

import heronarts.lx.LX;
import heronarts.lx.audio.*;
import heronarts.lx.parameter.*;
import heronarts.lx.modulator.*;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;

import com.symmetrylabs.util.MathUtils;

public class Traktor extends CubesPattern {

    private LXAudioInput audioInput = lx.engine.audio.getInput();
    private GraphicMeter eq = new GraphicMeter(audioInput);

    final int FRAME_WIDTH = 120;

    final CompoundParameter speed = new CompoundParameter("SPD", 0.5);
    final CompoundParameter hueSpread = new CompoundParameter("hueSpread", .4, 0, 1);
    final CompoundParameter trebleGain= new CompoundParameter("trebG", 1, 0, 10);
    final CompoundParameter bassGain = new CompoundParameter("bassG", 1, 0, 10);
    private float[] bass = new float[FRAME_WIDTH];
    private float[] treble = new float[FRAME_WIDTH];

    private int index = 0;

    public Traktor(LX lx) {
        super(lx);
        for (int i = 0; i < FRAME_WIDTH; ++i) {
            bass[i] = 0;
            treble[i] = 0;
        }
        addParameter(speed);
        addParameter(hueSpread);
        addParameter(bassGain);
        addParameter(trebleGain);
        addModulator(eq).start();
    }

    public void onActive() {
        eq.slope.setValue(6);
        eq.gain.setValue(12);
        eq.range.setValue(36);
        eq.release.setValue(200);
        // addParameter(eq.gain);
        // addParameter(eq.range);
        // addParameter(eq.attack);
        // addParameter(eq.release);
        // addParameter(eq.slope);
    }

    int counter = 0;

    public void run(double deltaMs) {

        int stepThresh = (int) (40 - 39*speed.getValuef());
        counter += deltaMs;
        if (counter < stepThresh) {
            return;
        }
        counter = counter % stepThresh;

        index = (index + 1) % FRAME_WIDTH;

        float rawBass = eq.getAveragef(0, 4);
        float rawTreble = eq.getAveragef(eq.numBands-7, 7);

        bass[index] = rawBass * rawBass * rawBass * rawBass;
        treble[index] = rawTreble * rawTreble;
        float hueV = hueSpread.getValuef();
        float bassG = bassGain.getValuef();
        float trebG = trebleGain.getValuef();
        for (LXPoint p : model.points) {
            int i = (int)MathUtils.constrain((model.xMax - p.x) / model.xMax * FRAME_WIDTH, 0, FRAME_WIDTH-1);
            int pos = (index + FRAME_WIDTH - i) % FRAME_WIDTH;

            colors[p.index] = lx.hsb(
                360 + palette.getHuef() + .8f * hueV * (float)Math.abs(p.x-model.cx),
                100,
                MathUtils.constrain(9 * bassG * (bass[pos] * model.cy - (float)Math.abs(p.y - model.cy + 5)), 0, 100)
            );
            blendColor(p.index, lx.hsb(
                400 + palette.getHuef() + .5f * hueV * (float)Math.abs(p.x-model.cx),
                60,
                MathUtils.constrain(7 * trebG * (treble[pos] * .6f * model.cy - (float)Math.abs(p.y - model.cy)), 0, 100)
            ), LXColor.Blend.ADD);
        }
    }
}
