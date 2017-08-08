package com.symmetrylabs.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.audio.*;
import heronarts.lx.parameter.*;
import heronarts.lx.modulator.*;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;

import com.symmetrylabs.util.MathUtils;

public class CubeEQ extends LXPattern {

    private LXAudioInput audioInput = lx.engine.audio.getInput();
    private GraphicMeter eq = new GraphicMeter(audioInput);

    private final CompoundParameter edge = new CompoundParameter("EDGE", 0.5);
    private final CompoundParameter clr = new CompoundParameter("CLR", 0.1, 0, .5);
    private final CompoundParameter blockiness = new CompoundParameter("BLK", 0.5);

    public CubeEQ(LX lx) {
        super(lx);
        // addParameter(eq.range);
        // addParameter(eq.attack);
        // addParameter(eq.release);
        // addParameter(eq.slope);
        addParameter(edge);
        addParameter(clr);
        addParameter(blockiness);
        addModulator(eq).start();
    }

    @Override
    public void onActive() {
        eq.range.setValue(48);
        eq.release.setValue(300);
    }

    @Override
    public void run(double deltaMs) {
        float edgeConst = 2 + 30 * edge.getValuef();
        float clrConst = 1.1f + clr.getValuef();

        for (LXPoint p : model.points) {
            float avgIndex = MathUtils.constrain(2 + p.x / model.xMax * (eq.numBands-4), 0, eq.numBands-4);
            int avgFloor = (int)avgIndex;

            float leftVal = eq.getBandf(avgFloor);
            float rightVal = eq.getBandf(avgFloor+1);
            float smoothValue = MathUtils.lerp(leftVal, rightVal, avgIndex-avgFloor);

            float chunkyValue = (
                eq.getBandf(avgFloor/4*4) +
                eq.getBandf(avgFloor/4*4 + 1) +
                eq.getBandf(avgFloor/4*4 + 2) +
                eq.getBandf(avgFloor/4*4 + 3)
            ) / 4f;

            float value = MathUtils.lerp(smoothValue, chunkyValue, blockiness.getValuef());

            float b = MathUtils.constrain(edgeConst * (value*model.yMax - p.y), 0, 100);
            colors[p.index] = lx.hsb(
                480 + palette.getHuef() - Math.min(clrConst*p.y, 120),
                100,
                b
            );
        }
    }
}
