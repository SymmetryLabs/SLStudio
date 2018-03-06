package com.symmetrylabs.slstudio.model;

import java.util.List;

import com.google.gson.annotations.Expose;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXFixture;


public class Strip extends SLModel {

    public final String id;

    @Expose
    public final Metrics metrics;

    // for Lattice
    public Object obj1 = null, obj2 = null;

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
}
