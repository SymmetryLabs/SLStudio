package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.ping.SLPatternWithMarkers;
import com.symmetrylabs.util.CubeMarker;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.OctahedronWithArrow;
import com.symmetrylabs.util.OctreeModelIndex;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static heronarts.lx.PolyBuffer.Space.SRGB8;

public class Starlings extends SLPatternWithMarkers {
    public enum ColorMode {RANDOM, PALETTE, GREYSCALE};

    private List<Bird> falcons = new ArrayList<>();
    private List<Bird> starlings = new ArrayList<>();

    private CompoundParameter tmSclParam = new CompoundParameter("TmScl", 1, 0, 20);
    private EnumParameter<ColorMode> clrModeParam = new EnumParameter<>("ClrMode", ColorMode.RANDOM);
    private CompoundParameter satParam = new CompoundParameter("Sat", 0.6, 0, 1);
    private CompoundParameter brtParam = new CompoundParameter("Brt", 0.6, 0, 1);
    private CompoundParameter fltrSecParam = new CompoundParameter("FltrSec", 8, 0, 20);
    private CompoundParameter fltrHzParam = new CompoundParameter("FltrHz", 1, 0, 4);

    private DiscreteParameter numStarParam = new DiscreteParameter("# Star", 10, 0, 500);
    private CompoundParameter rangeParam = new CompoundParameter("Range", 10, 0, 100);
    private CompoundParameter sepRngParam = new CompoundParameter("SepRng", 5, 0, 100);
    private CompoundParameter sepFrcParam = new CompoundParameter("SepFrc", 100, 0, 500);

    private CompoundParameter cohWtParam = new CompoundParameter("CohWt", 0.5, 0, 1);
    private CompoundParameter cohMaxParam = new CompoundParameter("CohMax", 1, 0, 10);
    private CompoundParameter alnWtParam = new CompoundParameter("AlnWt", 0.5, 0, 1);
    private CompoundParameter alnMaxParam = new CompoundParameter("AlnMax", 1, 0, 10);
    private CompoundParameter sepWtParam = new CompoundParameter("SepWt", 0.5, 0, 1);
    private CompoundParameter sepMaxParam = new CompoundParameter("SepMax", 1, 0, 10);
    private CompoundParameter grvWtParam = new CompoundParameter("GrvWt", 0.5, 0, 1);
    private CompoundParameter grvMaxParam = new CompoundParameter("GrvMax", 1, 0, 10);
    private CompoundParameter escWtParam = new CompoundParameter("EscWt", 1, 0, 500);
    private CompoundParameter escMaxParam = new CompoundParameter("EscMax", 1, 0, 500);

    private CompoundParameter tkOffParam = new CompoundParameter("TkOff", 0.5, 0, 10);
    private CompoundParameter minSpdParam = new CompoundParameter("MinSpd", 1, 0, 20);
    private CompoundParameter maxSpdParam = new CompoundParameter("MaxSpd", 10, 0, 20);
    private CompoundParameter dragParam = new CompoundParameter("Drag", 0.2, 0, 1);
    private CompoundParameter yDragParam = new CompoundParameter("YDrag", 0.4, 0, 1);
    private CompoundParameter radiusParam = new CompoundParameter("Radius", 10, 0, 20);

    private DiscreteParameter numFalcParam = new DiscreteParameter("# Falc", 1, 0, 10);
    private CompoundParameter falcSpdParam = new CompoundParameter("FalcSpd", 1, 0, 20);
    private CompoundParameter falcRngParam = new CompoundParameter("FalcRng", 100, 0, 500);
    private CompoundParameter falcDlyParam = new CompoundParameter("FalcDly", 4, 0, 480);

    private CompoundParameter landSecParam = new CompoundParameter("LandSec", 2, 0, 240);
    private CompoundParameter landDrgParam = new CompoundParameter("LandDrg", 0.1, 0, 1);
    private CompoundParameter landSpdParam = new CompoundParameter("LandSpd", 1, 0, 4);
    private CompoundParameter landRngParam = new CompoundParameter("LandRng", 6, 0, 24);

    private Random random = new Random(0);
    private OctreeModelIndex modelIndex;

