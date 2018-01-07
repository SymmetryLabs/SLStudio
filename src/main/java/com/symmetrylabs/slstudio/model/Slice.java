package com.symmetrylabs.slstudio.model;

import com.symmetrylabs.slstudio.mappings.MappingGroup;
import com.symmetrylabs.slstudio.mappings.StripMapping;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.transform.LXTransform;
import org.apache.commons.math3.util.FastMath;

import java.util.*;

import static com.symmetrylabs.util.DistanceConstants.*;
import static com.symmetrylabs.util.MathConstants.*;


public class Slice extends StripsModel<CurvedStrip> {

    public enum Type {
        FULL, TWO_THIRDS, BOTTOM_ONE_THIRD
    }

    public static final int MAX_NUM_STRIPS_PER_SLICE = 69;
    public static final float STRIP_SPACING = 0.7f;
    public final static float DIAMETER = 8 * FEET;
    public final static float RADIUS = DIAMETER / 2;

    public final static int   LEDS_PER_METER = 60;
    public final static float PIXEL_PITCH = INCHES_PER_METER / LEDS_PER_METER;

    public String sunId;
    public final String id;
    public final Type type;
    private final Map<String, CurvedStrip> stripMap;

    public Slice(MappingGroup mappingGroup, String sunId, String id, Type type, float[] coordinates,
                                 float[] rotations, LXTransform transform) {

        super(new Fixture(mappingGroup, sunId, id, type, coordinates, rotations, transform));

        Fixture fixture = (Fixture) this.fixtures.get(0);

        this.sunId = sunId;
        this.id = id;
        this.type = type;

        this.strips.addAll(fixture.strips);
        this.stripMap = new HashMap<>();

        for (CurvedStrip strip : strips) {
            stripMap.put(strip.id, strip);
        }
    }

        public String getId() {
                return id;
        }

    // These are different than sun and slice ids, which are unique. These ids are all the same slice to slice.
    public CurvedStrip getStripById(String id) {
        return stripMap.get(id);
    }

    private static class Fixture extends LXAbstractFixture {

        private final MappingGroup mappingGroup;
        private final String sunId;
        private List<CurvedStrip> strips = new ArrayList<>();

        private Fixture(MappingGroup mapppings, String sunId, String id, Slice.Type type, float[] coordinates,
                                                float[] rotations, LXTransform transform) {

            this.mappingGroup = mapppings;
            this.sunId = sunId;
            transform.push();
            transform.translate(coordinates[0], coordinates[1], coordinates[2]);
            transform.rotateX(rotations[0] * DEG_TO_RAD);
            transform.rotateY(rotations[1] * DEG_TO_RAD);
            transform.rotateZ(rotations[2] * DEG_TO_RAD);

            // create curved strips...
            int start = 0;
            int numStripsInSlice = MAX_NUM_STRIPS_PER_SLICE;
            int stop = numStripsInSlice;

            if (type == Slice.Type.TWO_THIRDS) {
                stop = 45;
            } else if (type == Slice.Type.BOTTOM_ONE_THIRD) {
                start = 45;
                numStripsInSlice = MAX_NUM_STRIPS_PER_SLICE - 2;
                stop = numStripsInSlice;
            }

            for (int i = start; i < stop; i++) {
                addStrip(i, numStripsInSlice, transform, id);
            }

            for (CurvedStrip strip : strips) {
                this.points.addAll(strip.getPoints());
            }

            transform.pop();
        }

        private void addStrip(int i, int numStripsInSlice, LXTransform transform, String sliceId) {
            StripMapping stripMapping = mappingGroup.getItemByIndex(i, StripMapping.class);
            float stripY = calculateStripY(i, numStripsInSlice);
            if (FastMath.abs(stripY) >= Slice.RADIUS && stripMapping.numPoints > 0) {
                throw new RuntimeException("Error: trying to place strip " + i + " off sun " + sliceId);
            }
            float stripX = calculateStripX(stripY);
            float arcWidth = calculateArcWidth(stripX);
            float stripXFromCenter = stripX - Slice.RADIUS;

            CurvedStrip.CurvedMetrics metrics = new CurvedStrip.CurvedMetrics(arcWidth, stripMapping.numPoints);
            strips.add(new CurvedStrip(
                                stripMapping,
                Integer.toString(i + 1),
                metrics,
                new float[]{stripXFromCenter, stripY, 0},
                new float[]{0, 0, 0},
                transform,
                sunId,
                sliceId
            ));
        }
    }

    public static float calculateStripY(int i, int numStripsInSlice) {
        return STRIP_SPACING / 2f + (numStripsInSlice - i - 1) * STRIP_SPACING;
    }

    public static float calculateStripX(float stripY) {
        return Slice.RADIUS - (float) FastMath.sqrt(Slice.RADIUS * Slice.RADIUS - stripY * stripY);
    }

    public static float calculateArcWidth(float stripX) {
        return 2 * FastMath.abs(stripX - Slice.RADIUS);
    }
}
