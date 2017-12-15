package com.symmetrylabs.slstudio.model;

import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;
import org.apache.commons.math3.util.FastMath;

import java.util.*;

import static com.symmetrylabs.slstudio.SLStudio.FEET;
import static processing.core.PConstants.PI;


public class Slice extends LXModel {

    public enum Type {
        FULL, TWO_THIRDS, BOTTOM_ONE_THIRD
    }

    ;

    public static final int MAX_NUM_STRIPS_PER_SLICE = 69;
    public static final float STRIP_SPACING = 0.7f;
    public final static float DIAMETER = 8 * FEET;
    public final static float RADIUS = DIAMETER / 2;

    public final static int   LEDS_PER_METER = 60;
    public final static float INCHES_PER_METER = 39.3701f;
    public final static float PIXEL_PITCH = INCHES_PER_METER / LEDS_PER_METER;

    public final String id;
    public final Type type;
    public final List<Strip> strips;
    private final Map<String, Strip> stripMap;

    public Slice(
        String id,
        Type type,
        float[] coordinates,
        float[] rotations,
        LXTransform transform,
        List<Double> numPointsPerStrip
    ) {
        super(new Fixture(id, type, coordinates, rotations, transform, numPointsPerStrip));
        Fixture fixture = (Fixture) this.fixtures.get(0);

        this.id = id;
        this.type = type;
        this.strips = Collections.unmodifiableList(fixture.strips);
        this.stripMap = new HashMap<String, Strip>();

        for (Strip strip : strips) {
            stripMap.put(strip.id, strip);
        }
    }

    // These are different than sun and slice ids, which are unique. These ids are all the same slice to slice.
    public Strip getStripById(String id) {
        return stripMap.get(id);
    }

    private static class Fixture extends LXAbstractFixture {

        private List<Strip> strips = new ArrayList<Strip>();

        private Fixture(
            String id,
            Slice.Type type,
            float[] coordinates,
            float[] rotations,
            LXTransform transform,
            List<Double> numPointsPerStrip
        ) {
            transform.push();
            transform.translate(coordinates[0], coordinates[1], coordinates[2]);
            transform.rotateX(rotations[0] * PI / 180);
            transform.rotateY(rotations[1] * PI / 180);
            transform.rotateZ(rotations[2] * PI / 180);

            // create curved strips...
            int counter = 0;
            if (type != Slice.Type.BOTTOM_ONE_THIRD) {
                for (int i = 0; i < MAX_NUM_STRIPS_PER_SLICE; i++) {
                    if (type == Slice.Type.TWO_THIRDS && i > 45) {
                        break;
                    }

                    int numPoints = numPointsPerStrip.get(i + 1).intValue();//[counter++];
                    addStrip(i, numPoints, transform, id);
                }
            } else {
                for (int i = 45; i < MAX_NUM_STRIPS_PER_SLICE - 2; i++) {
                    int numPoints = numPointsPerStrip.get(i + 1).intValue();//numPointsPerStrip[counter++];
                    addStrip(i, numPoints, transform, id);
                }
            }

            for (Strip strip : strips) {
                this.points.addAll(Arrays.asList(strip.points));
            }

            transform.pop();
        }

        private void addStrip(int i, int numPoints, LXTransform transform, String sliceId) {
            float stripY = Slice.RADIUS - (i + 1) * STRIP_SPACING;
            if (FastMath.abs(stripY) >= Slice.RADIUS) {
                throw new RuntimeException("Error: trying to place strip off sun: " + i);
            }
            float stripX = (float) -FastMath.sqrt(Slice.RADIUS * Slice.RADIUS - stripY * stripY);
            float arcWidth = 2 * FastMath.abs(stripX);

            CurvedStrip.CurvedMetrics metrics = new CurvedStrip.CurvedMetrics(arcWidth, numPoints);
            strips.add(new CurvedStrip(
                Integer.toString(i + 1),
                metrics,
                new float[]{stripX, stripY, 0},
                new float[]{0, 0, 0},
                transform,
                sliceId
            ));
        }
    }
}
