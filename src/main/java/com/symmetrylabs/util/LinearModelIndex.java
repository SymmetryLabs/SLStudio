package com.symmetrylabs.util;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;

import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;

public class LinearModelIndex extends ModelIndex {
    private boolean flattenZ;

    public LinearModelIndex(LXFixture fixture) {
        this(fixture, false);
    }

    public LinearModelIndex(LXFixture fixture, boolean flattenZ) {
        super(fixture);

        this.flattenZ = flattenZ;
    }

    @Override
    public List<LXPoint> pointsWithin(LXVector target, float d) {
        float dSquared = d * d;
        return fixture.getPoints().parallelStream()
            .filter(p -> pointDistCheck(target, p, d, dSquared))
            .collect(Collectors.toList());
    }

    @Override
    public LXPoint nearestPoint(LXVector target) {
        float nearestSquaredDist = 0;
        LXPoint nearestPoint = null;
        for (LXPoint p : fixture.getPoints()) {
            float dSquared = squaredPointDistance(target, p);
            if (nearestPoint == null || dSquared < nearestSquaredDist) {
                nearestPoint = p;
                nearestSquaredDist = dSquared;
            }
        }
        return nearestPoint;
    }

    private boolean pointDistCheck(LXVector a, LXPoint b, float d, float dSquared) {
        float x_diff = a.x - b.x;
        float y_diff = a.y - b.y;
        float z_diff = flattenZ ? 0 : a.z - b.z;
        return FastMath.abs(x_diff) < d && FastMath.abs(y_diff) < d && FastMath.abs(z_diff) < d
            && (x_diff * x_diff + y_diff * y_diff + z_diff * z_diff) < dSquared;
    }

    private float squaredPointDistance(LXVector a, LXPoint b) {
        float x_diff = a.x - b.x;
        float y_diff = a.y - b.y;
        float z_diff = flattenZ ? 0 : a.z - b.z;
        return x_diff * x_diff + y_diff * y_diff + z_diff * z_diff;
    }
}
