package com.symmetrylabs.shows.cubes;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.output.CubeModelControllerMapping;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.util.CubeInventory;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.symmetrylabs.slstudio.output.CubeModelControllerMapping.PhysIdAssignment;
import static com.symmetrylabs.util.CubeInventory.PhysicalCube;

import static com.symmetrylabs.util.MathUtils.*;
import static com.symmetrylabs.util.DistanceConstants.*;
import static com.symmetrylabs.util.MathConstants.*;

/**
 * Top-level model of the entire sculpture. This contains a set of
 * every cube on the sculpture, which forms a hierarchy of faces, strips
 * and points.
 */
public class CubesModel extends StripsModel<CubesModel.CubesStrip> {
    protected final List<Tower> towers = new ArrayList<>();
    protected final List<Cube> cubes = new ArrayList<>();
    protected final List<Face> faces = new ArrayList<>();

    private final List<Tower> towersUnmodifiable = Collections.unmodifiableList(towers);
    private final List<Cube> cubesUnmodifiable = Collections.unmodifiableList(cubes);
    private final List<Face> facesUnmodifiable = Collections.unmodifiableList(faces);

    private final Map<String, PointsGrouping> controllerPointCache = new HashMap<>();

    public final CubeModelControllerMapping mapping;
    public final CubeInventory inventory;

    private final Map<String, Cube> cubesByModelId = new HashMap<>();

    public CubesModel() {
        this(new ArrayList<>(), new Cube[0], null, null);
    }

    public CubesModel(List<Tower> towers, Cube[] cubeArr, CubeInventory inventory, CubeModelControllerMapping mapping) {
        super(new Fixture(cubeArr));
        Fixture fixture = (Fixture) this.fixtures.get(0);

        this.inventory = inventory;
        this.mapping = mapping;

        this.towers.addAll(towers);
        for (Cube cube : cubeArr) {
            if (cube != null) {
                this.cubes.add(cube);
                this.faces.addAll(cube.getFaces());
                this.strips.addAll(cube.getStrips());
            }
            cubesByModelId.put(cube.modelId, cube);
        }
    }

    @Override
    public LXModel update(boolean normalize, boolean recurse) {
        super.update(normalize, recurse);
        controllerPointCache.clear();
        return this;
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
        private Fixture(Cube[] cubeArr) {
            for (Cube cube : cubeArr) {
                if (cube != null) {
                    for (LXPoint point : cube.points) {
                        this.points.add(point);
                    }
                }
            }
        }
    }

    public Cube getCubeByModelId(String id) {
        return cubesByModelId.get(id);
    }

