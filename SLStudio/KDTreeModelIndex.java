package com.symmetrylabs.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXFixture;

import edu.wlu.cs.levy.CG.KDTree;

public class KDTreeModelIndex extends ModelIndex {
    private KDTree<LXPoint> kd;

    public KDTreeModelIndex(LXFixture fixture) {
        super(fixture);

        kd = new KDTree<LXPoint>(3);

        for (LXPoint point : fixture.getPoints()) {
            try {
                kd.insert(new double[] {point.x, point.y, point.z}, point);
            }
            catch (Exception e) {
                System.err.println("Exception while building KDTree: " + e.getMessage());
            }
        }
    }

    @Override
    public List<PointDist> pointsWithin(LXPoint target, float d) {
        List<PointDist> nearbyPoints = new ArrayList<PointDist>();

        List<LXPoint> nearby = null;

        try {
            nearby = kd.nearestEuclidean(new double[] {target.x, target.y, target.z}, d);
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
            nearest = kd.nearest(new double[] {target.x, target.y, target.z});
        }
        catch (Exception e) {
            System.err.println("Exception while finding nearest point: " + e.getMessage());
        }

        if (nearest == null)
            return null;

        return new PointDist(nearest, pointDistance(target, nearest));
    }
}
