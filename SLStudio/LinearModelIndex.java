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
    public List<PointDist> pointsWithin(LXPoint target, float d) {
        List<PointDist> nearbyPoints = new ArrayList<PointDist>();
        for (LXPoint p : fixture.getPoints()) {
            float pd = pointDistance(target, p);
            if (pd <= d) {
                nearbyPoints.add(new PointDist(p, pd));
            }
        }
        return nearbyPoints;
    }

    @Override
    public PointDist nearestPoint(LXPoint target) {
        float nearestDist = 0;
        LXPoint nearestPoint = null;
        for (LXPoint p : fixture.getPoints()) {
            float d = pointDistance(target, p);
            if (nearestPoint == null || d < nearestDist) {
                nearestPoint = p;
                nearestDist = d;
            }
        }
        return nearestPoint == null ? null : new PointDist(nearestPoint, nearestDist);
    }
}
