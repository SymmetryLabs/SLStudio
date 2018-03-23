package com.symmetrylabs.util;

import heronarts.lx.LX;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;

import java.util.List;

public class GetIndices {
    public static int[] getIndices(LXFixture fixture) {
        List<LXPoint> points = fixture.getPoints();
        int numpoints = points.size();
        int[] indices = new int[numpoints];

        for (int i = 0; i < numpoints; i++) {
            indices[i] = points.get(i).index;
        }
        return indices;
    }
    public static int[] getIndices(LX lx) {
        List<LXPoint> points = lx.model.getPoints();
        int numpoints = points.size();
        int[] indices = new int[numpoints];

        for (int i = 0; i < numpoints; i++) {
            indices[i] = points.get(i).index;
        }
        return indices;
    }
}
