package com.symmetrylabs.model;

import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

import java.util.List;

import static processing.core.PApplet.floor;

/**
 * A strip run of points
 */
public class Strip extends LXModel {

    public final String id;

    public static final float INCHES_PER_METER = 39.3701f;

    public static class Metrics {

        public final float length;
        public final int numPoints;
        public final int ledsPerMeter;

        public final float POINT_SPACING;

        public Metrics(float length, int numPoints, int ledsPerMeter) {
            this.length = length;
            this.numPoints = numPoints;
            this.ledsPerMeter = ledsPerMeter;
            this.POINT_SPACING = INCHES_PER_METER / ledsPerMeter;
        }

        public Metrics(int numPoints, float spacing) {
            this.length = numPoints * spacing;
            this.numPoints = numPoints;
            this.ledsPerMeter = (int) floor((INCHES_PER_METER / this.length) * numPoints);
            this.POINT_SPACING = spacing;
        }
    }

    public final Metrics metrics;

    /**
     * Whether this is a horizontal strip
     */
    public final boolean isHorizontal;

    /**
     * Rotation about the y axis
     */
    public final float ry;

    public Object obj1 = null, obj2 = null;

    Strip(String id, Metrics metrics, List<LXPoint> points) {
        super(points);
        this.id = id;
        this.isHorizontal = true;
        this.metrics = metrics;
        this.ry = 0;
    }
}
