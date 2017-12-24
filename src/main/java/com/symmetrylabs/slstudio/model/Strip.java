package com.symmetrylabs.slstudio.model;

import java.util.List;

import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXFixture;

import static com.symmetrylabs.slstudio.util.MathUtils.floor;


public class Strip extends LXModel {

    public static final float INCHES_PER_METER = 39.3701f;

    public final String id;
    public final Metrics metrics;

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
