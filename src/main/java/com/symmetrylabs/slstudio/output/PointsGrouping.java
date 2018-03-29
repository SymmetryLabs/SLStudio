package com.symmetrylabs.slstudio.output;

import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PointsGrouping {
    public final static boolean REVERSE_ORDERING = true;

    public String id;
    private final List<LXPoint> points = new ArrayList<LXPoint>();

    public PointsGrouping(String id) {
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

    public PointsGrouping reversePoints() {
        Collections.reverse(Arrays.asList(points));
        return this;
    }

    public PointsGrouping addPoints(List<LXPoint> pointsToAdd) {
        for (LXPoint p : pointsToAdd) {
            this.points.add(p);
        }
        return this;
    }

    public PointsGrouping addPoints(List<LXPoint> pointsToAdd, boolean reverseOrdering) {
        if (reverseOrdering) {
          pointsToAdd = new ArrayList<LXPoint>(pointsToAdd);
            Collections.reverse(pointsToAdd);
        }

        addPoints(pointsToAdd);
        return this;
    }

    public PointsGrouping addPoints(LXPoint[] pointsToAdd) {
        for (LXPoint p : pointsToAdd) {
            this.points.add(p);
        }
        return this;
    }

    public PointsGrouping addPoints(LXPoint[] pointsToAdd, boolean reverseOrdering) {
        if (reverseOrdering) {
            pointsToAdd = pointsToAdd.clone();
            Collections.reverse(pointsToAdd);
        }

        addPoints(pointsToAdd);
        return this;
    }
}
