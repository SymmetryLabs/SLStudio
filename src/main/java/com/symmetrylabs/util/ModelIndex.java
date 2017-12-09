package com.symmetrylabs.util;

import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;

import java.util.List;

public abstract class ModelIndex {
    protected final LXFixture fixture;

    public ModelIndex(LXFixture fixture) {
        this.fixture = fixture;
    }

    protected float pointDistance(LXPoint a, LXPoint b) {
        float x_diff = a.x - b.x;
        float y_diff = a.y - b.y;
        float z_diff = a.z - b.z;
        return (float) Math.sqrt(x_diff * x_diff + y_diff * y_diff + z_diff * z_diff);
    }

    public abstract List<LXPoint> pointsWithin(LXPoint target, float d);

    public abstract LXPoint nearestPoint(LXPoint target);
}
