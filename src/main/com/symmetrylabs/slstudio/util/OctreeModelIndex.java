package com.symmetrylabs.slstudio.util;

import java.util.Collections;
import java.util.List;

import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;

public class OctreeModelIndex extends ModelIndex {
    private boolean flattenZ;
    private float centerZ;
    private FixedWidthOctree<LXPoint> ot;
    //private HashOctree<LXPoint> ot;

    public OctreeModelIndex(LXModel model) {
        this(model, false);
    }

    public OctreeModelIndex(LXModel model, boolean flattenZ) {
        super(model);

        this.flattenZ = flattenZ;
        this.centerZ = model.cz;

        ot = new FixedWidthOctree<LXPoint>(model.cx, model.cy, model.cz,
        //ot = new HashOctree<LXPoint>(model.cx, model.cy, model.cz,
            (float) Math.max(model.xRange, Math.max(model.yRange, model.zRange)), 3
        );

        for (LXPoint point : model.getPoints()) {
            try {
                ot.insert(point.x, point.y, flattenZ ? centerZ : point.z, point);
            }
            catch (Exception e) {
                System.err.println("Exception while building Octree: " + e.getMessage());
            }
        }

        //System.out.println(ot.dump());
    }

    @Override
    public List<LXPoint> pointsWithin(LXVector target, float d) {
        try {
            return ot.withinDistance(target.x, target.y, flattenZ ? centerZ : target.z, d);
        } catch (Exception e) {
            System.err.println("Exception while finding nearby points: " + e.getMessage());
        }

        return Collections.emptyList();
    }

    @Override
    public LXPoint nearestPoint(LXVector target) {
        try {
            return ot.nearest(target.x, target.y, flattenZ ? centerZ : target.z);
        } catch (Exception e) {
            System.err.println("Exception while finding nearest point: " + e.getMessage());
        }

        return null;
    }
}
