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

    private static final float RED_CHASE_DELAY = 500; // ms
    private static final float ROTATION_PERIOD_BASE = 4800; // ms
    private static final float ROTATION_PERIOD_AMPLITUDE = 600; // ms
    private static final float ROTATION_PERIOD_RATE = 10200; // ms
    private static final float TRAIL_MAX_AGE = 600; // ms
    private static final float TRAIL_MAX_AGE_CHASE = 200; // ms
    private static final float TRAIL_NEW_ELEM_AGE = 15; // ms
    private static final float TRAIL_WIDTH = 100.f; // inches shrunk per trail element

    private static final float CHASE_TIME = 1600f; // ms
    private static final float CHASE_SIZE_TRANSITION = 2000f; // ms
    private static final float RED_EAT_YELLOW_TRANSITION = 6000f; // ms
    private static final float RED_GROWTH = 25; // inches
    private static final float YELLOW_SHRINK_TO = 30; // inches

    private final SinLFO redRestLfo = new SinLFO(-40, 30, 4500);
    private final SinLFO yellowRestLfo = new SinLFO(35, -20, 5100);

    enum Phase {
        IDLE,
        MOVE_IN,
        ROTATING,
        RED_EATS_YELLOW,
        CHASE,
        OUT,
    }

    Phase phase = Phase.IDLE;
    Random random = new Random();
    float theta = 0;
    float phaseAge = 0;
    float moveAge = 0;

    private static final class TrailElement {
        LXVector loc;
        float age;
        boolean display;

        TrailElement(LXVector v) {
            loc = v;
            age = 0f;
            display = true;
        }
    }

    LXVector redBase, yellowBase;
    LXVector chaseStart, chaseTarget;

    /* these are used for the first red move, while it catches up to yellow */
    LXVector redChaseStart;
    boolean redCatchingUp;

    Deque<TrailElement> yTrail = new LinkedList<>();
    Deque<TrailElement> rTrail = new LinkedList<>();

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
                yVel = 80f / 1000f;
                rVel = 80f / 1000f;
                break;
            }

            case ROTATING:
            case RED_EATS_YELLOW: {
                float rate = ROTATION_PERIOD_BASE + (float) Math.sin(2 * Math.PI * phaseAge / ROTATION_PERIOD_RATE) * ROTATION_PERIOD_AMPLITUDE;
                if (phase == Phase.RED_EATS_YELLOW)
                    rate -= Math.min(1200f, phaseAge / 2.5f);
                theta += 2 * Math.PI * elapsedMs / rate;

                float rad = ellipseMinor + (float) ((ellipseMajor - ellipseMinor) / 2f * (Math.cos(2 * theta) + 1f));
                float yRad = rad;
                float rRad = rad;
                if (phase == Phase.RED_EATS_YELLOW)
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

                InterpResult y = expInterpToTarget(chaseStart, chaseTarget);
                yellowBase = y.loc;
                if (redCatchingUp) {
                    InterpResult r = expInterpToTarget(redChaseStart, chaseTarget);
                    redBase = r.loc;
                    if (r.t > 0.99 && y.t > 0.99) {
                        redCatchingUp = false;
                        pickChaseTarget();
                    }
                } else {
                    redBase = yTrail.peekLast().loc;
                    if (y.t > 0.99)
                        pickChaseTarget();
                }
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

    class InterpResult {
        float t;
        LXVector loc;
    }
    private InterpResult expInterpToTarget(LXVector start, LXVector end) {
        LXVector chaseVec = start.copy().mult(-1).add(end);
        float t = 2f * moveAge / (redCatchingUp ? CHASE_TIME + RED_CHASE_DELAY : CHASE_TIME);
        InterpResult res = new InterpResult();
        if (t < 1) {
            res.t = 0.5f * (float) Math.pow(2, 10 * (t - 1));
        } else {
            res.t = 0.5f * (float) (-Math.pow(2, -10 * (t - 1)) + 2);
        }
        chaseVec.mult(res.t);
        res.loc = start.copy().add(chaseVec);
        return res;
    }

    private void pickChaseTarget() {
        if (redCatchingUp)
            redChaseStart = redBase;
        chaseStart = yellowBase;
        LXVector[] vs = model.getVectorArray();
        do {
            int i = random.nextInt(vs.length);
            chaseTarget = vs[i];
        } while (chaseTarget.dist(chaseStart) < 0.3f * model.xRange);
        moveAge = 0;
    }

    private void updateTrails(double elapsedMs, LXVector yLoc, LXVector rLoc) {
        for (TrailElement e : yTrail)
            e.age += (float) elapsedMs;
        for (TrailElement e : rTrail)
            e.age += (float) elapsedMs;

        if (yTrail.isEmpty() || yTrail.peekFirst().age > TRAIL_NEW_ELEM_AGE)
            yTrail.addFirst(new TrailElement(yLoc));
        if (rTrail.isEmpty() || rTrail.peekFirst().age > TRAIL_NEW_ELEM_AGE)
            rTrail.addFirst(new TrailElement(rLoc));

        float yMaxKeepAge = phase == Phase.CHASE ? RED_CHASE_DELAY : TRAIL_MAX_AGE;
        float yMaxDisplayAge = phase == Phase.CHASE ? TRAIL_MAX_AGE_CHASE : TRAIL_MAX_AGE;
        float rMaxAge = phase == Phase.RED_EATS_YELLOW ? 2.5f * TRAIL_MAX_AGE : yMaxDisplayAge;

        /* Only remove one trail element per frame */
        if (yTrail.peekLast().age > yMaxKeepAge)
            yTrail.removeLast();
        for (TrailElement e : yTrail)
            e.display = e.age <= yMaxDisplayAge;

        if (rTrail.peekLast().age > rMaxAge)
            rTrail.removeLast();
    }

    @Override
    public void run(double elapsedMs) {
        phaseAge += elapsedMs;
        moveAge += elapsedMs;

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

        updateTrails(elapsedMs, yLoc, rLoc);

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
        float rArea = areaEffect;

        if (phase == Phase.RED_EATS_YELLOW || phase == Phase.CHASE) {
            float rBoost, yAttenuate;

            if (phase == Phase.RED_EATS_YELLOW) {
                rBoost = Math.min(phaseAge / RED_EAT_YELLOW_TRANSITION, 1f);
                yAttenuate = Math.min(phaseAge / RED_EAT_YELLOW_TRANSITION, 1f);
            } else {
                rBoost = 1f - Math.min(phaseAge / CHASE_SIZE_TRANSITION, 1f);
                yAttenuate = 1f - Math.min(phaseAge / CHASE_SIZE_TRANSITION, 1f);
            }

            rHeartSize += rBoost * RED_GROWTH * 0.333;
            rArea += rBoost * RED_GROWTH * 0.667;

            float newYArea = YELLOW_SHRINK_TO + (1f - yAttenuate) * (yArea - YELLOW_SHRINK_TO);
            yArea = newYArea;
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
            for (TrailElement e : rTrail) {
                if (!e.display)
                    continue;
                LXVector rv = e.loc;
                float d = (rv.x - x) * (rv.x - x) + (rv.y - y) * (rv.y - y) + (rv.z - z) * (rv.z - z);
                d += t * TRAIL_WIDTH;
                if (d < rDist)
                    rDist = d;
                t++;
            }
            t = 0;
            for (TrailElement e : yTrail) {
                if (!e.display)
                    continue;
                LXVector yv = e.loc;
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

            int color;
            if (rStrength > yStrength) {
                float b = 100f * (float) Math.sqrt(Float.max(0f, 1f - (float) rDist / rArea));
                color = rDist < rHeartSize
                    ? LXColor.hsba(rHue, 100 * (1 - (rHeartSize - rDist) / rHeartSize), 100, rAlpha)
                    : LXColor.hsba(rHue, 100, b, rAlpha);
            } else {
                float b = 90f * (float) Math.sqrt(Float.max(0f, 1f - (float) yDist / yArea));
                color = yDist < yHeartSize
                    ? LXColor.hsba(yHue, 100 * (1 - (yHeartSize - yDist) / yHeartSize), 100, yAlpha)
                    : LXColor.hsba(yHue, 100, b, yAlpha);
            }
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
                phase = Phase.RED_EATS_YELLOW;
                break;
            case RED_EATS_YELLOW:
                phase = Phase.CHASE;
                moveAge = 0;
                redCatchingUp = true;
                break;
            case CHASE:
                chaseTarget = null;
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
    }

    @Override
    public void noteOffReceived(MidiNote note) {
    }
}
