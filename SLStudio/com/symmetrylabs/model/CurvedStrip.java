package com.symmetrylabs.model;

import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.transform.LXTransform;

import static processing.core.PConstants.PI;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class CurvedStrip extends Strip {

    public static final int LEDS_PER_METER = 60;
    public static final float INCHES_PER_METER = 39.3701f;
    public static final float PIXEL_PITCH = LEDS_PER_METER / INCHES_PER_METER;

    private static int counter = 0;

    public static class CurvedMetrics {
        public final Strip.Metrics metrics;
        public final float pitch;
        public final int numPoints;
        public final float arcWidth;

        public CurvedMetrics(float arcWidth, int numPoints) {
            this.metrics = new Strip.Metrics(numPoints, PIXEL_PITCH);
            this.pitch = metrics.POINT_SPACING;
            this.numPoints = metrics.numPoints;
            this.arcWidth = arcWidth;
        }
    }

    public CurvedStrip(String id, CurvedMetrics metrics, float[] coordinates, float[] rotations, LXTransform transform) {
        super(id, metrics.metrics, new Fixture(id, metrics, coordinates, rotations, transform).getPoints());
    }

    private static class Fixture extends LXAbstractFixture {
        private Fixture(String id, CurvedMetrics metrics, float[] coordinates, float[] rotations, LXTransform transform) {
            transform.push();
            transform.translate(coordinates[0], coordinates[1], coordinates[2]);
            transform.rotateX(rotations[1] * PI / 180);
            transform.rotateY(rotations[2] * PI / 180);
            transform.rotateZ(rotations[0] * PI / 180);

            for (int i = 0; i < metrics.numPoints; i++) {
                transform.push();
                float t = i / (float) metrics.numPoints;
                float x = bezierPoint(0, metrics.arcWidth * 0.2f, metrics.arcWidth * 0.8f, metrics.arcWidth, t);
                float z = bezierPoint(0, metrics.arcWidth * -0.3f, metrics.arcWidth * -0.3f, 0, t);
                transform.translate(x, 0, z);

                points.add(new LXPointNormal(
                    transform.x(), transform.y(), transform.z()));
                transform.pop();
            }

            transform.pop();
        }

        private float bezierPoint(float a, float b, float c, float d, float t) {
            float t1 = 1.0f - t;
            return ((a * t1) + (3 * b * t)) * (t1 * t1) + ((3 * c * t1) + (d * t)) * (t * t);
        }
    }
}
