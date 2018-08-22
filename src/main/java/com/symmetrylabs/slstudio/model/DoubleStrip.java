package com.symmetrylabs.slstudio.model;

import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.transform.LXTransform;

public class DoubleStrip extends Strip {
    public static class Metrics extends Strip.Metrics {

        public final double gap;
        public final double verticalOffset;

        public Metrics(int numPoints, double pixelPitch) {
            this(numPoints, pixelPitch, 0);
        }

        public Metrics(int numPoints, double pixelPitch, double gap) {
            this(numPoints, pixelPitch, gap, 0);
        }

        public Metrics(int numPoints, double pixelPitch, double gap, double verticalOffset) {
            super(numPoints, pixelPitch);

            this.gap = gap;
            this.verticalOffset = verticalOffset;
        }
    }

    public DoubleStrip(String id, Metrics metrics) {
        super(id, metrics, new Fixture(metrics, new LXTransform()));
    }

    public DoubleStrip(String id, Metrics metrics, LXTransform transform) {
        super(id, metrics, new Fixture(metrics, transform));
    }

    private static class Fixture extends LXAbstractFixture {
        private Fixture(Metrics metrics, LXTransform transform) {

            transform.push();
            transform.translate(0, 0, (float)-metrics.gap / 2);

            for (int i = 0; i < metrics.numPoints; i++) {
                transform.translate(0, (float)metrics.pixelPitch, 0);
                points.add(new LXPoint(transform.x(), transform.y(), transform.z()));
            }

            transform.translate(0, (float)metrics.verticalOffset, (float)metrics.gap / 2);

            for (int i = 0; i < metrics.numPoints; i++) {
                points.add(new LXPoint(transform.x(), transform.y(), transform.z()));
                transform.translate(0, (float)-metrics.pixelPitch, 0);
            }

            transform.pop();
        }
    }
}
