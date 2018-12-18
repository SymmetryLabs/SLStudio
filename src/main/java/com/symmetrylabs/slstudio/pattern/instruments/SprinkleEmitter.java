package com.symmetrylabs.slstudio.pattern.instruments;

import com.symmetrylabs.color.Ops16;

import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import heronarts.lx.transform.LXVector;

import static heronarts.lx.PolyBuffer.Space.RGB16;

public class SprinkleEmitter extends EmitterInstrument.AbstractEmitter implements EmitterInstrument.Emitter {
    @Override
    public Puff emit(Instrument.ParameterSet paramSet, int pitch, double intensity) {
        return new Puff(
            new LXVector(paramSet.getPoint(randomXyDisc())),
            paramSet.getSize(intensity),
            paramSet.getColor(randomVariation()),
            paramSet.getDecaySec()
        );
    }

    class Puff implements EmitterInstrument.Mark {
        public LXVector center;
        public double radius;
        public long color;
        public double lifetime;
        public double brightness;

        public Puff(LXVector center, double radius, long color, double lifetime) {
            this.center = center;
            this.radius = radius;
            this.color = color;
            this.lifetime = lifetime;
            brightness = 1.0;
            System.out.println("new puff: " + center);
        }

        public void advance(double deltaSec, double intensity, boolean sustain) {
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
                double dist = center.dist(p) / radius;
                if (dist < 1) {
                    colors[i] = Ops16.add(colors[i], color, (1 - dist * dist) * brightness);
                }
            }
            buffer.markModified(RGB16);
        }
    }
}
