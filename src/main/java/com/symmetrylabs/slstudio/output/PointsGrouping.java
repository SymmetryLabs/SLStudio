package com.symmetrylabs.slstudio.output;

import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class PointsGrouping {
    public final static boolean REVERSE_ORDERING = true;

    public String id;
    public boolean grbSwap = false;  // false = RGB (normal), true = GRB (swap red/green)
    private final List<LXPoint> points = new ArrayList<LXPoint>();
    // Parallel list: true = real point (use p.index), false = black sentinel (use -1)
    private final List<Boolean> isReal = new ArrayList<Boolean>();

    // Per-strip segments for individual GRB control within shared universes
    public static class StripSegment {
        public final int startIndex;  // Inclusive
        public final int endIndex;    // Exclusive
        public final boolean grbSwap;
        public StripSegment(int startIndex, int endIndex, boolean grbSwap) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.grbSwap = grbSwap;
        }
    }
    private final List<StripSegment> stripSegments = new ArrayList<>();

    public void addStripSegment(int startIndex, int endIndex, boolean grbSwap) {
        stripSegments.add(new StripSegment(startIndex, endIndex, grbSwap));
    }

    public List<StripSegment> getStripSegments() {
        return stripSegments;
    }

    public PointsGrouping() {
        this("no-humanID");
    }

    public PointsGrouping(String id) {
        this.id = id;
    }

    public PointsGrouping(List<LXPoint> points) {
        this("no-humanID", points);
    }

    public PointsGrouping(LXPoint[] points) {
        this("ni-humanID", points);
    }

    public PointsGrouping(String id, List<LXPoint> points) {
        this.id = id;
        addPoints(points);
    }

    public PointsGrouping(String id, LXPoint[] points) {
        this.id = id;
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
        return isReal.size();
    }

    public int[] getIndices() {
        int[] indices = new int[isReal.size()];
        int pi = 0;
        for (int i = 0; i < isReal.size(); i++) {
            indices[i] = isReal.get(i) ? points.get(pi++).index : -1;
        }
        return indices;
    }

    public int[] getIndicesInRange(int fromIndex, int toIndex) {
        return Arrays.copyOfRange(getIndices(), fromIndex, toIndex);
    }

    public PointsGrouping reversePoints() {
        Collections.reverse(points);
        Collections.reverse(isReal);
        return this;
    }

    public PointsGrouping addPoints(List<LXPoint> pointsToAdd) {
        for (LXPoint p : pointsToAdd) { points.add(p); isReal.add(true); }
        return this;
    }

    public PointsGrouping addPoints(LXPoint[] pointsToAdd) {
        for (LXPoint p : pointsToAdd) { points.add(p); isReal.add(true); }
        return this;
    }

    public PointsGrouping addPoints(List<LXPoint> pointsToAdd, boolean reverseOrdering) {
        List<LXPoint> adjusted = new ArrayList<LXPoint>(pointsToAdd);
        if (reverseOrdering) Collections.reverse(adjusted);
        addPoints(adjusted);
        return this;
    }

    public PointsGrouping addPoints(LXPoint[] pointsToAdd, boolean reverseOrdering) {
        addPoints(Arrays.asList(pointsToAdd), reverseOrdering);
        return this;
    }

    public void addPoint(LXPoint point) {
        points.add(point);
        isReal.add(true);
    }

    /** Add n black (unmapped) padding slots with sentinel index -1. No LXPoint created. */
    public PointsGrouping addBlackPixels(int n) {
        for (int i = 0; i < n; i++) isReal.add(false);
        return this;
    }

}
