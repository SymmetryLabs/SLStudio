package com.symmetrylabs.slstudio.pattern.instruments;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.slstudio.pattern.instruments.EmitterInstrument.AbstractMark;
import com.symmetrylabs.slstudio.pattern.instruments.EmitterInstrument.Mark;

import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import heronarts.lx.transform.LXVector;

import static com.symmetrylabs.slstudio.pattern.instruments.EmitterInstrument.*;
import static heronarts.lx.PolyBuffer.Space.RGB16;

public class SprinkleEmitter extends AbstractEmitter implements Emitter {
    @Override
    public Puff emit(Instrument.ParameterSet paramSet, int pitch, double intensity) {
        return new Puff(
            new LXVector(paramSet.getPoint(randomXyDisc())),
            paramSet.getSize(intensity),
            paramSet.getColor(randomVariation()),
            paramSet.getDecaySec()
        );
    }

    class Puff extends AbstractMark implements Mark {
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
        }

        public void advance(double deltaSec, double intensity, boolean sustain) {
            if (!sustain) {
                brightness *= Math.pow(0.01, deltaSec/lifetime);
            }
        }

        public boolean isExpired() {
            return brightness < 0.001;
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
