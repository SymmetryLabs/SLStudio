package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.audio.LXAudioInput;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;

import static heronarts.lx.PolyBuffer.Space.SRGB8;
import static processing.core.PApplet.constrain;

public class SquareBinEQ extends SLPattern<SLModel> {

    private final LXAudioInput audioInput = lx.engine.audio.getInput();
    private final GraphicMeter eq = new GraphicMeter(audioInput);

    private final CompoundParameter gain = new CompoundParameter("Gain", 0.5);
    private final CompoundParameter range = new CompoundParameter("Range", 0.2);
    private final CompoundParameter attack = new CompoundParameter("Attack", 0.4);
    private final CompoundParameter release = new CompoundParameter("Rls", 0.4);
    private final CompoundParameter slope = new CompoundParameter("Slope", 0.5);
    private final CompoundParameter speed = new CompoundParameter("Speed", 0, -2, 2);
    private final CompoundParameter bins = new CompoundParameter("Bins", 16, 2, 64);
    private final CompoundParameter hueShift = new CompoundParameter("HueShift", 0.5);
    private final CompoundParameter zFade = new CompoundParameter("ZFade", 0.05, 0, 1);

    private float yOffset = 0;

    public SquareBinEQ(LX lx) {
        super(lx);
        eq.start();
        addParameter(gain);
        addParameter(range);
        addParameter(attack);
        addParameter(release);
        addParameter(slope);
        addParameter(speed);
        addParameter(bins);
        addParameter(hueShift);
        addParameter(zFade);
        addModulator(eq).start();
    }

    public void onActive() {
        yOffset = 0;
    }

    @Override
    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);

        eq.gain.setNormalized(gain.getValuef());
        eq.range.setNormalized(range.getValuef());
        eq.attack.setNormalized(attack.getValuef());
        eq.release.setNormalized(release.getValuef());
        eq.slope.setNormalized(slope.getValuef());

        yOffset += speed.getValuef() * model.yRange * (float) deltaMs / 1000f;
        yOffset = yOffset % model.yRange;
        if (yOffset < 0) {
            yOffset += model.yRange;
        }

        int numBins = (int) bins.getValue();

        model.forEachPoint((start, end) -> {
            for (LXVector v : getVectors(start, end)) {
                float yPos = v.y - model.yMin + yOffset;
                float yNorm = (yPos % model.yRange) / model.yRange;
                if (yNorm < 0) {
                    yNorm += 1;
                }
                int visualBand = (int) (yNorm * numBins);
                visualBand = Math.max(0, Math.min(visualBand, numBins - 1));

                int eqBand = (int) constrain(
                    visualBand * (eq.numBands - 1) / (float) (numBins - 1),
                    0,
                    eq.numBands - 1
                );
                float value = eq.getBandf(eqBand);

                float zRel = (model.zMax - v.z) / model.zRange;
                float fade = zFade.getValuef();
                float b;
                if (fade < 0.001f) {
                    b = (zRel <= value) ? 100f : 0f;
                } else {
                    b = constrain(100f - 100f * (zRel - value) / fade, 0f, 100f);
                }

                float h = (palette.getHuef() + visualBand * 15f * hueShift.getValuef()) % 360f;
                colors[v.index] = LXColor.hsb(h, 100f, b);
            }
        });
        markModified(SRGB8);
    }
}
