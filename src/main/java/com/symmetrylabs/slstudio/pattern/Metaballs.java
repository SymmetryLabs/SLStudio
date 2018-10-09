package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.MathUtils;
import com.symmetrylabs.util.SphereMarker;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class Metaballs extends SLPattern<SLModel> {
    public enum FieldMapping {
        HUE,
        LUM,
    }

    private final CompoundParameter add = new CompoundParameter("add", 100, 0, 360);
    private final CompoundParameter hueLimit = new CompoundParameter("maxhue", 250,0, 360);
    private final CompoundParameter falloff = new CompoundParameter("falloff", 3.6,0.1, 100);
    private final CompoundParameter attract = new CompoundParameter("attract", 4,1, 100);
    private final CompoundParameter repel = new CompoundParameter("repel", 2,1, 12);
    private final CompoundParameter jump = new CompoundParameter("jump", 5,0.2, 20);
    private final EnumParameter<FieldMapping> mapping = new EnumParameter<>("map", FieldMapping.HUE);
    private final DiscreteParameter count = new DiscreteParameter("count", 3, 0, 30);
    private final DiscreteParameter posterize = new DiscreteParameter("poster", 0, 0, 9);
    private final ArrayList<Ball> balls = new ArrayList<>();
    private final Random random = new Random();

    public Metaballs(LX lx) {
        super(lx);
        addParameter(add);
        addParameter(hueLimit);
        addParameter(falloff);
        addParameter(mapping);
        addParameter(count);
        addParameter(attract);
        addParameter(repel);
        addParameter(posterize);
        addParameter(jump);
        refillBalls();
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == count) {
            refillBalls();
        }
    }

    private void refillBalls() {
        int c = count.getValuei();
        if (balls.size() > c) {
            balls.subList(c, balls.size()).clear();
        } else {
            while (balls.size() < c) {
                balls.add(new Ball(1f));
            }
        }
    }

    @Override
    public void run(double deltaMs) {
        final float hl = hueLimit.getValuef();
        final float fo = falloff.getValuef();
        final float a = add.getValuef();
        final int poster = posterize.getValuei();

        for (Ball b : balls) {
            b.advance(deltaMs);
        }
        for (Ball b : balls) {
            b.commit();
        }

        getVectorList().parallelStream().forEach(p -> {
            double f = 0;
            for (Ball b : balls) {
                f += b.eval(p);
            }
            float h = 360e-3f * fo / (float)f - a;
            if (poster > 0) {
                int bucket = (int) (h * poster / 360f);
                h = 360f * bucket / poster;
            }

            int c = 0;
            switch (mapping.getEnum()) {
                case HUE: {
                    float b = h < hl ? 100f : MathUtils.constrain(100f - 1.2f * (h - hl), 0f, 100f);
                    h = MathUtils.constrain(h, 0f, hl);
                    c = LXColor.hsb(h, 100f, b);
                    break;
                }
                case LUM: {
                    float b = MathUtils.constrain((360f - h) / 3.6f, 0f, 100f);
                    c = LXColor.gray(b);
                }
            }
            colors[p.index] = c;
        });
    }

    private final class Ball {
        private final double strength;
        private LXVector target;

        /**
         * The negation of the origin of the ball. Calculated on every frame, cached
         * so we don't have to calculate for every point eval. Stored as a negative so
         * we can add it to a point to transform the point into ball-relative coordinates
         * in a single operation.
          */
        LXVector c;

        /**
         * The position we'll be in in the next frame. This is used because the balls
         * need to know the positions of other balls for the repel calculation, but we
         * don't want them moving during the loop where their positions are calculated.
         */
        LXVector next;

        LXVector v;

        Ball(double strength) {
            this.strength = strength;
            v = new LXVector(0, 0, 0);
            target = new LXVector(0, 0, 0);
            pickRandomTarget();
            c = target.copy().mult(-1);
        }

        private void pickRandomTarget() {
            LXPoint p = null;
            while (p == null) {
                p = model.points[random.nextInt(model.points.length)];
            }
            target.set(p.x, p.y, p.z);
        }

        final void advance(double deltaMs) {
            LXVector a = target.copy().add(c);
            double t = deltaMs / 1000;
            double d2 = a.magSq();

            /* drop a little energy on each frame */
            v.mult(0.96f);

            if (d2 > 1e-2) {
                double d = Math.sqrt(d2);
                double F = Math.log(Math.pow(d, 1.1) / 10 * attract.getValue());
                a.mult((float) (F * t / d));

                for (Ball b : balls) {
                    if (b == this) {
                        continue;
                    }
                    /* points from other center to this center */
                    LXVector c2c = c.copy().mult(-1).add(b.c);
                    double rd2 = c2c.magSq();
                    if (rd2 < 1e-2) {
                        continue;
                    }
                    /* only you can prevent wildfires */
                    rd2 = Math.max(rd2, 1);
                    double rF = Math.pow(4, repel.getValue()) / rd2;
                    c2c.setMag((float) (rF * t));
                    a.add(c2c);
                }

                v.add(a);

                if (random.nextFloat() < t / jump.getValue() || d2 < 15) {
                    pickRandomTarget();
                }
            } else {
                pickRandomTarget();
            }

            /* negation here because c is the negation of our actual position */
            next = c.copy().add(v.copy().mult(-1));
        }

        final void commit() {
            c = next;
        }

        final double eval(LXVector p) {
            LXVector v = c.copy().add(p);
            double f = strength / Double.max(0.001, v.mag());
            return f;
        }
    }

    @Override
    public Collection<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<>();
        for (Ball b : balls) {
            markers.add(new SphereMarker(new PVector(-b.c.x, -b.c.y, -b.c.z), 12f, LXColor.RED));
            markers.add(new SphereMarker(new PVector(b.target.x, b.target.y, b.target.z), 12f, LXColor.GREEN));
        }
        return markers;
    }
}
