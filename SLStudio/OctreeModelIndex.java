package com.symmetrylabs.util;

import java.util.List;
import java.util.ArrayList;

import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXModel;

public class OctreeModelIndex extends ModelIndex {
    private FixedWidthOctree<LXPoint> ot;

    public OctreeModelIndex(LXModel model) {
        this(model, false);
    }

    public OctreeModelIndex(LXModel model, boolean flattenZ) {
        super(model);

        ot = new FixedWidthOctree<LXPoint>(model.cx, model.cy, model.cz,
                (float)Math.max(model.xRange, Math.max(model.yRange, model.zRange)), 5);

        for (LXPoint point : model.getPoints()) {
            try {
                ot.insert(point.x, point.y, flattenZ ? 0 : point.z, point);
            }
            catch (Exception e) {
                System.err.println("Exception while building Octree: " + e.getMessage());
            }
        }
    }

    @Override
    public List<PointDist> pointsWithin(LXPoint target, float d) {
        List<PointDist> nearbyPoints = new ArrayList<PointDist>();

        List<LXPoint> nearby = null;

        try {
            nearby = ot.withinDistance((float)target.x, (float)target.y, (float)target.z, d);
        }
        catch (Exception e) {
            System.err.println("Exception while finding nearest points: " + e.getMessage());
        }

        if (nearby == null)
            return nearbyPoints;

        for (LXPoint p : nearby) {
            float pd = pointDistance(target, p);
            nearbyPoints.add(new PointDist(p, pd));
        }

        return nearbyPoints;
    }

    @Override
    public PointDist nearestPoint(LXPoint target) {
        LXPoint nearest = null;
        try {
            nearest = ot.nearest((float)target.x, (float)target.y, (float)target.z);
        }
        catch (Exception e) {
            System.err.println("Exception while finding nearest point: " + e.getMessage());
        }

        if (nearest == null)
            return null;

        return new PointDist(nearest, pointDistance(target, nearest));
    }
}
