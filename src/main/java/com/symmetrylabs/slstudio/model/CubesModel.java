package com.symmetrylabs.slstudio.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.SimplePixliteConfigs;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.transform.LXTransform;

import static com.symmetrylabs.slstudio.util.MathUtils.*;
import static com.symmetrylabs.util.DistanceConstants.*;
import static com.symmetrylabs.util.MathConstants.*;

/**
 * Top-level model of the entire sculpture. This contains a list of
 * every cube on the sculpture, which forms a hierarchy of faces, strips
 * and points.
 */
public class CubesModel extends StripsModel<CubesModel.CubesStrip> {
    protected final List<Tower> towers = new ArrayList<>();
    protected final List<Cube> cubes = new ArrayList<>();
    protected final List<Face> faces = new ArrayList<>();
    protected final Map<String, Cube> cubeTable = new HashMap<>();

    public final List<LocatedForm> bars = new ArrayList<>();

    private final List<Tower> towersUnmodifiable = Collections.unmodifiableList(towers);
    private final List<Cube> cubesUnmodifiable = Collections.unmodifiableList(cubes);
    private final List<Face> facesUnmodifiable = Collections.unmodifiableList(faces);

    private final List<LXFixture> barsUnmodifiable = Collections.unmodifiableList(bars);

    private final Cube[] _cubes;

    public CubesModel() {
        this(new ArrayList<>(), new Cube[0], null);
    }

    public CubesModel(List<Tower> towers, Cube[] cubeArr, LXFixture[] dynamicAllBars) {
        super(new Fixture(cubeArr, dynamicAllBars));
        Fixture fixture = (Fixture) this.fixtures.get(0);

        _cubes = cubeArr;

        for (Tower tower : towers) {
            this.towers.add(tower);

            for (Cube cube : tower.getCubes()) {
                if (cube != null) {
                    this.cubeTable.put(cube.id, cube);
                    this.cubes.add(cube);
                    this.faces.addAll(cube.getFaces());
                    this.strips.addAll(cube.getStrips());
                }
            }

            for(LXFixture bar : dynamicAllBars){
                if (bar instanceof LocatedForm){
                    this.bars.add((LocatedForm) bar);
                }
            }
        }
    }


    public List<Tower> getTowers() {
        return towersUnmodifiable;
    }

    public List<Cube> getCubes() {
        return cubesUnmodifiable;
    }

    public List<Face> getFaces() {
        return facesUnmodifiable;
    }

