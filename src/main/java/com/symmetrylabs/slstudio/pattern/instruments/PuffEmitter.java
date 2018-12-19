package com.symmetrylabs.slstudio.pattern.instruments;

import com.symmetrylabs.color.Ops16;

import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import heronarts.lx.transform.LXVector;

import static heronarts.lx.PolyBuffer.Space.RGB16;

public class PuffEmitter implements Emitter {
    @Override
    public Puff emit(Instrument.ParameterSet paramSet, int pitch, double intensity) {
        return new Puff(
            new LXVector(paramSet.getPoint(RandomUtils.randomXyDisc())),
            paramSet.getSize(intensity),
            paramSet.getColor(RandomUtils.randomVariation()),
            paramSet.getDecaySec()
        );
    }

    class Puff extends AttackDecayMark {
        public LXVector center;
        public double radius;
        public long color;
        public double lifetime;

        public Puff(LXVector center, double radius, long color, double decaySec) {
            super(0, decaySec);

            this.center = center;
            this.radius = radius;
            this.color = color;
        }

        public void render(LXModel model, PolyBuffer buffer) {
            long[] colors = (long[]) buffer.getArray(RGB16);
            LXVector p = new LXVector(0, 0, 0);
            for (int i = 0; i < model.points.length; i++) {
                p.x = model.points[i].x;
                p.y = model.points[i].y;
                p.z = model.points[i].z;
                if (Math.abs(p.x - center.x) < radius && Math.abs(p.y - center.y) < radius) {
                    double dist = center.dist(p) / radius;
                    if (dist < 1) {
                        colors[i] = Ops16.add(colors[i], color, (1 - dist * dist) * amplitude);
                    }
                }
            }
            buffer.markModified(RGB16);
        }
    }
}
