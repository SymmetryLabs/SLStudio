package com.symmetrylabs.slstudio.model;

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

        public final int numPoints;

        public Metrics(int numPoints) {
            this.numPoints = numPoints;
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