    public PointsGrouping getControllerPoints(String controllerId) {
        PointsGrouping cached = controllerPointCache.get(controllerId);
        if (cached != null) {
            return cached;
        }
        PhysIdAssignment ca = mapping.lookUpByControllerId(controllerId);
        if (ca == null) {
            return null;
        }
        Cube cube = cubesByModelId.get(ca.modelId);
        PointsGrouping res;
        if (cube instanceof DoubleControllerCube) {
            DoubleControllerCube dcc = (DoubleControllerCube) cube;
            PhysicalCube pc = inventory.cubeByCtrlId.get(ca.physicalId);
            res = controllerId.equals(pc.idB) ? dcc.getPointsB() : dcc.getPointsA();
        } else {
            res = new PointsGrouping(cube.getPoints());
        }
        controllerPointCache.put(controllerId, res);
        return res;
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

    public static class DoubleControllerCube extends Cube {
        public DoubleControllerCube(String modelId, float x, float y, float z, float rx, float ry, float rz, LXTransform t) {
            super(modelId, x, y, z, rx, ry, rz+180, t, CubesModel.Cube.Type.HD);
        }

        public PointsGrouping getPointsB() {
            return new PointsGrouping()
                .addPoints(getStrips().get(11).getPoints())
                .addPoints(getStrips().get(8).getPoints())
                .addPoints(getStrips().get(4).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(getStrips().get(6).getPoints())
                .addPoints(getStrips().get(7).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(getStrips().get(9).getPoints());
        }

        public PointsGrouping getPointsA() {
            return new PointsGrouping()
                .addPoints(getStrips().get(0).getPoints())
                .addPoints(getStrips().get(3).getPoints())
                .addPoints(getStrips().get(5).getPoints())
                .addPoints(getStrips().get(1).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(getStrips().get(2).getPoints())
                .addPoints(getStrips().get(10).getPoints(), PointsGrouping.REVERSE_ORDERING);
        }
    }

    /**
     * Model of a single cube, which has an orientation and position on the
     * car. The position is specified in x,y,z coordinates with rotation. The
     * x axis is left-to-right, y is bottom-to-top, and z is front-to-back.
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
            LARGE_DOUBLE  (24,        60,       30),
            HD            (24,        60,       28);


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
        public static final int MAX_PIXELS_PER_CONTROLLER = Type.LARGE.POINTS_PER_CUBE;

        public final static int FACES_PER_CUBE = 4;

        public final static int STRIPS_PER_CUBE = FACES_PER_CUBE * Face.STRIPS_PER_FACE;

        public final static float CHANNEL_WIDTH = 1.5f;

        public final Type type;

        public final String modelId;

        protected final List<Face> faces = new ArrayList<>();
        private final List<Face> facesUnmodifiable = Collections.unmodifiableList(faces);

        /**
         * Front left corner x coordinate
         */
        public float x;

        /**
         * Front left corner y coordinate
         */
        public float y;

        /**
         * Front left corner z coordinate
         */
        public float z;

        /**
         * Rotation about the x-axis
         */
        public float rx;

        /**
         * Rotation about the y-axis
         */
        public float ry;

        /**
         * Rotation about the z-axis
         */
        public float rz;

        public Cube(String modelId, float x, float y, float z, float rx, float ry, float rz, LXTransform t, Type type) {
            super(new Fixture(type));

            Fixture fixture = (Fixture) this.fixtures.get(0);

            this.type = type;
            this.modelId = modelId;

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
            updatePoints(t);
        }

        public List<Face> getFaces() {
            return facesUnmodifiable;
        }

        public void updatePoints(LXTransform t) {
            t.push();
            t.translate(x, y+type.EDGE_HEIGHT, z);
            t.translate(type.EDGE_WIDTH/2, type.EDGE_HEIGHT/2, type.EDGE_WIDTH/2);
            t.rotateX(rx * Math.PI / 180.);
            t.rotateY((ry * Math.PI / 180.) - HALF_PI);
            t.rotateZ(rz * Math.PI / 180.);
            t.translate(-type.EDGE_WIDTH/2, -type.EDGE_HEIGHT/2, -type.EDGE_WIDTH/2);

            for (int i = 0; i < FACES_PER_CUBE; i++) {
                faces.get(i).updatePoints(t);
                t.translate(type.EDGE_WIDTH, 0, 0);
                t.rotateY(-HALF_PI);
            }
            t.pop();
            update(true, true);
        }

        private static class Fixture extends LXAbstractFixture {

            private final List<Face> faces = new ArrayList<>();
            private final List<CubesStrip> strips = new ArrayList<>();

            private Fixture(Type type) {
                for (int i = 0; i < FACES_PER_CUBE; i++) {
                    Face face = new Face(type.FACE_METRICS);
                    faces.add(face);
                    strips.addAll(face.getStrips());
                    for (LXPoint p : face.points) {
                        points.add(p);
                    }
                }
            }
        }
    }

    /**
     * A Face is a component of a Cube, made of 3 strips in a "U" shape (3 of the 4
     * edges of a square).  A whole cube is formed by 4 of these Faces.
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

        private final Metrics metrics;

        public Face(Metrics metrics) {
            super(new Fixture(metrics));

            Fixture fixture = (Fixture) this.fixtures.get(0);
            strips.addAll(fixture.strips);
            this.metrics = metrics;
        }

        public void updatePoints(LXTransform t) {
            t.push();
            for (int i = 0; i < STRIPS_PER_FACE; i++) {
                CubesStrip strip = strips.get(i);
                strip.updatePoints(t);
                t.translate(i % 2 == 0 ? metrics.horizontal.length : metrics.vertical.length, 0, 0);
                t.rotateZ(-HALF_PI);
            }
            t.pop();
            update(true, true);
        }

        private static class Fixture extends LXAbstractFixture {

            private final List<CubesStrip> strips = new ArrayList<>();

            private Fixture(Metrics metrics) {
                for (int i = 0; i < STRIPS_PER_FACE; i++) {
                    boolean isHorizontal = (i % 2 == 0);
                    CubesStrip.Metrics stripMetrics = isHorizontal ? metrics.horizontal : metrics.vertical;
                    CubesStrip strip = new CubesStrip(i+"", stripMetrics, isHorizontal);
                    this.strips.add(strip);
                    for (LXPoint p : strip.points) {
                        this.points.add(p);
                    }
                }
            }
        }
    }

    /**
     * A strip is a linear run of points along a single edge of one cube.
     */
    public static class CubesStrip extends Strip {
        public boolean isHorizontal = false;

        public static class Metrics extends Strip.Metrics {

            public final float length;
            public final int ledsPerMeter;

            public final float pointSpacing;

            public Metrics(float length, int numPoints, int ledsPerMeter) {
                super(numPoints);

                this.length = length;
                this.ledsPerMeter = ledsPerMeter;
                this.pointSpacing = INCHES_PER_METER / ledsPerMeter;
            }

            public Metrics(int numPoints, float spacing) {
                super(numPoints);

                this.length = numPoints * spacing;
                this.ledsPerMeter = floor((INCHES_PER_METER / this.length) * numPoints);
                this.pointSpacing = spacing;
            }
        }

        public CubesStrip(String id, Metrics metrics, boolean isHorizontal, List<LXPoint> points) {
            super(id, metrics, points);
        }

        public CubesStrip(String id, Metrics metrics, boolean isHorizontal) {
            super(id, metrics, new Fixture(metrics));
            this.isHorizontal = isHorizontal;
        }

        public void updatePoints(LXTransform t) {
          Metrics m = (Metrics) metrics;
            float offset = (m.length - (m.numPoints - 1) * m.pointSpacing) / 2f;

            t.push();
            t.translate(offset, -Cube.CHANNEL_WIDTH / 2f, 0);
            for (int i = 0; i < m.numPoints; i++) {
                points[i].update(t.x(), t.y(), t.z());
                t.translate(m.pointSpacing, 0, 0);
            }
            t.pop();
            update(true, true);
        }

        private static class Fixture extends LXAbstractFixture {
            private Fixture(Metrics metrics) {
                float offset = (metrics.length - (metrics.numPoints - 1) * metrics.pointSpacing) / 2f;

                LXTransform transform = new LXTransform();
                transform.push();
                transform.translate(offset, -Cube.CHANNEL_WIDTH / 2f, 0);

                for (int i = 0; i < metrics.numPoints; i++) {
                    LXPoint point = new LXPoint(transform.x(), transform.y(), transform.z());
                    this.points.add(point);
                    transform.translate(metrics.pointSpacing, 0, 0);
                }

                transform.pop();
            }
        }
    }
}
