package com.symmetrylabs.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

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
    public List<LXPoint> pointsWithin(LXPoint target, float d) {
        try {
            return ot.withinDistance((float)target.x, (float)target.y, (float)target.z, d);
        }
        catch (Exception e) {
            System.err.println("Exception while finding nearest points: " + e.getMessage());
        }

        return Collections.emptyList();
    }

    @Override
    public LXPoint nearestPoint(LXPoint target) {
        try {
            return ot.nearest((float)target.x, (float)target.y, (float)target.z);
        }
        catch (Exception e) {
            System.err.println("Exception while finding nearest point: " + e.getMessage());
        }

        return null;
    }
}
