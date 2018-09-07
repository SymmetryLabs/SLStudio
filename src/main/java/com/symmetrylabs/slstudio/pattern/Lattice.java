package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Spaces;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.StripsTopology.Dir;
import com.symmetrylabs.slstudio.model.StripsTopology.Sign;
import com.symmetrylabs.slstudio.pattern.base.MidiPolyphonicExpressionPattern;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.PolyBuffer.Space;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

/**
 * MIDI notes trigger activators, activators trigger strip animations.
 * Activators, their hues, the animations, and the strip directions are selectable.
 */
public class Lattice extends MidiPolyphonicExpressionPattern<StripsModel<? extends Strip>> {
    public enum AnimationId {
        TRAIN
    };

    public enum ShapeId {
        SWEEP,
        TRICKLE
    }

    public enum DirSign {
        X_POS,
        X_NEG,
        Y_POS,
        Y_NEG,
        Z_POS,
        Z_NEG
    }

    protected CompoundParameter hueParam = new CompoundParameter("Hue", 0, 0, 2);
    protected EnumParameter<ShapeId> shapeParam = new EnumParameter<>("Shape", ShapeId.SWEEP);
    protected CompoundParameter shpDurParam = new CompoundParameter("ShpDur", 0.5, 0, 2);
    protected EnumParameter<AnimationId> animParam = new EnumParameter<>("Anim", AnimationId.TRAIN);
    protected EnumParameter<DirSign> dirParam = new EnumParameter<>("Dir", DirSign.X_POS);
    protected BooleanParameter triggerParam = new BooleanParameter("Trigger", false).setDescription("Trigger a shape").setMode(BooleanParameter.Mode.MOMENTARY);

    private DiscreteParameter noteLoParam = new DiscreteParameter("NoteLo", 36, 0, 127).setDescription("Lowest MIDI note of keyboard range");
    private DiscreteParameter noteHiParam = new DiscreteParameter("NoteHi", 72, 0, 127).setDescription("Highest MIDI note of keyboard range");

    List<ScheduledActivation> activations = new ArrayList<>();
    List<AnimationRun> activeRuns = new ArrayList<>();
    double timeSec = 0;

