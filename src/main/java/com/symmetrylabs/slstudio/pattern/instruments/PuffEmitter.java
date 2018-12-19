package com.symmetrylabs.slstudio.pattern.instruments;

import java.util.ArrayList;
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
            //new LXVector(paramSet.getPoint(pitch, MarkUtils.randomXyDisc())),
            paramSet.getPosition(pitch, MarkUtils.randomXyDisc()),
            paramSet.getSize(intensity),
            paramSet.getColor(MarkUtils.randomVariation()),
            paramSet.getDecaySec()
        );
    }

    class Puff extends AttackDecayMark {
        public LXVector center;
        public double radius;
        public long color;
        public int[] indexes;
        public double[] intensities;

        protected LXModel model;

        public Puff(LXVector center, double radius, long color, double decaySec) {
            super(0, decaySec);

            this.center = center;
            this.radius = radius;
            this.color = color;
        }

        public void render(LXModel model, PolyBuffer buffer) {
            if (model != this.model) {
                this.model = model;
                List<LXPoint> points = MarkUtils.getAllPointsWithin(model, center, radius);
                indexes = new int[points.size()];
                intensities = new double[points.size()];
                for (int i = 0; i < points.size(); i++) {
                    LXPoint p = points.get(i);
                    indexes[i] = p.index;
                    double dx = center.x - p.x;
                    double dy = center.y - p.y;
                    double dz = center.z - p.z;
                    intensities[i] = 1 - (dx*dx + dy*dy + dz*dz)/(radius*radius);
                }
            }

            long[] colors = (long[]) buffer.getArray(RGB16);
            for (int i = 0; i < indexes.length; i++) {
                MarkUtils.addColor(colors, indexes[i], color, intensities[i] * amplitude);
            }
            buffer.markModified(RGB16);
        }
    }
}
