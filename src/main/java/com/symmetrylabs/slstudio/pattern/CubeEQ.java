package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.audio.LXAudioInput;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;

import static heronarts.lx.PolyBuffer.Space.SRGB8;
import static processing.core.PApplet.*;


public class CubeEQ extends SLPattern<SLModel> {

    private LXAudioInput audioInput = lx.engine.audio.getInput();
    private GraphicMeter eq = new GraphicMeter(audioInput);

    private final CompoundParameter attack = new CompoundParameter("Attack", 0.4);
    private final CompoundParameter blockiness = new CompoundParameter("Block", 0.5);
    private final CompoundParameter clr = new CompoundParameter("Clr", 0.1, 0, .5);

    private final CompoundParameter edge = new CompoundParameter("Edge", 0.5);
        private final CompoundParameter gain = new CompoundParameter("Gain", 0.5);
    private final CompoundParameter range = new CompoundParameter("Range", 0.2);

    private final CompoundParameter release = new CompoundParameter("Rls", 0.4);
    private final CompoundParameter slope = new CompoundParameter("Slope", 0.5);

    public CubeEQ(LX lx) {
        super(lx);
        eq.start();
        addParameter(gain);
        addParameter(range);
        addParameter(attack);
        addParameter(release);
        addParameter(slope);

        addParameter(edge);
        addParameter(clr);
        addParameter(blockiness);
        addModulator(eq).start();
    }

    public void onActive() {
        // eq.range.setValue(48);
        // eq.release.setValue(300);
    }

    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);

        eq.gain.setNormalized(gain.getValuef());
        eq.range.setNormalized(range.getValuef());
        eq.attack.setNormalized(attack.getValuef());
        eq.release.setNormalized(release.getValuef());
        eq.slope.setNormalized(release.getValuef());

        final float edgeConst = 2 + 30 * edge.getValuef();
        final float clrConst = 1.1f + clr.getValuef();

        model.forEachPoint((start, end) -> {
            for (LXVector v : getVectorList(start, end)) {
                float normalizedX = (v.x - model.xMin) / model.xRange;
                float avgIndex = constrain(2 + normalizedX * (eq.numBands - 4), 0, eq.numBands - 4);
                int avgFloor = (int) avgIndex;

                float leftVal = eq.getBandf(avgFloor);
                float rightVal = eq.getBandf(avgFloor + 1);
                float smoothValue = lerp(leftVal, rightVal, avgIndex - avgFloor);

                float chunkyValue = (
                    eq.getBandf(avgFloor / 4 * 4) +
                        eq.getBandf(avgFloor / 4 * 4 + 1) +
                        eq.getBandf(avgFloor / 4 * 4 + 2) +
                        eq.getBandf(avgFloor / 4 * 4 + 3)
                ) / 4f;

                float value = lerp(smoothValue, chunkyValue, blockiness.getValuef());

                float h = 480 + palette.getHuef() - min(clrConst * v.y, 120);
                float b = constrain(edgeConst * (value * model.yMax - v.y), 0, 100);
                colors[v.index] = LXColor.hsb(h, 100, b);
            }
        });
        markModified(SRGB8);
    }
}
