package com.symmetrylabs.util;

import com.harium.storage.kdtree.KDTree;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;

import java.util.Collections;
import java.util.List;

public class KDTreeModelIndex extends ModelIndex {
    private KDTree<LXPoint> kd;

    public KDTreeModelIndex(LXFixture fixture) {
        super(fixture);

        kd = new KDTree<LXPoint>(3);

        for (LXPoint point : fixture.getPoints()) {
            try {
                kd.insert(new double[]{point.x, point.y, point.z}, point);
            } catch (Exception e) {
                System.err.println("Exception while building KDTree: " + e.getMessage());
            }
        }
    }

    @Override
    public List<LXPoint> pointsWithin(LXVector target, float d) {
        List<LXPoint> nearby = null;

        try {
            nearby = kd.nearestEuclidean(new double[]{target.x, target.y, target.z}, d);
        } catch (Exception e) {
            System.err.println("Exception while finding nearest points: " + e.getMessage());
        }

        if (nearby == null)
            return Collections.emptyList();

        return nearby;
    }

    @Override
    public LXPoint nearestPoint(LXVector target) {
        try {
            return kd.nearest(new double[]{target.x, target.y, target.z});
        } catch (Exception e) {
            System.err.println("Exception while finding nearest point: " + e.getMessage());
        }

        return null;
    }
}
