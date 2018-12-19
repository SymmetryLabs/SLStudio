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
import heronarts.lx.parameter.LXParameter;

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
    private CompoundParameter inParam = new CompoundParameter("In", 0, -5, 3);
    private CompoundParameter outParam = new CompoundParameter("Out", 0, -5, 3);

    private CompoundParameter brtParam = new CompoundParameter("Brt", 1, 0, 1);
    private DiscreteParameter pitchLoParam = new DiscreteParameter("PitchLo", MusicUtils.PITCH_C1, 0, 127);
    private DiscreteParameter pitchHiParam = new DiscreteParameter("PitchHi", MusicUtils.PITCH_C5, 0, 127);
    private EnumParameter<Shape> shapeParam = new EnumParameter<>("Shape", Shape.ALL);
    private BooleanParameter holdParam = new BooleanParameter("Hold").setMode(BooleanParameter.Mode.TOGGLE);
    private BooleanParameter followParam = new BooleanParameter("Follow").setMode(BooleanParameter.Mode.TOGGLE);

    private BooleanParameter xPosParam = new BooleanParameter("XPOS").setMode(BooleanParameter.Mode.MOMENTARY);
    private BooleanParameter xNegParam = new BooleanParameter("XNEG").setMode(BooleanParameter.Mode.MOMENTARY);
    private BooleanParameter discParam = new BooleanParameter("DISC").setMode(BooleanParameter.Mode.MOMENTARY);
    private BooleanParameter holeParam = new BooleanParameter("HOLE").setMode(BooleanParameter.Mode.MOMENTARY);

    protected float[] amplitudes;
    protected float leadProgress;
    protected float trailProgress;
    protected float radius;
    protected boolean needReset;

    public MidiTrigger(LX lx) {
        super(lx);
        pitchLoParam.setFormatter(MusicUtils.MIDI_PITCH_FORMATTER);
        pitchHiParam.setFormatter(MusicUtils.MIDI_PITCH_FORMATTER);

        addParameter(attackParam);
        addParameter(decayParam);
        addParameter(inParam);
        addParameter(outParam);

        addParameter(brtParam);
        addParameter(pitchLoParam);
        addParameter(pitchHiParam);
        addParameter(shapeParam);

        addParameter(holdParam);
        addParameter(followParam);

        addParameter(xPosParam);
        addParameter(xNegParam);
        addParameter(discParam);
        addParameter(holeParam);

        amplitudes = new float[model.points.length];
        radius = (float) Math.hypot(Math.hypot(model.xRange, model.yRange), model.zRange)/2;
    }

    @Override public void onParameterChanged(LXParameter param) {
        if (param instanceof BooleanParameter && ((BooleanParameter) param).isOn()) {
            if (param == xPosParam) shapeParam.setValue(Shape.XPOS);
            if (param == xNegParam) shapeParam.setValue(Shape.XNEG);
            if (param == discParam) shapeParam.setValue(Shape.DISC);
            if (param == holeParam) shapeParam.setValue(Shape.HOLE);
        }
    }

    public void run(double deltaMs, double enabledAmount, PolyBuffer.Space preferredSpace) {
        double deltaSec = deltaMs / 1000;
        double attackSec = attackParam.getValue();
        double decaySec = decayParam.getValue();
        double growSec = Math.pow(2, inParam.getValue());
        double shrinkSec = Math.pow(2, outParam.getValue());
        boolean follow = followParam.isOn();

        System.out.println(String.format("lead %.3f  trail %.3f", leadProgress, trailProgress));
        if (isSustaining()) {
            if (follow) {
                if (needReset) {
                    leadProgress = 0;
                    needReset = false;
                }
            } else {
                trailProgress = 0;
            }
            leadProgress += deltaSec / growSec;
        } else {
            if (follow) {
                leadProgress += deltaSec / growSec;
                trailProgress += deltaSec / shrinkSec;
                needReset = true;
            } else {
                leadProgress -= deltaSec / shrinkSec;
            }
        }
        leadProgress = Math.max(0, Math.min(1, leadProgress));
        trailProgress = Math.max(0, Math.min(leadProgress, trailProgress));

        for (int i = 0; i < model.points.length; i++) {
            if (isInCoverage(model.points[i])) {
                amplitudes[i] += deltaSec / attackSec;
            } else {
                amplitudes[i] -= deltaSec / decaySec;
            }
            amplitudes[i] = Math.max(0, Math.min(1, amplitudes[i]));
        }

        if (enabledAmount > 0) {
            double brt = brtParam.getValue();
            long[] colors = (long[]) getArray(RGB16);
            for (int i = 0; i < colors.length; i++) {
                colors[i] = Ops16.multiply(colors[i], amplitudes[i] * brt);
            }
            markModified(RGB16);
        }
    }

    protected boolean isInCoverage(LXPoint point) {
        float dx = point.x - model.cx;
        float dy = point.y - model.cy;
        float dz = point.z - model.cz;
        float dFrac = (dx * dx + dy * dy + dz * dz) / (radius * radius);
        float xFrac = (point.x - model.xMin) / model.xRange;

        switch (shapeParam.getEnum()) {
            case ALL:
                return leadProgress > trailProgress;
            case XPOS:
                return leadProgress > xFrac && xFrac > trailProgress;
            case XNEG:
                return leadProgress > (1 - xFrac) && (1 - xFrac) > trailProgress;
            case DISC:
                return leadProgress > dFrac && dFrac > trailProgress;
            case HOLE:
                return leadProgress > (1 - dFrac) && (1 - dFrac) > trailProgress;
        }
        return false;
    }

    protected boolean isSustaining() {
        for (BooleanParameter param : new BooleanParameter[] {holdParam, xPosParam, xNegParam, discParam, holeParam}) {
            if (param.isOn()) return true;
        }
        for (int i = pitchLoParam.getValuei(); i <= pitchHiParam.getValuei(); i++) {
            if (velocities[i] > 0) {
                return true;
            }
        }
        return false;
    }

    @Override public void noteOn(int pitch, double velocity) {
        int pitchLo = pitchLoParam.getValuei();
        int pitchHi = pitchHiParam.getValuei();
        if (pitch >= pitchLo && pitch <= pitchHi) {
            int ordinal = pitch - (pitchLo + 1);
            if (ordinal >= 0 && ordinal < Shape.values().length) {
                shapeParam.setValue(ordinal);
            }
        }
    }
}
