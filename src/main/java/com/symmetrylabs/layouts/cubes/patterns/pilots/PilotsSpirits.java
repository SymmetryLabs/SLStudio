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
import heronarts.lx.transform.LXVector;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;

public class PilotsSpirits extends SLPattern<SLModel> {
    private CompoundParameter spiritSizeParam = new CompoundParameter("size", 30, 1, 200);
    private CompoundParameter spiritHeartParam = new CompoundParameter("heart", 30, 1, 200);

    private static final float RED_CHASE_DELAY = 400; // ms
    private static final float CHASE_PERIOD = 2700; // ms
    private static final float ROTATION_PERIOD_BASE = 4800; // ms
    private static final float ROTATION_PERIOD_AMPLITUDE = 600; // ms
    private static final float ROTATION_PERIOD_RATE = 10200; // ms
    private static final int TRAIL_LENGTH = 5;
    private static final float TRAIL_WIDTH = 7.f;

    private final SinLFO redRestLfo = new SinLFO(-40, 30, 4500);
    private final SinLFO yellowRestLfo = new SinLFO(35, -20, 5100);

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
    float theta = 0;
    float phaseAge = 0;
    float chaseAge = 0;

    LXVector redBase, yellowBase;
    LXVector chaseStart, chaseTarget;
    Deque<LXVector> yTrail = new LinkedList<>();
    Deque<LXVector> rTrail = new LinkedList<>();

