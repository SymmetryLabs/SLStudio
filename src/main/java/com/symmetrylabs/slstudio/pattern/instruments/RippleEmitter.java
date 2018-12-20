package com.symmetrylabs.slstudio.pattern.instruments;

import com.symmetrylabs.util.NoiseUtils;

import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;

import static heronarts.lx.PolyBuffer.Space.RGB16;

public class RippleEmitter implements Emitter {
    @Override
    public Ripple emit(Instrument.ParameterSet paramSet, int pitch, double intensity) {
        double variation = 2 * intensity - 1;
        return new Ripple(
            new LXVector(paramSet.getPoint(pitch, MarkUtils.randomXyDisc())),
            paramSet.getSize(variation),
            paramSet.getColor(MarkUtils.randomVariation()),
            intensity,
            paramSet.getTwist(),
            1/(0.05 + paramSet.getRate(variation)/4),
            paramSet.getDecaySec()
        );
    }

    @Override public int getMaxCount() {
        return 40;
    }

    class Ripple extends AttackDecayMark {
        public LXVector center;
        public float width;
        public long color;
        public float intensity;
        public float irregularity;

        protected LXModel model;
        protected float modelRadius;
        protected float[] distances;

        protected double growSec;
        protected float radius;

        public Ripple(LXVector center, double width, long color, double intensity, double irregularity, double growSec, double decaySec) {
            super(0, decaySec);

            this.center = center;
            this.width = (float) width;
            this.color = color;
            this.intensity = (float) intensity;
            this.irregularity = (float) irregularity;
            this.growSec = growSec;
            this.radius = 0;
        }

        public void advance(double deltaSec, double intensity, boolean sustain) {
            super.advance(deltaSec, intensity, sustain);
            radius += modelRadius * deltaSec / growSec;
        }

        public void render(LXModel model, PolyBuffer buffer) {
            if (model != this.model) {
                this.model = model;
                modelRadius = (float) Math.hypot(Math.hypot(model.xRange, model.yRange), model.zRange)/2;
                distances = new float[model.points.length];
                int i = 0;
                for (LXPoint p : model.points) {
                    float dx = p.x - center.x;
                    float dy = p.y - center.y;
                    float dz = p.z - center.z;
                    distances[i++] = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
                }
            }

            long[] colors = (long[]) buffer.getArray(RGB16);
            int i = 0;
            for (LXPoint p : model.points) {
                float dx = p.x - center.x;
                float dy = p.y - center.y;
                float dz = p.z - center.z;
                float dist = distances[i++];
                if (irregularity > 0) {
                    float noise = NoiseUtils.noise(center.x + dx / radius, center.y + dy / radius, center.z + dz / radius);
                    dist += radius * irregularity * (2 * noise - 1);
                }
                float dr = (dist - radius)/width;
                float brt = 1 - dr * dr;
                if (brt > 0) {
                    MarkUtils.addColor(colors, p.index, color, brt * intensity * amplitude);
                }
            }
            buffer.markModified(RGB16);
        }
    }
}
