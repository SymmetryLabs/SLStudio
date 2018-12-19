package com.symmetrylabs.slstudio.pattern.instruments;

import com.symmetrylabs.util.NoiseUtils;

import java.util.List;

import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;

import static heronarts.lx.PolyBuffer.Space.RGB16;

public class PuffEmitter implements Emitter {
    @Override
    public Puff emit(Instrument.ParameterSet paramSet, int pitch, double intensity) {
        return new Puff(
            new LXVector(paramSet.getPoint(pitch, MarkUtils.randomXyDisc())),
            paramSet.getSize(intensity),
            paramSet.getColor(MarkUtils.randomVariation()),
            paramSet.getTwist(),
            1/(1 + paramSet.getRate() * 4),
            paramSet.getDecaySec()
        );
    }

    class Puff extends AttackDecayMark {
        public LXVector center;
        public float radius;
        public long color;
        public float irregularity;

        protected LXModel model;
        protected List<LXPoint> points;

        protected double growSec;
        protected float scale;

        public Puff(LXVector center, double radius, long color, double irregularity, double growSec, double decaySec) {
            super(0, decaySec);

            this.center = center;
            this.radius = (float) radius;
            this.color = color;
            this.irregularity = (float) irregularity;
            this.growSec = growSec;
            this.scale = 0;
        }

        public void advance(double deltaSec, double intensity, boolean sustain) {
            super.advance(deltaSec, intensity, sustain);
            if (sustain) {
                scale += deltaSec / growSec;
                if (scale > 1) scale = 1;
            }
        }

        public void render(LXModel model, PolyBuffer buffer) {
            if (model != this.model) {
                this.model = model;
                points = MarkUtils.getAllPointsWithin(model, center, radius * 2);
            }

            long[] colors = (long[]) buffer.getArray(RGB16);
            float size = radius * scale;
            for (LXPoint p : points) {
                float dx = p.x - center.x;
                float dy = p.y - center.y;
                float dz = p.z - center.z;
                float dist = (float) Math.sqrt(dx*dx + dy*dy + dz*dz)/size;
                if (irregularity > 0) {
                    float noise = NoiseUtils.noise(center.x + dx / size, center.y + dy / size, center.z + dz / size);
                    dist += irregularity * (2 * noise - 1);
                }
                if (dist < 1) {
                    MarkUtils.addColor(colors, p.index, color, amplitude);
                }
            }
            buffer.markModified(RGB16);
        }
    }
}
