package com.symmetrylabs.util;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.function.Predicate;
import java.util.function.Supplier;

import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXFixture;

public class LinearModelIndex extends ModelIndex {
    private boolean flattenZ;

    public LinearModelIndex(LXFixture fixture, boolean flattenZ) {
        super(fixture);

        this.flattenZ = flattenZ;
    }

    @Override
    public List<LXPoint> pointsWithin(final LXPoint target, final float d) {
        return fixture.getPoints().parallelStream().filter(new Predicate<LXPoint>() {
            public boolean test(LXPoint p) {
                return pointDistance(target, p) < d;
            }
        }).collect(Collectors.toCollection(new Supplier<List<LXPoint>>() {
            public List<LXPoint> get() {
                return new ArrayList<>();
            }
        }));
    }

    @Override
    public LXPoint nearestPoint(LXPoint target) {
        float nearestDist = 0;
        LXPoint nearestPoint = null;
        for (LXPoint p : fixture.getPoints()) {
            float d = pointDistance(target, p);
            if (nearestPoint == null || d < nearestDist) {
                nearestPoint = p;
                nearestDist = d;
            }
        }
        return nearestPoint;
    }

    protected float pointDistance(LXPoint a, LXPoint b) {
        float x_diff = a.x - b.x;
        float y_diff = a.y - b.y;
        float z_diff = flattenZ ? 0 : a.z - b.z;
        return (float)Math.sqrt(x_diff * x_diff + y_diff * y_diff + z_diff * z_diff);
    }
}
