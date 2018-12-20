package com.symmetrylabs.slstudio.pattern.instruments;

import com.symmetrylabs.color.Ops16;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import heronarts.lx.LXUtils;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;

import static heronarts.lx.PolyBuffer.Space.RGB16;

public class SparkleEmitter implements Emitter {
    @Override
    public Sparkle emit(Instrument.ParameterSet paramSet, int pitch, double intensity) {
        return new Sparkle(
            new LXVector(paramSet.getPoint(pitch, MarkUtils.randomXyDisc())),
            paramSet.getSize(0),
            paramSet.getHue(),
            paramSet.getHueVar(),
            paramSet.getSat(),
            paramSet.getTwist(),
            1/(1 + paramSet.getRate(2 * intensity - 1) * 4),
            paramSet.getDecaySec() + 0.05
        );
    }

    @Override public int getMaxCount() {
        return 40;
    }

    class Sparkle implements Mark {
        public LXVector center;
        public double radius;
        public double hue;
        public double hueVar;
        public double sat;
        public double density;
        public double attackSec;
        public double decaySec;
        public boolean isSustaining = true;

        protected LXModel model;
        protected float leftoverSec = 0;
        protected List<Spark> sparks = new LinkedList<>();
        protected List<LXPoint> points = new ArrayList<>();

        public Sparkle(LXVector center, double radius, double hue, double hueVar, double sat, double density, double attackSec, double decaySec) {
            this.center = center;
            this.radius = radius;
            this.hue = hue;
            this.hueVar = hueVar;
            this.sat = sat;
            this.density = Math.max(0, density);
            this.attackSec = attackSec;
            this.decaySec = decaySec;
        }

        public void advance(double deltaSec, double intensity, boolean sustain) {
            isSustaining = sustain;

            if (model != null) {
                leftoverSec += deltaSec;
                float secPerSpark = 1 / (float) ((density + .01) * (radius * 5));
                if (sustain) {
                    while (leftoverSec > secPerSpark) {
                        leftoverSec -= secPerSpark;
                        sparks.add(new Spark(MarkUtils.randomElement(points)));
                    }
                }

                Iterator<Spark> i = sparks.iterator();
                while (i.hasNext()) {
                    Spark spark = i.next();
                    boolean dead = spark.age(deltaSec);
                    if (dead) {
                        i.remove();
                    }
                }
            }
        }

        public boolean isExpired() {
            return !isSustaining && sparks.isEmpty();
        }

        public void render(LXModel model, PolyBuffer buffer) {
            if (model != this.model) {
                this.model = model;
                points = MarkUtils.getAllPointsWithin(model, center, radius);
            }

            long[] colors = (long[]) buffer.getArray(RGB16);
            for (Spark spark : sparks) {
                MarkUtils.addColor(colors, spark.point.index, Ops16.hsb(hue + hueVar * spark.hue, sat, spark.value));
            }
            buffer.markModified(RGB16);
        }

        class Spark {
            LXPoint point;
            float value;
            float hue;
            boolean hasPeaked;

            Spark(LXPoint point) {
                this.point = point;
                hue = (float) LXUtils.random(-1, 1);
                boolean infiniteAttack = (attackSec < 0.01);
                hasPeaked = infiniteAttack;
                value = (infiniteAttack ? 1 : 0);
            }

            // returns TRUE if this should die
            boolean age(double sec) {
                if (!hasPeaked) {
                    value += sec / attackSec;
                    if (value >= 1.0) {
                        value = (float)1.0;
                        hasPeaked = true;
                    }
                    return false;
                } else {
                    value -= sec / decaySec;
                    return value <= 0;
                }
            }
        }
    }
}
