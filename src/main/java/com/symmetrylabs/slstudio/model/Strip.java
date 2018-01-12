package com.symmetrylabs.slstudio.model;

import java.util.List;

import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXFixture;


public class Strip extends SLModel {

    public final String id;
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
        public final int numPoints;

        public Metrics(int numPoints) {
            this.numPoints = numPoints;
        }
    }
}
