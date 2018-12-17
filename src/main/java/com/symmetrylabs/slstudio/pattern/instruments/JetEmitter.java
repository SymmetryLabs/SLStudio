package com.symmetrylabs.slstudio.pattern.instruments;

import com.symmetrylabs.color.Ops16;

import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import heronarts.lx.transform.LXVector;

import static heronarts.lx.PolyBuffer.Space.RGB16;

public class JetEmitter extends EmitterInstrument.AbstractEmitter implements EmitterInstrument.Emitter {
    @Override
    public Jet emit(Instrument.ParameterSet paramSet, int pitch, double intensity) {
        return new Jet(
            paramSet.getPosition(randomXyDisc()),
            paramSet.getSize(intensity),
            paramSet.getColor(randomVariation()),
            paramSet.getVelocity(),
            paramSet.getRate(),
            1
        );
    }

    class Jet implements EmitterInstrument.Mark {
        public static final int NUM_STOPS = 100;

        public LXVector center;
        public double radius;
        public long color;
        public LXVector velocity;
        public double rate;
        public double lifetime;
        public double brightness;
        public long[] stream = new long[NUM_STOPS];
        public LXVector[] points = new LXVector[NUM_STOPS];

        private double accumSec;

        public Jet(LXVector center, double radius, long color, LXVector velocity, double rate, double lifetime) {
            this.center = center;
            this.radius = radius;
            this.color = color;
            this.rate = rate;
            this.lifetime = lifetime;
            brightness = 1.0;

            LXVector p = new LXVector(center);
            LXVector dp = new LXVector(velocity).mult(10 / (float) rate);
            for (int i = 0; i < points.length; i++) {
                p.add(dp);
                points[i] = new LXVector(p);
            }
        }

        public void advance(double deltaSec, double intensity, boolean sustain) {
            double periodSec = (1 / rate) / 100;
            accumSec += deltaSec;
            while (accumSec > periodSec) {
                for (int i = stream.length - 1; i > 0; i--) {
                    stream[i] = stream[i - 1];
                }
                stream[0] = Ops16.multiply(color, intensity);
                accumSec -= periodSec;
            }
            if (!sustain) {
                brightness *= Math.pow(0.01, deltaSec/lifetime);
            }
        }

        public boolean isExpired() {
            return brightness < 0.01;
        }

        public void render(LXModel model, PolyBuffer buffer) {
            long[] colors = (long[]) buffer.getArray(RGB16);
            LXVector p = new LXVector(0, 0, 0);
            for (int i = 0; i < model.points.length; i++) {
                p.x = model.points[i].x;
                p.y = model.points[i].y;
                p.z = model.points[i].z;
                for (int j = 0; j < points.length; j++) {
                    double dist = points[j].dist(p) / ((1 + (float) j/points.length) * radius);
                    if (dist < 1) {
                        colors[i] = Ops16.add(colors[i], color, (1 - dist*dist)*brightness);
                    }
                }
            }
            buffer.markModified(RGB16);
        }
    }
}