    public Starlings(LX lx) {
        super(lx);
        onVectorsChanged();

        addParameter(tmSclParam);
        addParameter(clrModeParam);
        addParameter(satParam);
        addParameter(brtParam);
        addParameter(fltrSecParam);
        addParameter(fltrHzParam);

        addParameter(numStarParam);
        addParameter(rangeParam);
        addParameter(sepRngParam);
        addParameter(sepFrcParam);

        addParameter(cohWtParam);
        addParameter(cohMaxParam);
        addParameter(alnWtParam);
        addParameter(alnMaxParam);
        addParameter(sepWtParam);
        addParameter(sepMaxParam);
        addParameter(grvWtParam);
        addParameter(grvMaxParam);
        addParameter(escWtParam);
        addParameter(escMaxParam);

        addParameter(tkOffParam);
        addParameter(minSpdParam);
        addParameter(maxSpdParam);
        addParameter(dragParam);
        addParameter(yDragParam);
        addParameter(radiusParam);

        addParameter(numFalcParam);
        addParameter(falcSpdParam);
        addParameter(falcRngParam);
        addParameter(falcDlyParam);

        addParameter(landSecParam);
        addParameter(landSpdParam);
        addParameter(landRngParam);
    }

    public void onVectorsChanged() {
        super.onVectorsChanged();
        modelIndex = new OctreeModelIndex(model, getVectors());
    }

    public void run(double deltaMs, PolyBuffer.Space space) {
        updateStarlingCount(numStarParam.getValuei());
        updateFalconCount(numFalcParam.getValuei());
        advanceBirds(deltaMs * tmSclParam.getValue());
        int[] colors = (int[]) getArray(SRGB8);
        drawBirds(colors);
        markModified(SRGB8);
    }

