package com.symmetrylabs.slstudio.model;

import java.util.List;

import com.google.gson.annotations.Expose;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXFixture;
import heronarts.lx.transform.LXTransform;


public class Strip extends SLModel {

    public final String id;

    @Expose
    public final Metrics metrics;

    // for Lattice
    public Object obj1 = null, obj2 = null;

    public Strip(Metrics metrics, List<LXPoint> points) {
        this("untitled", metrics, points);
    }

    public Strip(String id, Metrics metrics, List<LXPoint> points) {
        super(points);

        this.id = id;
        this.metrics = metrics;
    }

    public Strip(String id, Metrics metrics, LXFixture fixture) {
        super(fixture);

        this.id = id;
        this.metrics = metrics;
    }

    public Strip(String id, Metrics metrics, LXTransform t) {
        super(new Fixture(metrics, t));

        this.id = id;
        this.metrics = metrics;
    }

    public static class Metrics {
        @Expose
        public final int numPoints;

        @Expose
        public final double pixelPitch;

        public Metrics(int numPoints) {
            // 1.0 default pitch i guess...
            this(numPoints, 1.0);
        }
        public Metrics(int numPoints, double pitch) {
            this.numPoints = numPoints;
            this.pixelPitch = pitch;
        }
    }

    private static class Fixture extends LXAbstractFixture {
        private Fixture(Metrics metrics, LXTransform t) {
            for (int i = 0; i < metrics.numPoints; i++) {
                t.push();
                t.translate((float)metrics.pixelPitch * i, 0, 0);
                points.add(new LXPoint(t.x(), t.y(), t.z()));
                t.pop();
            }
        }
    }
}
