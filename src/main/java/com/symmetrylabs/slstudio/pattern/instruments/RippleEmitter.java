package com.symmetrylabs.slstudio.pattern.instruments;

import com.symmetrylabs.util.NoiseUtils;

import java.util.List;

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
            paramSet,
            new LXVector(paramSet.getPoint(pitch, MarkUtils.randomXyDisc())),
            paramSet.getSize(variation),
            paramSet.getColor(MarkUtils.randomVariation(), variation),
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
        public float irregularity;
        public float bend;

        protected Instrument.ParameterSet paramSet;
        protected LXModel model;
        protected float modelRadius;
        protected float[] distances;

        protected double growSec;
        protected float radius;

        public Ripple(Instrument.ParameterSet paramSet, LXVector center, double width, long color, double irregularity, double growSec, double decaySec) {
            super(0, decaySec);

            this.paramSet = paramSet;
            this.center = center;
            this.width = (float) width;
            this.color = color;
            this.irregularity = (float) irregularity;
            this.growSec = growSec;
            this.radius = 0;
        }

        public void advance(double deltaSec, double intensity, boolean sustain, double bend) {
            super.advance(deltaSec, intensity, sustain, bend);
            double variation = 2 * intensity - 1;
            width = (float) paramSet.getSize(variation);
            this.bend = (float) bend;
            radius += modelRadius * deltaSec / growSec;
        }

        public void render(LXModel model, List<LXVector> vectors, PolyBuffer buffer) {
            if (model != this.model) {
                this.model = model;
                modelRadius = (float) Math.hypot(Math.hypot(model.xRange, model.yRange), model.zRange)/2;
                distances = new float[vectors.size()];
                int i = 0;
                for (LXVector v : vectors) {
                    float dx = v.x - center.x;
                    float dy = v.y - center.y;
                    float dz = v.z - center.z;
                    distances[i++] = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
                }
            }

            long[] colors = (long[]) buffer.getArray(RGB16);
            int i = 0;
            for (LXVector v : vectors) {
                float dx = v.x - center.x;
                float dy = v.y - center.y;
                float dz = v.z - center.z;
                float dist = distances[i++];
                if (irregularity > 0) {
                    float irreg = irregularity * (bend + 1) / 2;
                    float noise = NoiseUtils.noise(center.x + dx / radius, center.y + dy / radius, center.z + dz / radius);
                    dist += radius * irreg * (2 * noise - 1);
                }
                float dr = (dist - radius)/width;
                float brt = 1 - dr * dr;
                if (brt > 0) {
                    MarkUtils.addColor(colors, v.index, color, brt * amplitude);
                }
            }
            buffer.markModified(RGB16);
        }
    }
}
