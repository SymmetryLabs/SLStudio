package com.symmetrylabs.util;

import java.util.List;
import java.util.ArrayList;

import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXFixture;

public class LinearModelIndex extends ModelIndex {
    public LinearModelIndex(LXFixture fixture) {
        super(fixture);
    }

    @Override
    public List<LXPoint> pointsWithin(LXPoint target, float d) {
        List<LXPoint> nearbyPoints = new ArrayList<LXPoint>();
        for (LXPoint p : fixture.getPoints()) {
            float pd = pointDistance(target, p);
            if (pd <= d) {
                nearbyPoints.add(p);
            }
        }
        return nearbyPoints;
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
}
