package com.symmetrylabs.pixlites;

import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class PointsGrouping {
    public final static boolean REVERSE_ORDERING = true;

    public enum Shift {
        LEFT_TWICE, LEFT, RIGHT, RIGHT_TWICE
    }

    ;

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

    public PointsGrouping addPoints(LXPoint[] pointsToAdd) {
        for (LXPoint p : pointsToAdd) {
            this.points.add(p);
        }
        return this;
    }

    public PointsGrouping addPoints(LXPoint[] pointsToAdd, PointsGrouping.Shift shift) {
        LXPoint[] localPointsToAdd = pointsToAdd.clone();
        LXPoint[] shiftedPoints = null;

        if (shift == PointsGrouping.Shift.LEFT_TWICE) {
            shiftedPoints = new LXPoint[localPointsToAdd.length];

            for (int i = 0; i < shiftedPoints.length - 2; i++) {
                shiftedPoints[i] = localPointsToAdd[i + 2];
            }
            shiftedPoints[shiftedPoints.length - 2] = localPointsToAdd[shiftedPoints.length - 1];
            shiftedPoints[shiftedPoints.length - 1] = localPointsToAdd[shiftedPoints.length - 1];
        }
        if (shift == PointsGrouping.Shift.LEFT) {
            shiftedPoints = new LXPoint[localPointsToAdd.length];

            for (int i = 0; i < shiftedPoints.length - 1; i++) {
                shiftedPoints[i] = localPointsToAdd[i + 1];
            }
            shiftedPoints[shiftedPoints.length - 1] = localPointsToAdd[shiftedPoints.length - 1];
        }
        if (shift == PointsGrouping.Shift.RIGHT) {
            shiftedPoints = new LXPoint[localPointsToAdd.length];
            shiftedPoints[0] = localPointsToAdd[0];

            for (int i = 0; i < shiftedPoints.length - 1; i++) {
                shiftedPoints[i + 1] = localPointsToAdd[i];
            }
        }
        if (shift == PointsGrouping.Shift.RIGHT_TWICE) {
            shiftedPoints = new LXPoint[localPointsToAdd.length];
            shiftedPoints[0] = localPointsToAdd[0];
            shiftedPoints[1] = localPointsToAdd[0];

            for (int i = 0; i < shiftedPoints.length - 2; i++) {
                shiftedPoints[i + 2] = localPointsToAdd[i];
            }
        }

        addPoints(shiftedPoints);
        return this;
    }

    public PointsGrouping addPoints(LXPoint[] pointsToAdd, boolean reverseOrdering) {
        LXPoint[] localPointsToAdd = pointsToAdd.clone();

        if (reverseOrdering) {
            Collections.reverse(Arrays.asList(localPointsToAdd));
        }
        addPoints(localPointsToAdd);
        return this;
    }

    public PointsGrouping addPoints(LXPoint[] pointsToAdd, boolean reverseOrdering, PointsGrouping.Shift shift) {
        LXPoint[] localPointsToAdd = pointsToAdd.clone();
        LXPoint[] shiftedPoints = null;

        if (shift == PointsGrouping.Shift.RIGHT_TWICE) {
            shiftedPoints = new LXPoint[localPointsToAdd.length];

            for (int i = 0; i < shiftedPoints.length - 2; i++) {
                shiftedPoints[i] = localPointsToAdd[i + 2];
            }
            shiftedPoints[shiftedPoints.length - 2] = localPointsToAdd[shiftedPoints.length - 2];
            shiftedPoints[shiftedPoints.length - 1] = localPointsToAdd[shiftedPoints.length - 1];
        }
        if (shift == PointsGrouping.Shift.RIGHT) {
            shiftedPoints = new LXPoint[localPointsToAdd.length];

            for (int i = 0; i < shiftedPoints.length - 1; i++) {
                shiftedPoints[i] = localPointsToAdd[i + 1];
            }
            shiftedPoints[shiftedPoints.length - 1] = localPointsToAdd[shiftedPoints.length - 1];
        }
        if (shift == PointsGrouping.Shift.LEFT) {
            shiftedPoints = new LXPoint[localPointsToAdd.length];
            shiftedPoints[0] = localPointsToAdd[0];

            for (int i = 0; i < shiftedPoints.length - 1; i++) {
                shiftedPoints[i + 1] = localPointsToAdd[i];
            }
        }
        if (shift == PointsGrouping.Shift.LEFT_TWICE) {
            shiftedPoints = new LXPoint[localPointsToAdd.length];
            shiftedPoints[0] = localPointsToAdd[0];
            shiftedPoints[1] = localPointsToAdd[0];

            for (int i = 0; i < shiftedPoints.length - 2; i++) {
                shiftedPoints[i + 2] = localPointsToAdd[i];
            }
        }

        if (reverseOrdering) {
            Collections.reverse(Arrays.asList(shiftedPoints));
        }

        addPoints(shiftedPoints);
        return this;
    }
}