    private static class Fixture extends LXAbstractFixture {
        private Fixture(Cube[] cubeArr, LXFixture[] bars) {
            for (Cube cube : cubeArr) {
                if (cube != null) {
                    for (LXPoint point : cube.points) {
                        this.points.add(point);
                    }
                }
            }
            for (LXFixture bar : bars){
                for (LXPoint point : bar.getPoints()){
                    this.points.add(point);
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
    public Cube getCubeByRawIndex(int index) {
        return _cubes[index];
    }

    public Cube getCubeById(String id) {
        return this.cubeTable.get(id);
    }


        /**
             * Model of a set of cubes stacked in a tower
             */
    public static class Tower extends StripsModel<CubesStrip> {

        /**
         * Tower id
         */
        public final String id;

        protected final List<Cube> cubes = new ArrayList<>();
        protected final List<Face> faces = new ArrayList<>();

        private final List<Cube> cubesUnmodifiable = Collections.unmodifiableList(cubes);
        private final List<Face> facesUnmodifiable = Collections.unmodifiableList(faces);

        /**
         * Constructs a tower model from these cubes
         *
         * @param cubes Array of cubes
         */
        public Tower(String id, List<Cube> cubes) {
            super(cubes.toArray(new Cube[0]));

            this.id = id;

            for (Cube cube : cubes) {
                this.cubes.add(cube);
                this.faces.addAll(cube.getFaces());
                this.strips.addAll(cube.getStrips());
            }
        }

        public List<Cube> getCubes() {
            return cubesUnmodifiable;
        }

        public List<Face> getFaces() {
            return facesUnmodifiable;
        }
    }

    /**
     * Model of a single cube, which has an orientation and position on the
     * car. The position is specified in x,y,z coordinates with rotation. The
     * x axis is left->right, y is bottom->top, and z is front->back.
     *
     * A cube's x,y,z position is specified as the left, bottom, front corner.
     *
     * Dimensions are all specified in real-world inches.
     */
    public static class Cube extends StripsModel<CubesStrip> {

        public enum Type {

            //            Edge     |  LEDs   |  LEDs
            //            Length   |  Per    |  Per
            //            Inches   |  Meter  |  Edge
            SMALL         (12,        72,       15),
            MEDIUM        (18,        60,       23),
            LARGE         (24,        30,       15),
            LARGE_DOUBLE  (24,        60,       30);


            public final float EDGE_WIDTH;
            public final float EDGE_HEIGHT;

            public final int POINTS_PER_STRIP;
            public final int POINTS_PER_CUBE;
            public final int POINTS_PER_FACE;

            public final int LEDS_PER_METER;

            public final Face.Metrics FACE_METRICS;

            Type(float edgeLength, int ledsPerMeter, int ledsPerStrip) {
                this.EDGE_WIDTH = this.EDGE_HEIGHT = edgeLength;

                this.POINTS_PER_STRIP = ledsPerStrip;
                this.POINTS_PER_CUBE = STRIPS_PER_CUBE*POINTS_PER_STRIP;
                this.POINTS_PER_FACE = Face.STRIPS_PER_FACE*POINTS_PER_STRIP;

                this.LEDS_PER_METER = ledsPerMeter;

                this.FACE_METRICS = new Face.Metrics(
                    new CubesStrip.Metrics(this.EDGE_WIDTH, POINTS_PER_STRIP, ledsPerMeter),
                    new CubesStrip.Metrics(this.EDGE_HEIGHT, POINTS_PER_STRIP, ledsPerMeter)
                );
            }

        };

        public static final Type CUBE_TYPE_WITH_MOST_PIXELS = Type.LARGE_DOUBLE;

        public final static int FACES_PER_CUBE = 4;

        public final static int STRIPS_PER_CUBE = FACES_PER_CUBE * Face.STRIPS_PER_FACE;

        public final static float CHANNEL_WIDTH = 1.5f;

        public final Type type;

        public final String id;

        protected final List<Face> faces = new ArrayList<>();
        private final List<Face> facesUnmodifiable = Collections.unmodifiableList(faces);

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

        public Cube(String id, float x, float y, float z, float rx, float ry, float rz, LXTransform t, Type type) {
            super(new Fixture(x, y, z, rx, ry, rz, t, type));

            Fixture fixture = (Fixture) this.fixtures.get(0);

            this.type = type;
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

            this.faces.addAll(fixture.faces);
            this.strips.addAll(fixture.strips);
        }

        public List<Face> getFaces() {
            return facesUnmodifiable;
        }

        private static class Fixture extends LXAbstractFixture {

            private final List<Face> faces = new ArrayList<>();
            private final List<CubesStrip> strips = new ArrayList<>();

            private Fixture(float x, float y, float z, float rx, float ry, float rz, LXTransform t, Cube.Type type) {
                // LXTransform t = new LXTransform();
                t.push();
                t.translate(x, y, z);
                t.translate(type.EDGE_WIDTH/2, type.EDGE_HEIGHT/2, type.EDGE_WIDTH/2);
                t.rotateX(rx * Math.PI / 180.);
                t.rotateY(ry * Math.PI / 180.);
                t.rotateZ(rz * Math.PI / 180.);
                t.translate(-type.EDGE_WIDTH/2, -type.EDGE_HEIGHT/2, -type.EDGE_WIDTH/2);

                for (int i = 0; i < FACES_PER_CUBE; i++) {
                    Face face = new Face(type.FACE_METRICS, t);
                    this.faces.add(face);
                    for (CubesStrip s : face.strips) {
                        this.strips.add(s);
                    }
                    for (LXPoint p : face.points) {
                        this.points.add(p);
                    }
                    t.translate(type.EDGE_WIDTH, 0, 0);
                    t.rotateY(HALF_PI);
                }
                t.pop();
            }
        }
    }

    /**
     * A face is a component of a cube. It is comprised of four strips forming
     * the lights on this side of a cube. A whole cube is formed by four faces.
     */
    public static class Face extends StripsModel<CubesStrip> {

        public final static int STRIPS_PER_FACE = 3;

        public static class Metrics {
            final CubesStrip.Metrics horizontal;
            final CubesStrip.Metrics vertical;

            public Metrics(CubesStrip.Metrics horizontal, CubesStrip.Metrics vertical) {
                this.horizontal = horizontal;
                this.vertical = vertical;
            }
        }

        public Face(Metrics metrics, LXTransform transform) {
            super(new Fixture(metrics, transform));

            Fixture fixture = (Fixture) this.fixtures.get(0);
            this.strips.addAll(fixture.strips);
        }

        private static class Fixture extends LXAbstractFixture {

            private final List<CubesStrip> strips = new ArrayList<>();

            private Fixture(Metrics metrics, LXTransform transform) {
                transform.push();
                for (int i = 0; i < STRIPS_PER_FACE; i++) {
                    boolean isHorizontal = (i % 2 == 0);
                    CubesStrip.Metrics stripMetrics = isHorizontal ? metrics.horizontal : metrics.vertical;
                    CubesStrip strip = new CubesStrip(i+"", stripMetrics, transform);
                    this.strips.add(strip);
                    transform.translate(isHorizontal ? metrics.horizontal.length : metrics.vertical.length, 0, 0);
                    transform.rotateZ(HALF_PI);
                    for (LXPoint p : strip.points) {
                        this.points.add(p);
                    }
                }
                transform.pop();
            }
        }
    }

    /**
     * A strip is a linear run of points along a single edge of one cube.
     */
    public static class CubesStrip extends Strip {
        public static class Metrics extends Strip.Metrics {

            public final float length;
            public final int ledsPerMeter;

            public final float POINT_SPACING;

            public Metrics(float length, int numPoints, int ledsPerMeter) {
                super(numPoints);

                this.length = length;
                this.ledsPerMeter = ledsPerMeter;
                this.POINT_SPACING = INCHES_PER_METER / ledsPerMeter;
            }

            public Metrics(int numPoints, float spacing) {
                super(numPoints);

                this.length = numPoints * spacing;
                this.ledsPerMeter = floor((INCHES_PER_METER / this.length) * numPoints);
                this.POINT_SPACING = spacing;
            }
        }

        public CubesStrip(String id, Metrics metrics, List<LXPoint> points) {
            super(id, metrics, points);
        }

        public CubesStrip(String id, Metrics metrics, LXTransform transform) {
            super(id, metrics, new Fixture(metrics, transform));
        }

        private static class Fixture extends LXAbstractFixture {
            private Fixture(Metrics metrics, LXTransform transform) {
                float offset = (metrics.length - (metrics.numPoints - 1) * metrics.POINT_SPACING) / 2f;

                transform.push();
                transform.translate(offset, -Cube.CHANNEL_WIDTH / 2f, 0);

                for (int i = 0; i < metrics.numPoints; i++) {
                    LXPoint point = new LXPoint(transform.x(), transform.y(), transform.z());
                    this.points.add(point);
                    transform.translate(metrics.POINT_SPACING, 0, 0);
                }

                transform.pop();
            }
        }
    }
}
