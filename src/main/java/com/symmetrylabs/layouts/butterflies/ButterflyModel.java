package com.symmetrylabs.layouts.cubes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.transform.LXTransform;

import static com.symmetrylabs.util.MathUtils.*;
import static com.symmetrylabs.util.DistanceConstants.*;
import static com.symmetrylabs.util.MathConstants.*;

/**
 * Top-level model of the entire sculpture. This contains a list of
 * every cube on the sculpture, which forms a hierarchy of faces, strips
 * and points.
 */
public class ButterfliesModel extends StripsModel<ButterfliesModel.ButterfliesStrip> {
    protected final List<Butterfly> butterflies = new ArrayList<>();
    protected final List<Butterfly.Wing> wings = new ArrayList<>();
    protected final Map<String, Butterfly> butterflyTable = new HashMap<>();

    private final List<Butterfly> butterfliesUnmodifiable = Collections.unmodifiableList(butterflies);

    private final Butterfly[] _butterflies;

    public ButterfliesModel() {
        this(new ArrayList<>(), new Butterfly[0]);
    }

    public ButterfliesModel(List<Tower> towers, Butterfly[] butterflyArr) {
        super(new Fixture(cubeArr));
        Fixture fixture = (Fixture) this.fixtures.get(0);

        _butterflies = butterflyArr;

        for (Butterfly butterfly : butterflies) {
            if (butterfly != null) {
                this.butterflyTable.put(butterfly.id, cube);
                this.butterflies.add(butterfly);
                this.wings.addAll(butterfly.getWings());
            }
        }
    }

    public List<Butterfly> getWings() {
        return towersUnmodifiable;
    }

    public List<Butterfly.Wing> getCubes() {
        return cubesUnmodifiable;
    }

    private static class Fixture extends LXAbstractFixture {
        private Fixture(Butterfly[] butterflyArr) {
            for (ButterFly butterfly : butterflyArr) {
                if (butterfly != null) {
                    for (LXPoint point : butterfly.points) {
                        this.points.add(point);
                    }
                }
            }
        }
    }

    /**
     * TODO(mcslee): figure out better solution
     *
     * @param index
     * @return
     */
    public Butterfly getButterflyByRawIndex(int index) {
        return _butterflies[index];
    }

    public Butterfly getButterflyById(String id) {
        return this.butterflyTable.get(id);
    }

    public static class Butterfly extends StripsModel<ButterflyStrip> {

        public final String id;

        protected final List<Wing> wings = new ArrayList<>();
        private final List<Wing> wingsUnmodifiable = Collections.unmodifiableList(wings);

        /**
         * Front left corner x coordinate
         */
        public final float x;

        /**
         * Front left corner y coordinate
         */
        public final float y;

        /**
         * Front left corner z coordinate
         */
        public final float z;

        /**
         * Rotation about the x-axis
         */
        public final float rx;

        /**
         * Rotation about the y-axis
         */
        public final float ry;

        /**
         * Rotation about the z-axis
         */
        public final float rz;

        public Cube(String id, float x, float y, float z, float rx, float ry, float rz, LXTransform t) {
            super(new Fixture(x, y, z, rx, ry, rz, t));
            Fixture fixture = (Fixture)this.fixtures.get(0);
            this.id = id;

            while (rx < 0) rx += 360;
            while (ry < 0) ry += 360;
            while (rz < 0) rz += 360;
            rx = rx % 360;
            ry = ry % 360;
            rz = rz % 360;

            this.x = x;
            this.y = y;
            this.z = z;
            this.rx = rx;
            this.ry = ry;
            this.rz = rz;

            this.wings.addAll(fixture.wings);
        }

        public List<Wing> getWings() {
            return wingsUnmodifiable;
        }

        private static class Fixture extends LXAbstractFixture {

            private final List<Wing> wings = new ArrayList<>();

            private Fixture(float x, float y, float z, float rx, float ry, float rz, LXTransform t) {
                t.push();
                t.translate(x, y, z);
                //t.translate(type.EDGE_WIDTH/2, type.EDGE_HEIGHT/2, type.EDGE_WIDTH/2);
                t.rotateX(rx * Math.PI / 180.);
                t.rotateY(ry * Math.PI / 180.);
                t.rotateZ(rz * Math.PI / 180.);
                //t.translate(-type.EDGE_WIDTH/2, -type.EDGE_HEIGHT/2, -type.EDGE_WIDTH/2);

                this.wings.add(new Wing(0, 0, 0, 0,   0, 0, Wing.Type.UPPER));
                this.wings.add(new Wing(0, 0, 0, 0, 180, 0, Wing.Type.UPPER));
                this.wings.add(new Wing(0, -Wing.UPPER_WING_HEIGHT, 0, 0,   0, 0, Wing.Type.LOWER));
                this.wings.add(new Wing(0, -Wing.UPPER_WING_HEIGHT, 0, 0, 180, 0, Wing.Type.LOWER));
                t.pop();
            }
        }

        public static class Wing extends StripsModel<ButterflyStrip> {

            Type { UPPER, LOWER }

            public Type type;

            public final List<Strip> strips = new ArrayList<>();

            public Face(Type type, float[] coordinates, float[] rotations, LXTransform transform) {
                super(new Fixture(type, coordinates, rotations, transform));
                Fixture fixture = (Fixture) this.fixtures.get(0);
                this.strips.addAll(fixture.strips);
            }

            private static class Fixture extends LXAbstractFixture {

                private final List<Strip> strips = new ArrayList<>();

                private Fixture(Metrics metrics, LXTransform transform) {
                    transform.push();
                    // for (int i = 0; i < STRIPS_PER_FACE; i++) {
                    //   boolean isHorizontal = (i % 2 == 0);
                    //   CubesStrip.Metrics stripMetrics = isHorizontal ? metrics.horizontal : metrics.vertical;
                    //   CubesStrip strip = new CubesStrip(i+"", stripMetrics, transform);
                    //   this.strips.add(strip);
                    //   transform.translate(isHorizontal ? metrics.horizontal.length : metrics.vertical.length, 0, 0);
                    //   transform.rotateZ(HALF_PI);
                    //   for (LXPoint p : strip.points) {
                    //     this.points.add(p);
                    //   }
                    // }
                    transform.pop();
                }
            }
        }
    }

    public static class ButterflyStrip extends Strip {
        public static class Metrics extends Strip.Metrics {
            public final int LEDS_PER_METER = 30;
            public final float POINT_SPACING = INCHES_PER_METER / LEDS_PER_METER;
            public final float length;
            public final float arcWidth;

            public Metrics(int numPoints, float arcWidth) {
                super(numPoints);
                this.length = numPoints * POINT_SPACING;
                this.arcWidth = arcWidth;
            }
        }

        public ButterflyStrip(Metrics metrics, LXTransform transform) {
            super("", metrics, new Fixture(metrics, transform));
        }

        private static class Fixture extends LXAbstractFixture {
            private Fixture(Metrics metrics, LXTransform transform) {
                transform.push();

                for (int i = 0; i < metrics.numPoints; i++) {
                    transform.push();
                    float t = i / (float)metrics.numPoints;
                    float x = bezierPoint(0, metrics.arcWidth*0.2, metrics.arcWidth*0.8, metrics.arcWidth, t);
                    float y = bezierPoint(0, metrics.arcWidth*-0.3, metrics.arcWidth*-0.3, 0, t);
                    transform.translate(x, y, 0);
                    points.add(new LXPoint(transform.x(), transform.y(), transform.z()));
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
}
