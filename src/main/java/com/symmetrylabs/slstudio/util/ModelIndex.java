package com.symmetrylabs.slstudio.util;

import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;

import java.util.List;

public abstract class ModelIndex {
    protected final LXFixture fixture;

    public ModelIndex(LXFixture fixture) {
        this.fixture = fixture;
    }

    public List<LXPoint> pointsWithin(LXPoint target, float d) {
        return pointsWithin(new LXVector(target), d);
    }

    public LXPoint nearestPoint(LXPoint target) {
        return nearestPoint(new LXVector(target));
    }

    public abstract List<LXPoint> pointsWithin(LXVector target, float d);
    public abstract LXPoint nearestPoint(LXVector target);
}
