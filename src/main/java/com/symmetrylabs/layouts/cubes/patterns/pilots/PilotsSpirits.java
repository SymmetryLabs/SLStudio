package com.symmetrylabs.layouts.cubes.patterns.pilots;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.transform.LXMatrix;
import heronarts.lx.transform.LXTransform;
import heronarts.lx.transform.LXVector;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;

public class PilotsSpirits extends SLPattern<SLModel> {
    private CompoundParameter spiritSizeParam = new CompoundParameter("size", 30, 1, 200);
    private CompoundParameter spiritHeartParam = new CompoundParameter("heart", 30, 1, 200);
    private DiscreteParameter trailParam = new DiscreteParameter("trail", 35, 0, 500);
    private CompoundParameter trailWidthParam = new CompoundParameter("twidth", 1.8f, 0.1f, 3f);

    private static final float RED_CHASE_DELAY = 400; // ms
    private static final float CHASE_PERIOD = 2700; // ms
    private static final float ROTATION_PERIOD = 4800; // ms
    private static final float RED_STRONGER_TIME = 2200; // ms
    private static final float SHAKING_AMP_TIME = 4000; // ms
    private static final float YELLOW_WINS_TIME = 1500; // ms

    SinLFO redRestLfo = new SinLFO(-40, 30, 4500);
    SinLFO yellowRestLfo = new SinLFO(35, -20, 5100);
    float phaseAge = 0;
    float chaseAge = 0;

    enum Phase {
        IDLE,
        MOVE_IN,
        ROTATING,
        SHAKING,
        CHASE,
        YELLOW_WINS,
        OUT,
    }

    Phase phase = Phase.ROTATING;
    Random random = new Random();
    boolean jerking = false;

    LXVector redBase, yellowBase;
    Deque<LXVector> yTrail = new LinkedList<>();
    Deque<LXVector> rTrail = new LinkedList<>();

    public PilotsSpirits(LX lx) {
        super(lx);

        addParameter(spiritSizeParam);
        addParameter(spiritHeartParam);
        addParameter(trailParam);
        addParameter(trailWidthParam);
        addModulator(redRestLfo);
        addModulator(yellowRestLfo);

        redRestLfo.start();
        yellowRestLfo.start();

        resetLocation();
    }

    private void resetLocation() {
        redBase = new LXVector(model.xMax, model.cy, model.cz);
        rTrail.clear();

        yellowBase = new LXVector(model.xMin, model.cy, model.cz);
        yTrail.clear();
    }

    private LXVector randomVector() {
        /* This doesn't create uniformly distributed vectors on the 2-sphere, but it sure is a lot simpler than the ways that do. */
        LXVector v = new LXVector(
            random.nextFloat() - 0.5f,
            random.nextFloat() - 0.5f,
            random.nextFloat() - 0.5f);
        return v;
    }