    public List<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<>();
        synchronized (starlings) {
            for (Bird b : starlings) {
                int c = b.color & 0xffffff | 0x40000000;
                markers.add(new OctahedronWithArrow(b.pos, 1, c, b.vel, c));
            }
        }
        synchronized (falcons) {
            for (Bird b : falcons) {
                markers.add(new CubeMarker(b.pos, falcRngParam.getValuef(), 0xff0000));
            }
        }
        return markers;
    }

    private void updateStarlingCount(int count) {
        synchronized (starlings) {
            trimList(starlings, count);
            removeFarawayBirds(starlings);
            LXPoint[] points = model.points;
            while (starlings.size() < count) {
                starlings.add(new Bird(points[random.nextInt(points.length)]));
            }
        }
    }

    private void updateFalconCount(int count) {
        synchronized (falcons) {
            trimList(falcons, count);
            removeFarawayBirds(falcons);
            float range = (model.xRange + model.yRange + model.zRange) / 3;
            while (falcons.size() < count) {
                float x = randomFloat(range*2, range*3) * (random.nextBoolean() ? 1 : -1);
                float y = randomFloat(range*2, range*3) * (random.nextBoolean() ? 1 : -1);
                float z = randomFloat(range*2, range*3) * (random.nextBoolean() ? 1 : -1);
                PVector pos = new PVector(model.cx + x, model.cy + y, model.cz + z);
                PVector vel = new PVector(-x, -y, -z);
                vel.div(vel.mag());
                vel.mult(falcSpdParam.getValuef());
                falcons.add(new Bird(pos, vel, falcDlyParam.getValuef()));
            }
        }
    }

    private void trimList(List list, int count) {
        while (list.size() > count) {
            list.remove(list.size() - 1);
        }
    }

    private float randomFloat(float min, float max) {
        return min + (max - min) * random.nextFloat();
    }

    private void removeFarawayBirds(List<Bird> birds) {
        Iterator<Bird> iter = birds.iterator();
        float range = (model.xRange + model.yRange + model.zRange) / 3;
        float radius = range * 5 + 100;
        while (iter.hasNext()) {
            Bird b = iter.next();
            if (Math.abs(b.pos.x - model.cx) > radius ||
                Math.abs(b.pos.y - model.cy) > radius ||
                Math.abs(b.pos.z - model.cz) > radius) {
                iter.remove();
            }
        }
    }

    private void advanceBirds(double deltaMs) {
        float deltaSec = (float) deltaMs * 0.001f;
        float cohesionWeight = cohWtParam.getValuef();
        float cohesionMax = cohMaxParam.getValuef();
        float alignmentWeight = alnWtParam.getValuef();
        float alignmentMax = alnMaxParam.getValuef();
        float separationWeight = sepWtParam.getValuef();
        float separationMax = sepMaxParam.getValuef();
        float gravityWeight = grvWtParam.getValuef();
        float gravityMax = grvMaxParam.getValuef();
        float escapeWeight = escWtParam.getValuef();
        float escapeMax = escMaxParam.getValuef();
        float takeOffAccel = tkOffParam.getValuef();

        for (Bird b : starlings) {
            b.safeSec += deltaSec;
            if (b.inFlight) {
                b.inFlightSec += deltaSec;
                b.landedSec = 0;
            } else {
                b.landedSec += deltaSec;
                b.inFlightSec = 0;
            }
            List<Bird> neighbours = getNearbyStarlings(b, rangeParam.getValuef());
            PVector accel = new PVector();
            if (!neighbours.isEmpty()) {
                accel.add(getCohesionAccel(b, neighbours).mult(cohesionWeight).limit(cohesionMax));
                accel.add(getAlignmentAccel(b, neighbours).mult(alignmentWeight).limit(alignmentMax));
                accel.add(getSeparationAccel(b, neighbours).mult(separationWeight).limit(separationMax));
                accel.add(getGravityAccel(b).mult(gravityWeight).limit(gravityMax));
                accel.add(getEscapeAccel(b, falcons).mult(escapeWeight).limit(escapeMax));
            }
            if (!b.inFlight && accel.mag() > takeOffAccel) {
                b.inFlight = true;
                b.inFlightSec = 0;
                b.landedSec = 0;
            }
            if (b.inFlight) {
                b.vel.add(accel.copy().mult(deltaSec));
                adjustVelocity(b, deltaSec);
            }
            b.pos.add(b.vel.copy().mult(deltaSec));
        }
        for (Bird b : falcons) {
            if (b.delaySec > 0) {
                b.delaySec -= deltaSec;
            } else {
                b.pos.add(b.vel.copy().mult(deltaSec));
            }
        }
    }

    private void drawBirds(int[] colors) {
        float rad = radiusParam.getValuef();
        float flutterSec = fltrSecParam.getValuef();
        float flutterHertz = fltrHzParam.getValuef();
        final double TAU = Math.PI * 2;

        for (LXPoint p : model.points) {
            colors[p.index] = 0;
        }
        for (Bird b : starlings) {
            for (LXPoint p : modelIndex.pointsWithin(b.asLXPoint(), rad)) {
                int c = b.color;
                int alpha = 255;
                float sec = b.inFlight ? b.inFlightSec : b.landedSec;
                if (!b.inFlight && sec < flutterSec) {
                    double t = sec * flutterHertz;
                    double wave = Math.sin(t * TAU) * Math.sin(t * TAU) * 1 / (1 + sec / flutterSec);
                    alpha = (int) (0.5 + 255 * (1 - wave) / 2);
                }
                c = Ops8.rgba(Ops8.red(c), Ops8.green(c), Ops8.blue(c), alpha);
                colors[p.index] = Ops8.add(colors[p.index], c);
            }
        }
    }

    private String format(PVector v) {
        return String.format("(%7.2f,%7.2f,%7.2f)", v.x, v.y, v.z);
    }

    private List<Bird> getNearbyStarlings(Bird b, float range) {
        List<Bird> neighbours = new ArrayList<>();
      for (Bird n : starlings) {
          if (n != b &&
              abs(n.pos.x - b.pos.x) < range &&
                abs(n.pos.y - b.pos.y) < range &&
                abs(n.pos.z - b.pos.z) < range &&
                n.pos.dist(b.pos) < range) {
                neighbours.add(n);
            }
        }
        return neighbours;
    }

    private PVector getCohesionAccel(Bird b, List<Bird> neighbours) {
        PVector center = new PVector();
        for (Bird n : neighbours) {
            center.add(n.pos);
        }
        center.div(neighbours.size());
        PVector vel = center.sub(b.pos);
        return vel.sub(b.vel);
    }

    private PVector getAlignmentAccel(Bird b, List<Bird> neighbours) {
        PVector avgVel = new PVector();
        for (Bird n : neighbours) {
            avgVel.add(n.vel);
        }
        avgVel.div(neighbours.size());
        return avgVel.sub(b.vel);
    }

    private PVector getSeparationAccel(Bird b, List<Bird> neighbours) {
        PVector accel = new PVector();
        float sepRange = sepRngParam.getValuef();
        float sepForce = sepFrcParam.getValuef();
        int count = 0;
        for (Bird n : neighbours) {
            float d = b.pos.dist(n.pos);
            if (d < sepRange) {
                float mag = sepForce / (d < 1 ? 1 : d);
                accel.add(b.pos.copy().sub(n.pos).mult(mag / d));
                count++;
            }
        }
        if (count > 0) {
            accel.div(count);
        }
        return accel;
    }

    private PVector getGravityAccel(Bird b) {
        return new PVector(
            model.cx - b.pos.x, model.cy - b.pos.y, model.cz - b.pos.z
        ).div((model.xRange + model.yRange + model.zRange)/3);
    }

    private PVector getEscapeAccel(Bird b, List<Bird> falcons) {
        float falconRange = falcRngParam.getValuef();
        PVector accel = new PVector();
        int count = 0;
        for (Bird f : falcons) {
            float d = b.pos.dist(f.pos) + 0.1f;
            if (d < falconRange) {
                accel.add(b.pos.copy().sub(f.pos).div(d * d));
                count++;
                b.safeSec = 0;
            }
        }
        if (count > 0) {
            accel.div(count);
        }
        return accel;
    }

    private void adjustVelocity(Bird bird, float deltaSec) {
        float speed = bird.vel.mag();
        float minSpeed = minSpdParam.getValuef();
        float maxSpeed = maxSpdParam.getValuef();
        float drag = dragParam.getValuef();
        float yDrag = yDragParam.getValuef();
        boolean canLand = bird.inFlight && bird.safeSec >= landSecParam.getValuef();

        if (canLand) {
            int count = modelIndex.pointsWithin(bird.asLXPoint(), landRngParam.getValuef()).size();
            drag += landDrgParam.getValuef() * count;
            minSpeed = landSpdParam.getValuef() * 0.9f;
        }

        float dragAccel = drag * speed;
        float newSpeed = speed - dragAccel * deltaSec;
        if (newSpeed < minSpeed) newSpeed = minSpeed;
        if (newSpeed > maxSpeed) newSpeed = maxSpeed;
        bird.vel.mult(newSpeed / speed);

        if (yDrag > 0) {
            speed = Math.abs(bird.vel.y);
            dragAccel = yDrag * speed;
            newSpeed = speed - dragAccel * deltaSec;
            if (newSpeed < 0) newSpeed = 0;
            bird.vel.y *= newSpeed / speed;
        }

        if (canLand && bird.vel.mag() < landSpdParam.getValuef()) {
            LXPoint point = modelIndex.nearestPoint(bird.asLXPoint());
            if (point != null) {
                bird.pos = new PVector(point.x, point.y, point.z);
                bird.vel = new PVector(0, 0, 0);
                bird.inFlight = false;
                bird.inFlightSec = 0;
                bird.landedSec = 0;
            }
        }
    }

    private int nextHue = 0;

    class Bird {
        public boolean inFlight;
        public PVector pos;
        public PVector vel;
        public int color;
        public float delaySec;
        public float safeSec;
        public float inFlightSec;
        public float landedSec;

        public Bird(PVector pos, PVector vel, float delaySec) {
            this.pos = pos.copy();
            this.vel = vel.copy();
            inFlight = vel.x != 0 || vel.y != 0 || vel.z != 0;
            color = selectColor(pos.x, pos.y, pos.z);
            this.delaySec = delaySec;
            safeSec = 0;
            inFlightSec = 0;
            landedSec = 0;
        }

        public Bird(PVector pos, PVector vel) {
            this(pos, vel, 0);
        }

        private int selectColor(float x, float y, float z) {
            float sat = satParam.getValuef() * 100;
            float brt = brtParam.getValuef() * 100;
            switch (clrModeParam.getEnum()) {
                case RANDOM:
                    return LXColor.hsb(nextHue += 10, sat, brt);
                case PALETTE:
                    return lx.palette.getColor(new LXPoint(x, y, z), sat, brt);
                case GREYSCALE:
                    return LXColor.hsb(0, 0, lx.palette.getHue(new LXPoint(x, y, z)) * 100 / 360);
            }
            return 0;
        }

        public Bird(PVector pos) {
            this(pos, new PVector());
        }

        public Bird(float x, float y, float z, float vx, float vy, float vz) {
            this(new PVector(x, y, z), new PVector(vx, vy, vz));
        }

        public Bird(float x, float y, float z) {
            this(x, y, z, 0, 0, 0);
        }

        public Bird(LXPoint point) {
            this(point.x, point.y, point.z);
        }

        public LXPoint asLXPoint() {
            return new LXPoint(pos.x, pos.y, pos.z);
        }
    }

    public static float abs(float x) {
        return x > 0 ? x : -x;
    }

    public static PVector limitMagnitude(PVector v, float minMag, float maxMag) {
        float mag = v.mag();
        if (mag < minMag) {
            v.mult(minMag / mag);
        } else if (mag > maxMag) {
            v.mult(maxMag / mag);
        }
        return v;
    }
}