    public Lattice(LX lx) {
        super(lx);

        addParameter(hueParam);
        addParameter(shapeParam);
        addParameter(shpDurParam);
        addParameter(animParam);
        addParameter(dirParam);
        addParameter(triggerParam);

        addParameter(noteLoParam);
        addParameter(noteHiParam);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p instanceof BooleanParameter) {
            BooleanParameter param = (BooleanParameter) p;
            if (param == triggerParam) {
                if (param.getValueb()) {
                    trigger(createShape(new LXVector(model.cx, model.cy, model.cz)));
                }
            }
        }
    }

    @Override
    public void noteOn(int pitch, double velocity) {
        int lo = noteLoParam.getValuei();
        int hi = noteHiParam.getValuei();
        if (pitch >= lo && pitch < hi) {
            float x = model.xMin + model.xRange * (pitch - lo) / (hi - lo);
            float y = model.yMin + model.yRange * 0.8f;
            LXVector origin = new LXVector(x, y, model.cz);
            trigger(createShape(origin));
        }
    }

    @Override
    protected void run(double deltaMs, Space preferredSpace) {
        double deltaSec = deltaMs / 1000;
        timeSec += deltaSec;

        List<ScheduledActivation> remainingActivations = new ArrayList<>();
        for (ScheduledActivation act : activations) {
            if (act.startSec <= timeSec) {
                activeRuns.add(act.startAnimation());
            } else {
                remainingActivations.add(act);
            }
        }
        activations = remainingActivations;

        List<AnimationRun> continuingRuns = new ArrayList<>();
        long[] colors = (long[]) getArray(Space.RGB16);
        Arrays.fill(colors, 0);
        for (AnimationRun run : activeRuns) {
            run.advance(deltaSec);
            run.blendOnto(colors);
            if (!run.isExpired()) continuingRuns.add(run);
        }
        activeRuns = continuingRuns;

        markModified(Space.RGB16);
    }

    protected void trigger(Shape shape) {
        double hue = hueParam.getValue();
        Animation animation = createAnimation();
        DirSign dirSign = dirParam.getEnum();
        Dir dir = getDir(dirSign);
        Sign sign = getSign(dirSign);

        List<ScheduledActivation> newActivations = new ArrayList<>();
        for (Strip strip : model.getStrips()) {
            if (getStripAxis(strip) == dir) {
                double delay = shape.getDelay(strip);
                if (delay >= 0) {
                    newActivations.add(new ScheduledActivation(timeSec + delay, strip, sign, animation, hue));

                }
            }
        }
        Collections.sort(newActivations);
        activations.addAll(newActivations);
    }

    public Shape createShape(LXVector origin) {
        double duration = shpDurParam.getValue();

        switch (shapeParam.getEnum()) {
            case SWEEP:
                return new SweepShape(origin, duration);
            case TRICKLE:
                return new TrickleShape(origin, duration);
        }
        return null;
    }

    public Animation createAnimation() {
        switch (animParam.getEnum()) {
            case TRAIN:
                return new TrainAnimation(0.5);
        }
        return null;
    }

    protected class ScheduledActivation implements Comparable<ScheduledActivation> {
        public final double startSec;
        public final Strip strip;
        public final Sign sign;
        public final Animation animation;
        public final double hue;

        public ScheduledActivation(double startSec, Strip strip, Sign sign, Animation animation, double hue) {
            this.startSec = startSec;
            this.strip = strip;
            this.sign = sign;
            this.animation = animation;
            this.hue = hue;
        }

        @Override
        public int compareTo(ScheduledActivation other) {
            if (startSec < other.startSec) return -1;
            if (startSec > other.startSec) return 1;
            return 0;
        }

        public AnimationRun startAnimation() {
            return new AnimationRun(strip, sign, animation, hue);
        }
    }

    protected class AnimationRun {
        Strip strip;
        Sign sign;
        Dir dir;
        Animation animation;
        double hue;
        double elapsedSec;

        public AnimationRun(Strip strip, Sign sign, Animation animation, double hue) {
            this.strip = strip;
            this.sign = sign;
            this.dir = getStripAxis(strip);
            this.animation = animation;
            this.hue = hue;
            elapsedSec = 0;
        }

        public void advance(double deltaSec) {
            elapsedSec += deltaSec;
        }

        public boolean isExpired() {
            return animation.isExpired(elapsedSec);
        }

        public void blendOnto(long[] colors) {
            for (LXPoint point : strip.points) {
                double pos = getPos(point, strip, dir, sign);
                long c = animation.getColor(elapsedSec, pos, hue);
                colors[point.index] = Ops16.add(colors[point.index], c);
            }
        }
    }

    /**
     * A Shape computes the activation sequence of the strips; it decides
     * when the animation starts on each strip, and in which direction.
     */
    interface Shape {
        double getDelay(Strip strip);  // start delay, in seconds; negative means "don't use"
    }

    class SweepShape implements Shape {
        LXVector origin;
        double duration;

        public SweepShape(LXVector origin, double duration) {
            this.origin = origin;
            this.duration = duration;
        }

        public double getDelay(Strip strip) {
            return duration * (
                  4 * Math.abs(strip.cx - origin.x) / model.xRange +
                    Math.abs(strip.cy - origin.y) / model.yRange
            ) / 5;
        }
    }

    class TrickleShape implements Shape {
        LXVector origin;
        double duration;

        public TrickleShape(LXVector origin, double duration) {
            this.origin = origin;
            this.duration = duration;
        }

        public double getDelay(Strip strip) {
            if (Math.abs(strip.cx - origin.x) > model.xRange / 48f) return -1;
            return duration * Math.abs(strip.cy - model.yMax) / model.yRange;
        }
    }

    /**
     * An Animation is a strip animation; it decides what colours to paint
     * at each position (0 to 1) along the strip at a given time (in seconds).
     */
    interface Animation {
        long getColor(double t, double pos, double hue);
        boolean isExpired(double t);
    }

    class TrainAnimation implements Animation {
        double duration;

        public TrainAnimation(double duration) {
            this.duration = duration;
        }

        public long getColor(double t, double pos, double hue) {
            long c = Spaces.rgb8ToRgb16(LXColor.hsb(hue * 360, 100, 100));
            double tf = t / duration;
            if (tf < 0.5) {
                return pos < tf * 2 ? c : 0;
            } else {
                return pos > (tf - 0.5) * 2 ? c : 0;
            }
        }

        public boolean isExpired(double t) {
            return t > duration;
        }
    }

    public static Dir getStripAxis(Strip strip) {
        if (strip.xRange > strip.yRange && strip.xRange > strip.zRange) return Dir.X;
        if (strip.yRange > strip.zRange) return Dir.Y;
        return Dir.Z;
    }

    public static Dir getDir(DirSign dirSign) {
        switch (dirSign) {
            case X_NEG:
            case X_POS:
                return Dir.X;
            case Y_NEG:
            case Y_POS:
                return Dir.Y;
            case Z_NEG:
            case Z_POS:
                return Dir.Z;
        }
        return null;
    }

    public static Sign getSign(DirSign dirSign) {
        switch (dirSign) {
            case X_NEG:
            case Y_NEG:
            case Z_NEG:
                return Sign.NEG;
            case X_POS:
            case Y_POS:
            case Z_POS:
                return Sign.POS;
        }
        return null;
    }

    public static double getPos(LXPoint point, Strip strip, Dir dir, Sign sign) {
        double pos;
        if (dir == Dir.X) {
            pos = (point.x - strip.xMin)/strip.xRange;
        } else if (dir == Dir.Y) {
            pos = (point.y - strip.yMin)/strip.yRange;
        } else {
            pos = (point.z - strip.zMin)/strip.zRange;
        }
        return (sign == sign.NEG) ? 1 - pos : pos;
    }
}