    @Override
    public void run(double elapsedMs) {
        phaseAge += elapsedMs;
        chaseAge += elapsedMs;

        int black = LXColor.gray(0);
        for (int i = 0; i < colors.length; i++)
            colors[i] = black;

        if (phase == Phase.OUT)
            return;

        LXVector yTarget = null;
        LXVector rTarget = null;
        float yVel = 0, rVel = 0;
        float rotateRadius = 60f;

        /* Update positions */
        switch (phase) {
            case MOVE_IN: {
                yTarget = new LXVector(model.cx - rotateRadius, model.cy, model.cz);
                rTarget = new LXVector(model.cx + rotateRadius, model.cy, model.cz);
                yVel = 60f / 1000f;
                rVel = 60f / 1000f;
                break;
            }

            case ROTATING: {
                float theta = phaseAge / ROTATION_PERIOD * 2f * (float) Math.PI;
                Rotation rot = new Rotation(new Vector3D(0, 0, 1), theta, RotationConvention.VECTOR_OPERATOR);
                Vector3D apacheOff = rot.applyTo(new Vector3D(rotateRadius, 0, 0));
                LXVector off = new LXVector((float) apacheOff.getX(), (float) apacheOff.getY(), (float) apacheOff.getZ());
                LXVector center = new LXVector(model.cx, model.cy, model.cz);
                redBase = off.copy().add(center);
                yellowBase = off.mult(-1).add(center);
                break;
            }

            case YELLOW_WINS:
            case CHASE: {
                yellowBase.x = model.cx + model.xRange * (float) Math.sin(chaseAge / CHASE_PERIOD * 2 * Math.PI) / 2;
                if (chaseAge > RED_CHASE_DELAY)
                    redBase.x = model.cx + model.xRange * (float) Math.sin((chaseAge - RED_CHASE_DELAY) / CHASE_PERIOD * 2 * Math.PI) / 2;
                break;
            }
        }

        if (yTarget != null) {
            LXVector move = yellowBase.copy().mult(-1).add(yTarget);
            float distToMove = Float.min(move.mag(), (float) elapsedMs * yVel);
            move.setMag(distToMove);
            yellowBase.add(move);
        }
        if (rTarget != null) {
            LXVector move = redBase.copy().mult(-1).add(rTarget);
            float distToMove = Float.min(move.mag(), (float) elapsedMs * rVel);
            move.setMag(distToMove);
            redBase.add(move);
        }

        /* Add idle animations */
        LXVector yLoc = new LXVector(
            -yellowRestLfo.getValuef() / 1.5f,
            yellowRestLfo.getValuef() / 2,
            -yellowRestLfo.getValuef());
        yLoc.add(yellowBase);

        LXVector rLoc = new LXVector(-redRestLfo.getValuef() / 3, redRestLfo.getValuef(), -redRestLfo.getValuef() / 7);
        rLoc.add(redBase);

        yTrail.addFirst(yLoc);
        rTrail.addFirst(rLoc);

        int trailLength = trailParam.getValuei();

        while (yTrail.size() > trailLength) {
            yTrail.removeLast();
        }
        while (rTrail.size() > trailLength) {
            rTrail.removeLast();
        }

        float areaEffect = spiritSizeParam.getValuef();
        float heartSize = spiritHeartParam.getValuef();
        float trailWidth = trailWidthParam.getValuef();

        /* Change colors and sizes */
        float rHue = 0;
        float yHue = 51;
        float rAlpha = 1;
        float yAlpha = 1;

        float rHeartSize = heartSize;
        float yHeartSize = heartSize;

        for (LXVector v : getVectors()) {
            double rDist = Double.MAX_VALUE;
            double yDist = Double.MAX_VALUE;
            int t = 0;
            for (LXVector rv : rTrail) {
                rDist = Double.min(rv.dist(v) + t * trailWidth, rDist);
                t++;
            }
            t = 0;
            for (LXVector yv : yTrail) {
                yDist = Double.min(yv.dist(v) + t * trailWidth, yDist);
                t++;
            }

            double rStrength = rHeartSize / rDist;
            double yStrength = yHeartSize / yDist;

            rDist -= rHeartSize;
            yDist -= yHeartSize;

            int color;
            if (rStrength > yStrength)
                color = rDist < 0
                    ? LXColor.hsba(rHue, 100 * (1 + rDist / rHeartSize), 100, rAlpha)
                    : LXColor.hsba(rHue, 100, 100 * Math.max(0, 1 - rDist / areaEffect), rAlpha);
            else
                color = yDist < 0
                    ? LXColor.hsba(yHue, 100 * (1 + yDist / yHeartSize), 85, yAlpha)
                    : LXColor.hsba(yHue, 100, 85 * Math.max(0, 1 - yDist / areaEffect), yAlpha);
            colors[v.index] = Ops8.add(color, colors[v.index]);
        }
    }

    @Override
    public void noteOnReceived(MidiNoteOn note) {
        if (note.getPitch() == 60) {
            switch (phase) {
                case IDLE:
                    phase = Phase.MOVE_IN;
                    break;
                case MOVE_IN:
                    phase = Phase.ROTATING;
                    break;
                case ROTATING:
                    phase = Phase.SHAKING;
                    break;
                case SHAKING:
                    phase = Phase.CHASE;
                    chaseAge = 0;
                    break;
                case CHASE:
                    phase = Phase.YELLOW_WINS;
                    break;
                case YELLOW_WINS:
                    phase = Phase.OUT;
                    break;
                case OUT:
                    resetLocation();
                    phase = Phase.IDLE;
                    break;
            }
            phaseAge = 0;
            System.out.println(phase);
        }
        if (note.getPitch() == 62)
            jerking = true;
    }

    @Override
    public void noteOffReceived(MidiNote note) {
        if (note.getPitch() == 62)
            jerking = false;
    }
}
