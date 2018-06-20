package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.ping.SLPatternWithMarkers;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.OctahedronWithArrow;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static heronarts.lx.PolyBuffer.Space.SRGB8;

public class Starlings extends SLPatternWithMarkers {
    private List<Bird> falcons = new ArrayList<>();
    private List<Bird> starlings = new ArrayList<>();
    private CompoundParameter tmSclParam = new CompoundParameter("TmScl", 1, 0, 20);
    private DiscreteParameter countParam = new DiscreteParameter("Count", 10, 0, 100);
    private CompoundParameter rangeParam = new CompoundParameter("Range", 10, 0, 100);
    private CompoundParameter sepRngParam = new CompoundParameter("SepRng", 5, 0, 100);
    private CompoundParameter sepFrcParam = new CompoundParameter("SepFrc", 100, 0, 1000);
    private CompoundParameter cohWtParam = new CompoundParameter("CohWt", 0.5, 0, 1);
    private CompoundParameter cohMaxParam = new CompoundParameter("CohMax", 1, 0, 10);
    private CompoundParameter alnWtParam = new CompoundParameter("AlnWt", 0.5, 0, 1);
    private CompoundParameter alnMaxParam = new CompoundParameter("AlnMax", 1, 0, 10);
    private CompoundParameter sepWtParam = new CompoundParameter("SepWt", 0.5, 0, 1);
    private CompoundParameter sepMaxParam = new CompoundParameter("SepMax", 1, 0, 10);
    private CompoundParameter grvWtParam = new CompoundParameter("GrvWt", 0.5, 0, 1);
    private CompoundParameter grvMaxParam = new CompoundParameter("GrvMax", 1, 0, 10);
    private CompoundParameter escWtParam = new CompoundParameter("EscWt", 0.5, 0, 1);
    private CompoundParameter escMaxParam = new CompoundParameter("EscMax", 1, 0, 10);
    private CompoundParameter tkOffParam = new CompoundParameter("TkOff", 0.5, 0, 10);
    private CompoundParameter minSpdParam = new CompoundParameter("MinSpd", 1, 0, 10);
    private CompoundParameter maxSpdParam = new CompoundParameter("MaxSpd", 10, 0, 10);
    private CompoundParameter radiusParam = new CompoundParameter("Radius", 10, 0, 20);
    private DiscreteParameter falcParam = new DiscreteParameter("Falc", 1, 0, 10);

    private Random random = new Random(0);
    private float t = 0;
    private int nextHue = 0;

    public Starlings(LX lx) {
        super(lx);
        addParameter(tmSclParam);
        addParameter(countParam);
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
        addParameter(radiusParam);
        addParameter(falcParam);
    }

    public void run(double deltaMs, PolyBuffer.Space space) {
        updateStarlingCount(countParam.getValuei());
        updateFalconCount(falcParam.getValuei());
        advanceBirds(deltaMs * tmSclParam.getValue());
        int[] colors = (int[]) getArray(SRGB8);
        drawBirds(colors);
        markModified(SRGB8);
    }

    public List<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<>();
        synchronized (starlings) {
            for (Bird b : starlings) {
                markers.add(new OctahedronWithArrow(b.pos, 2, b.color, b.vel, b.color));
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
            LXPoint[] points = model.points;
            while (falcons.size() < count) {
                falcons.add(new Bird(points[random.nextInt(points.length)]));
            }
        }
    }

    private void trimList(List list, int count) {
        while (list.size() > count) {
            list.remove(list.size() - 1);
        }
    }

    private void removeFarawayBirds(List<Bird> birds) {
        Iterator<Bird> iter = birds.iterator();
        while (iter.hasNext()) {
            Bird b = iter.next();
            if (Math.abs(b.pos.x - model.cx) > model.xRange * 2 + 100 ||
                Math.abs(b.pos.y - model.cy) > model.yRange * 2 + 100 ||
                Math.abs(b.pos.z - model.cz) > model.zRange * 2 + 100) {
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
        float minSpeed = minSpdParam.getValuef();
        float maxSpeed = maxSpdParam.getValuef();
        float takeOffAccel = tkOffParam.getValuef();
        t += deltaSec;
        int i = 0;
        for (Bird b : starlings) {
            List<Bird> neighbours = getNearbyStarlings(b, rangeParam.getValuef());
            PVector accel = new PVector();
            if (!neighbours.isEmpty()) {
                accel.add(limitMagnitude(getCohesionAccel(b, neighbours).mult(cohesionWeight), cohesionMax));
                accel.add(limitMagnitude(getAlignmentAccel(b, neighbours).mult(alignmentWeight), alignmentMax));
                accel.add(limitMagnitude(getSeparationAccel(b, neighbours).mult(separationWeight), separationMax));
                accel.add(limitMagnitude(getGravityAccel(b).mult(gravityWeight), gravityMax));
                //accel.add(getEscapeAccel(b).mult(escWtParam.getValuef()));
            }
            if (!b.inFlight && accel.mag() > takeOffAccel) {
                b.inFlight = true;
            }
            if (b.inFlight) {
                b.vel.add(accel.copy().mult(deltaSec));
                limitMagnitude(b.vel, minSpeed, maxSpeed);
            }
            b.pos.add(b.vel.copy().mult(deltaSec));

            if (i++ == 0) {
                System.out.println(String.format(
                    "%5.2f: pos %s, vel %s, acc %s", t, format(b.pos), format(b.vel), format(accel)
                ));
            }
        }
    }

    private void drawBirds(int[] colors) {
        float rad = radiusParam.getValuef();
        for (LXPoint p : model.points) {
            colors[p.index] = 0;
            for (Bird b : starlings) {
                if (Math.abs(b.pos.x - p.x) < rad &&
                      Math.abs(b.pos.y - p.y) < rad &&
                      Math.abs(b.pos.z - p.z) < rad &&
                      b.pos.dist(new PVector(p.x, p.y, p.z)) < rad) {
                    colors[p.index] = b.color;
                    break;
                }
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
            float d = n.pos.dist(b.pos);
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

    class Bird {
        public boolean inFlight;
        public PVector pos;
        public PVector vel;
        public int color;

        public Bird(float x, float y, float z) {
            inFlight = false;
            pos = new PVector(x, y, z);
            vel = new PVector(0, 0, 0);
            color = LXColor.hsb(nextHue, 60, 100);
            nextHue += 10;
        }

        public Bird(LXPoint point) {
            this(point.x, point.y, point.z);
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

    public static PVector limitMagnitude(PVector v, float maxMag) {
        float mag = v.mag();
        if (mag > maxMag) {
            v.mult(maxMag / mag);
        }
        return v;
    }
}
