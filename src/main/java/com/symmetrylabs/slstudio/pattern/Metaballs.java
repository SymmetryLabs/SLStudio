package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.MathUtils;
import com.symmetrylabs.util.SphereMarker;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.transform.LXVector;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Metaballs extends SLPattern<SLModel> {
    public enum FieldMapping {
        HUE,
        LUM,
    }

    private final CompoundParameter add = new CompoundParameter("Add", 100, 0, 360);
    private final CompoundParameter hueLimit = new CompoundParameter("MaxHue", 250,0, 360);
    private final CompoundParameter falloff = new CompoundParameter("Falloff", 3.6,0.1, 10);
    private final EnumParameter<FieldMapping> mapping = new EnumParameter<>("map", FieldMapping.HUE);
    private final ArrayList<Ball> balls;

    public Metaballs(LX lx) {
        super(lx);
        balls = new ArrayList<>();
        balls.add(new Ball(
            new Interpolator(lx.model.xRange / 1.8, 12000, lx.model.cx, 0),
            new Interpolator(lx.model.yRange / 2.1, 8000, lx.model.cy + 0.1 * lx.model.yRange, 1500),
            new Interpolator(lx.model.zRange / 1.6, 27000, lx.model.cz, 0),
            1f
            ));
        balls.add(new Ball(
            new Interpolator(lx.model.xRange / 1.4, 9000, lx.model.cx - 0.25 * lx.model.xRange, 2000),
            new Interpolator(lx.model.yRange / 3.1, 7000, lx.model.cy + 0.1 * lx.model.yRange, 3000),
            new Interpolator(lx.model.zRange, 30000, lx.model.cz, 0),
            2f
        ));
        balls.add(new Ball(
            new Interpolator(lx.model.xRange / 2.0, 19000, lx.model.cx, 9000),
            new Interpolator(lx.model.yRange / 2.0, 23000, lx.model.cy, 3000),
            new Interpolator(lx.model.zRange * 7.0, 8000, lx.model.cz, 0),
            1.5f
        ));

        addParameter(add);
        addParameter(hueLimit);
        addParameter(falloff);
        addParameter(mapping);
    }

    @Override
    public void run(double deltaMs) {
        float hl = hueLimit.getValuef();
        float fo = falloff.getValuef();
        float a = add.getValuef();

        for (Ball b : balls) {
            b.advance(deltaMs);
        }

        getVectorList().parallelStream().forEach(p -> {
            double f = 0;
            for (Ball b : balls) {
                f += b.eval(p);
            }
            float h = 360e-3f * fo / (float)f - a;

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

    private final class Interpolator {
        private final double halfrange, timescale, period, center;
        double t;

        Interpolator(double halfrange, double period, double center, double timeOffset) {
            this.halfrange = halfrange;
            this.period = period;
            this.center = center;
            timescale = Math.PI * 2 / period;
            t = timeOffset;
        }

        double advance(double deltaMs) {
            t += deltaMs;
            while (t > period)
                t -= period;
            return get();
        }

        double get() {
            return halfrange * Math.sin(t * timescale) + center;
        }
    }

    private final class Ball {
        private final Interpolator x, y, z;
        private final double strength;

        /**
         * The negation of the origin of the ball. Calculated on every frame, cached
         * so we don't have to calculate for every point eval. Stored as a negative so
         * we can add it to a point to transform the point into ball-relative coordinates
         * in a single operation.
          */
        LXVector c;

        Ball(Interpolator x, Interpolator y, Interpolator z, double strength) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.strength = strength;
            c = new LXVector(0, 0, 0);
        }

        final void advance(double deltaMs) {
            c.set(
                (float) -x.advance(deltaMs),
                (float) -y.advance(deltaMs),
                (float) -z.advance(deltaMs));
        }

        final double eval(LXVector p) {
            LXVector v = c.copy().add(p);
            double f = strength / Double.max(0.001, Math.sqrt(v.dot(v)));
            return f;
        }
    }

    @Override
    public Collection<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<>();
        for (Ball b : balls) {
            markers.add(new SphereMarker(
                new PVector((float) b.x.get(), (float) b.y.get(), (float) b.z.get()), 12f, LXColor.RED));
        }
        return markers;
    }
}
