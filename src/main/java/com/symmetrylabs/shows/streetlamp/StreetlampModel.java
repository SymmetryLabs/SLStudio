package com.symmetrylabs.shows.streetlamp;

import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.List;

public class StreetlampModel extends SLModel {
    static final int ELEMENT_X_SIZE = 4;
    static final int ELEMENT_Y_DELTA = 3;
    static final int ELEMENT_Z_SIZE = 4;

    private StreetlampModel(List<LXPoint> points) {
        super(points);
    }

    private static LXPoint P(int x, int y, int z) {
        return new LXPoint(ELEMENT_X_SIZE * x, ELEMENT_Y_DELTA * y, ELEMENT_Z_SIZE * z);
    }

    public static StreetlampModel create() {
        List<LXPoint> points = new ArrayList<>();
        points.add(P(4, 0, 0));
        points.add(P(3, 1, 0));
        points.add(P(2, 2, 0));
        points.add(P(1, 3, 0));
        points.add(P(0, 4, 0));
        points.add(P(0, 3, 1));
        points.add(P(1, 2, 1));
        points.add(P(2, 1, 1));
        points.add(P(3, 0, 1));
        points.add(P(2, 0, 2));
        points.add(P(1, 1, 2));
        points.add(P(0, 2, 2));
        points.add(P(0, 1, 3));
        points.add(P(1, 0, 3));
        points.add(P(0, 0, 4));
        points.add(P(1, 0, 5));
        points.add(P(2, 1, 5));
        points.add(P(3, 2, 5));
        points.add(P(4, 3, 5));
        points.add(P(5, 4, 5));
        points.add(P(5, 3, 4));
        points.add(P(4, 2, 4));
        points.add(P(3, 1, 4));
        points.add(P(2, 0, 4));
        points.add(P(3, 0, 3));
        points.add(P(4, 1, 3));
        points.add(P(5, 2, 3));
        points.add(P(5, 1, 2));
        points.add(P(4, 0, 2));
        points.add(P(5, 0, 1));

        return new StreetlampModel(points);
    }
}