    public PilotsSpirits(LX lx) {
        super(lx);

        addParameter(spiritSizeParam);
        addParameter(spiritHeartParam);
        addModulator(redRestLfo).start();
        addModulator(yellowRestLfo).start();

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

    private void updateLocations(double elapsedMs) {
        LXVector yTarget = null;
        LXVector rTarget = null;
        float yVel = 0, rVel = 0;
        float ellipseMajor = 95f;
        float ellipseMinor = 20f;

        /* Update positions */
        switch (phase) {
            case MOVE_IN: {
                yTarget = new LXVector(model.cx - ellipseMajor, model.cy, model.cz);
                rTarget = new LXVector(model.cx + ellipseMajor, model.cy, model.cz);
                yVel = 60f / 1000f;
                rVel = 60f / 1000f;
                break;
            }

            case ROTATING:
            case SHAKING: {
                float rate = ROTATION_PERIOD_BASE + (float) Math.sin(2 * Math.PI * phaseAge / ROTATION_PERIOD_RATE) * ROTATION_PERIOD_AMPLITUDE;
                if (phase == Phase.SHAKING)
                    rate -= Math.min(1200f, phaseAge / 2.5f);
                theta += 2 * Math.PI * elapsedMs / rate;

                float rad = ellipseMinor + (float) ((ellipseMajor - ellipseMinor) / 2f * (Math.cos(2 * theta) + 1f));
                float yRad = rad;
                float rRad = rad;
                if (phase == Phase.SHAKING)
                    yRad = yRad * Math.max(0.1f, 1f - phaseAge / 4000f);

                Rotation rot = new Rotation(new Vector3D(0, 0, 1), theta, RotationConvention.VECTOR_OPERATOR);
                Vector3D apacheOff = rot.applyTo(new Vector3D(1, 0, 0));
                LXVector off = new LXVector((float) apacheOff.getX(), (float) apacheOff.getY(), (float) apacheOff.getZ());
                LXVector center = new LXVector(model.cx, model.cy, model.cz);
                redBase = off.copy().mult(rRad).add(center);
                yellowBase = off.mult(-yRad).add(center);
                break;
            }

            case CHASE: {
                if (chaseTarget == null)
                    pickChaseTarget();

                LXVector chaseVec = chaseStart.copy().mult(-1).add(chaseTarget);
                float t = 2f * chaseAge / 2000f;
                if (t < 1) {
                    t = 0.5f * (float) Math.pow(2, 10 * (t - 1));
                } else {
                    t = 0.5f * (float) (-Math.pow(2, -10 * (t - 1)) + 2);
                }
                chaseVec.mult(t);
                yellowBase = chaseStart.copy().add(chaseVec);
                if (t > 0.99)
                    pickChaseTarget();
                break;
            }
        }

        boolean yAtTarget = false;
        boolean rAtTarget = false;

        if (yTarget != null) {
            LXVector move = yellowBase.copy().mult(-1).add(yTarget);
            float distToTarget = move.mag();
            float distToMove = Float.min(distToTarget, (float) elapsedMs * yVel);
            move.setMag(distToMove);
            yellowBase.add(move);
            yAtTarget = distToTarget < 1;
        }
        if (rTarget != null) {
            LXVector move = redBase.copy().mult(-1).add(rTarget);
            float distToTarget = move.mag();
            float distToMove = Float.min(distToTarget, (float) elapsedMs * rVel);
            move.setMag(distToMove);
            redBase.add(move);
            rAtTarget = distToTarget < 1;
        }

        if (phase == Phase.MOVE_IN && yAtTarget && rAtTarget) {
            nextPhase();
        }
    }

    private void pickChaseTarget() {
        chaseStart = yellowBase;
        LXVector[] vs = model.getVectorArray();
        do {
            int i = random.nextInt(vs.length);
            chaseTarget = vs[i];
        } while (chaseTarget.dist(chaseStart) < 0.2f * model.xRange);
        chaseAge = 0;
    }

    private void updateTrails(LXVector yLoc, LXVector rLoc) {
        if (yTrail.isEmpty() || yTrail.peekFirst().dist(yLoc) > 8)
            yTrail.addFirst(yLoc);
        if (rTrail.isEmpty() || rTrail.peekFirst().dist(rLoc) > 8)
            rTrail.addFirst(rLoc);
        while (yTrail.size() > TRAIL_LENGTH)
            yTrail.removeLast();
        while (rTrail.size() > (phase == Phase.SHAKING ? 3 * TRAIL_LENGTH : TRAIL_LENGTH))
            rTrail.removeLast();
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

        updateLocations(elapsedMs);

        /* Add idle animations */
        LXVector yLoc = new LXVector(
            -yellowRestLfo.getValuef() / 1.5f,
            yellowRestLfo.getValuef() / 2,
            -yellowRestLfo.getValuef());
        yLoc.add(yellowBase);
        LXVector rLoc = new LXVector(
            redRestLfo.getValuef() / 3,
            redRestLfo.getValuef(),
            -redRestLfo.getValuef() / 7);
        rLoc.add(redBase);

        updateTrails(yLoc, rLoc);

        float areaEffect = spiritSizeParam.getValuef();
        float heartSize = spiritHeartParam.getValuef();

        /* Change colors and sizes */
        float rHue = 0;
        float yHue = 51;
        float rAlpha = 1;
        float yAlpha = 1;

        float rHeartSize = heartSize;
        float yHeartSize = heartSize;
        float yArea = areaEffect;

        if (phase == Phase.SHAKING) {
            rHeartSize += Math.min(5 * phaseAge / 1000, 30);

            float yAtten = Math.min(5 * phaseAge / 1000, yArea - 5);
            yArea -= yAtten;
            yHeartSize += yAtten / 3f;
        }

        for (LXVector v : getVectors()) {
            double rDist = v.copy().mult(-1).add(rLoc).magSq();
            double yDist = v.copy().mult(-1).add(yLoc).magSq();

            /* This is quite a bit faster than the LXVector::dist method, partially because it's unwrapped
             * and partially because we compare squared distances to avoid the square root in the loop. */
            int t = 0;
            final float x = v.x;
            final float y = v.y;
            final float z = v.z;
            for (LXVector rv : rTrail) {
                float d = (rv.x - x) * (rv.x - x) + (rv.y - y) * (rv.y - y) + (rv.z - z) * (rv.z - z);
                d += t * TRAIL_WIDTH;
                if (d < rDist)
                    rDist = d;
                t++;
            }
            t = 0;
            for (LXVector yv : yTrail) {
                float d = (yv.x - x) * (yv.x - x) + (yv.y - y) * (yv.y - y) + (yv.z - z) * (yv.z - z);
                d += t * TRAIL_WIDTH;
                if (d < yDist)
                    yDist = d;
                t++;
            }

            rDist = Math.sqrt(rDist);
            yDist = Math.sqrt(yDist);

            double rStrength = rHeartSize - rDist;
            double yStrength = yHeartSize - yDist;

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
                    : LXColor.hsba(yHue, 100, 85 * Math.max(0, 1 - yDist / yArea), yAlpha);
            colors[v.index] = Ops8.add(color, colors[v.index]);
        }
    }

    private void nextPhase() {
        switch (phase) {
            case IDLE:
                phase = Phase.MOVE_IN;
                break;
            case MOVE_IN:
                theta = 0;
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
                chaseTarget = null;
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

    @Override
    public void noteOnReceived(MidiNoteOn note) {
        if (note.getPitch() == 60)
            nextPhase();
        if (note.getPitch() == 62)
            jerking = true;
    }

    @Override
    public void noteOffReceived(MidiNote note) {
        if (note.getPitch() == 62)
            jerking = false;
    }
}
