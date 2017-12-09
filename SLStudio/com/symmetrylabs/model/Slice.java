package com.symmetrylabs.model;

import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static processing.core.PConstants.PI;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class Slice extends LXModel {

    public enum Type {
        FULL, TWO_THIRDS, BOTTOM_ONE_THIRD
    }

    ;

    private static final int MAX_NUM_STRIPS_PER_SLICE = 69;
    private static final float STRIP_SPACING = 0.7f;
    public final static float DIAMETER = 8 * FEET;
    public final static float RADIUS = DIAMETER / 2;

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
        int[] numPointsPerStrip
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
            int[] numPointsPerStrip
        ) {
            transform.push();
            transform.translate(coordinates[0], coordinates[1], coordinates[2]);
            transform.rotateX(rotations[0] * PI / 180);
            transform.rotateY(rotations[1] * PI / 180);
            transform.rotateZ(rotations[2] * PI / 180);

            int numStrips = numPointsPerStrip.length;

            // create curved strips...
            int counter = 0;
            if (type != Slice.Type.BOTTOM_ONE_THIRD) {
                for (int i = 0; i < MAX_NUM_STRIPS_PER_SLICE; i++) {
                    if (type == Slice.Type.TWO_THIRDS && i > 45) {
                        break;
                    }

                    int numPoints = numPointsPerStrip[counter++];
                    float stripWidth = numPoints * CurvedStrip.PIXEL_PITCH / 2.6f;
                    float stripX = (DIAMETER - stripWidth) / 2;

                    CurvedStrip.CurvedMetrics metrics = new CurvedStrip.CurvedMetrics(stripWidth, numPoints);
                    strips.add(new CurvedStrip(
                        Integer.toString(i + 1),
                        metrics,
                        new float[]{stripX, -i * STRIP_SPACING, 0},
                        new float[]{0, 0, 0},
                        transform
                    ));
                }
            } else {
                for (int i = 45; i < MAX_NUM_STRIPS_PER_SLICE - 2; i++) {
                    int numPoints = numPointsPerStrip[counter++];
                    float stripWidth = numPoints * CurvedStrip.PIXEL_PITCH / 2.6f;
                    float stripX = (DIAMETER - stripWidth) / 2;

                    CurvedStrip.CurvedMetrics metrics = new CurvedStrip.CurvedMetrics(stripWidth, numPoints);
                    strips.add(new CurvedStrip(
                        Integer.toString(i + 1),
                        metrics,
                        new float[]{stripX, -i * STRIP_SPACING, 0},
                        new float[]{0, 0, 0},
                        transform
                    ));
                }
            }

            for (Strip strip : strips) {
                for (LXPoint point : strip.points) {
                    this.points.add(point);
                }
            }

            transform.pop();
        }
    }
}
