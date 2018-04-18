package com.symmetrylabs.layouts.dollywood;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import heronarts.lx.model.LXPoint;

public class ButterflyPointsGrouping {
    public final static boolean REVERSE_ORDERING = true;

    public String id;
    private final List<LXPoint> points = new ArrayList<LXPoint>();

    public ButterflyPointsGrouping(String id) {
        this.id = id;
    }

    public List<LXPoint> getPoints() {
        return points;
    }

    public LXPoint getPoint(int i) {
        return points.get(i);
    }

    public int size() {
        return points.size();
    }

    public int[] getIndices() {
        int[] indices = new int[points.size()];

        for (int i = 0; i < points.size(); i++) {
            indices[i] = points.get(i).index;
        }
        return indices;
    }

    public ButterflyPointsGrouping reversePoints() {
        Collections.reverse(Arrays.asList(points));
        return this;
    }

    public ButterflyPointsGrouping addPoints(LXPoint[] pointsToAdd) {
        for (LXPoint p : pointsToAdd) {
            this.points.add(p);
        }
        return this;
    }

    public ButterflyPointsGrouping addPoints(LXPoint[] pointsToAdd, boolean reverseOrdering) {
        LXPoint[] localPointsToAdd = pointsToAdd.clone();
        if (reverseOrdering) {
            Collections.reverse(Arrays.asList(localPointsToAdd));
        }
        addPoints(localPointsToAdd);
        return this;
    }
}