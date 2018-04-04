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

    public PointsGrouping() {
        this("no-id");
    }

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
        this.points.addAll(pointsToAdd);
        return this;
    }

    public PointsGrouping addPoints(LXPoint[] pointsToAdd) {
        this.points.addAll(Arrays.asList(pointsToAdd));
        return this;
    }

    public PointsGrouping addPoints(List<LXPoint> pointsToAdd, boolean reverseOrdering) {
        List<LXPoint> adjustedPoints = new ArrayList<LXPoint>(pointsToAdd);
        if (reverseOrdering) {
            Collections.reverse(adjustedPoints);
        }
        addPoints(adjustedPoints);
        return this;
    }

    public PointsGrouping addPoint(LXPoint[] pointsToAdd, boolean reverseOrdering) {
        addPoints(Arrays.asList(pointsToAdd), reverseOrdering);
        return this;
    }
}
