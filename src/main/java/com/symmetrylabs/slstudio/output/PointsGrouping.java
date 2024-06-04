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
        this("0");
    }

    public PointsGrouping(String id) {
        this.id = isValidInteger(id) ? id : "0";
    }

    public PointsGrouping(List<LXPoint> points) {
        this("0", points);
    }

    public PointsGrouping(LXPoint[] points) {
        this("0", points);
    }

    public PointsGrouping(String id, List<LXPoint> points) {
        this.id = isValidInteger(id) ? id : "0";
        addPoints(points);
    }

    public PointsGrouping(String id, LXPoint[] points) {
        this.id = isValidInteger(id) ? id : "0";
        addPoints(points);
    }


    public List<LXPoint> getPoints() {
        return points;
    }

    public LXPoint getPoint(int i) {
        return points.get(i);
    }

    public List<LXPoint> getPointsInRange(int fromIndex, int toIndex) {
        return points.subList(fromIndex, toIndex);
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

    public int[] getIndicesInRange(int fromIndex, int toIndex) {
        return Arrays.copyOfRange(getIndices(), fromIndex, toIndex);
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

    public PointsGrouping addPoints(LXPoint[] pointsToAdd, boolean reverseOrdering) {
        addPoints(Arrays.asList(pointsToAdd), reverseOrdering);
        return this;
    }

    public void addPoint(LXPoint point) {
        points.add(point);
    }
    private boolean isValidInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}


