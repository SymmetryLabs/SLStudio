package com.symmetrylabs.slstudio.effect;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.util.MusicUtils;

import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;

import static heronarts.lx.PolyBuffer.Space.RGB16;

public class MidiTrigger extends MidiPolyphonicExpressionEffect {
    public enum Shape {
        ALL,
        XPOS,
        XNEG,
        DISC,
        HOLE
    }

    private CompoundParameter attackParam = new CompoundParameter("Attack", 0.5, 0, 2);
    private CompoundParameter decayParam = new CompoundParameter("Decay", 0.5, 0, 2);
    private CompoundParameter growParam = new CompoundParameter("Grow", 0, -5, 3);
    private CompoundParameter shrinkParam = new CompoundParameter("Shrink", 0, -5, 3);

    private DiscreteParameter pitchLoParam = new DiscreteParameter("PitchLo", MusicUtils.PITCH_C1, 0, 127);
    private DiscreteParameter pitchHiParam = new DiscreteParameter("PitchHi", MusicUtils.PITCH_C5, 0, 127);
    private EnumParameter<Shape> shapeParam = new EnumParameter<>("Shape", Shape.ALL);
    private BooleanParameter holdParam = new BooleanParameter("Hold").setMode(BooleanParameter.Mode.TOGGLE);

    protected float[] amplitudes;
    protected float coverage;
    protected float radius;

    public MidiTrigger(LX lx) {
        super(lx);
        amplitudes = new float[model.points.length];
        radius = (float) Math.hypot(Math.hypot(model.xRange, model.yRange), model.zRange)/2;

        addParameter(attackParam);
        addParameter(decayParam);
        addParameter(growParam);
        addParameter(shrinkParam);
        addParameter(pitchLoParam);
        addParameter(pitchHiParam);
        addParameter(shapeParam);
        addParameter(holdParam);
    }

    public void run(double deltaMs, double enabledAmount, PolyBuffer.Space preferredSpace) {
        double deltaSec = deltaMs / 1000;
        double attackSec = attackParam.getValue();
        double decaySec = decayParam.getValue();
        double growSec = Math.pow(2, growParam.getValue());
        double shrinkSec = Math.pow(2, shrinkParam.getValue());

        if (isSustaining()) {
            coverage += deltaSec / growSec;
        } else {
            coverage -= deltaSec / shrinkSec;
        }
        coverage = Math.max(0, Math.min(1, coverage));

        for (int i = 0; i < model.points.length; i++) {
            if (isInCoverage(model.points[i])) {
                amplitudes[i] += deltaSec / attackSec;
            } else {
                amplitudes[i] -= deltaSec / decaySec;
            }
            amplitudes[i] = Math.max(0, Math.min(1, amplitudes[i]));
        }

        if (enabledAmount > 0) {
            long[] colors = (long[]) getArray(RGB16);
            for (int i = 0; i < colors.length; i++) {
                colors[i] = Ops16.multiply(colors[i], amplitudes[i]);
            }
            markModified(RGB16);
        }
    }

    protected boolean isInCoverage(LXPoint point) {
        float dx = point.x - model.cx;
        float dy = point.y - model.cy;
        float dz = point.z - model.cz;
        float size;

        switch (shapeParam.getEnum()) {
            case ALL:
                return coverage > 0;
            case XPOS:
                return point.x < model.xMin + coverage * model.xRange;
            case XNEG:
                return point.x > model.xMax - coverage * model.xRange;
            case DISC:
                size = coverage * radius;
                return (dx * dx + dy * dy + dz * dz) < (size * size);
            case HOLE:
                size = (1 - coverage) * radius;
                return (dx * dx + dy * dy + dz * dz) > (size * size);
        }
        return false;
    }

    protected boolean isSustaining() {
        if (holdParam.isOn()) return true;
        for (int i = pitchLoParam.getValuei(); i <= pitchHiParam.getValuei(); i++) {
            if (velocities[i] > 0) {
                return true;
            }
        }
        return false;
    }
}
