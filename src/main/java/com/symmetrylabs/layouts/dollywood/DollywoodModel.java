package com.symmetrylabs.layouts.dollywood;

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
import heronarts.lx.transform.LXVector;

import com.symmetrylabs.layouts.oslo.TreeModel;
import processing.core.PApplet;

import static com.symmetrylabs.util.MathUtils.*;
import static com.symmetrylabs.util.DistanceConstants.*;
import static com.symmetrylabs.util.MathConstants.*;

/**
 * Top-level model of the entire sculpture. This contains a list of
 * every cube on the sculpture, which forms a hierarchy of faces, strips
 * and points.
 */
public class DollywoodModel extends StripsModel<DollywoodModel.Wing> {
    public final TreeModel treeModel;

    protected final List<Butterfly> butterflies = new ArrayList<>();
    protected final Map<String, Butterfly> butterflyTable = new HashMap<>();

    protected final List<Wing> wings = new ArrayList<>();
    protected final Map<String, Wing> wingTable = new HashMap<>();

    private final List<Butterfly> butterfliesUnmodifiable = Collections.unmodifiableList(butterflies);
    private final List<Wing> wingsUnmodifiable = Collections.unmodifiableList(wings);

    private final Butterfly[] _butterflies;

    // public DollywoodModel() {
    //   this(new ArrayList<>(), new Butterfly[0]);
    // }

    public DollywoodModel(PApplet applet, TreeModel treeModel, List<Butterfly> butterflies) {
        super(new Fixture(applet, treeModel, butterflies));
        Fixture fixture = (Fixture) this.fixtures.get(0);
        this.treeModel = treeModel;

        int i = 0;
        this._butterflies = new Butterfly[butterflies.size()];
        for (Butterfly butterfly : butterflies) {
            if (butterfly != null) {
                this.butterflyTable.put(butterfly.id, butterfly);
                this.butterflies.add(butterfly);
                this._butterflies[i++] = butterfly;
                this.wings.addAll(butterfly.getWings());
                this.strips.addAll(butterfly.getWings());

                for (Wing wing : butterfly.getWings()) {
                    this.wingTable.put(wing.id, wing);
                }
            }
        }
    }

    public List<Butterfly> getButterflies() {
        return butterfliesUnmodifiable;
    }

    public List<Wing> getWings() {
        return wingsUnmodifiable;
    }

    private static class Fixture extends LXAbstractFixture {
        private Fixture(PApplet applet, TreeModel treeModel, List<Butterfly> butterflies) {
            for (LXPoint p : treeModel.points) {
                this.points.add(p);
            }

            for (Butterfly butterfly : butterflies) {
                if (butterfly != null) {
                    for (LXPoint point : butterfly.points) {
                        this.points.add(point);
                    }
                }
            }
        }
    }

    public Butterfly getButterflyByRawIndex(int index) {
        return _butterflies[index];
    }

    public Butterfly getButterflyById(String id) {
        return this.butterflyTable.get(id);
    }

    public Wing getWingById(String id) {
        return this.wingTable.get(id);
    }

    // public static class Cluster extends SLModel {

    //   public static final int NUM_BUTTERFLIES = 3;
    //   public static final int NUM_LEDS = NUM_BUTTERFLIES * (12+12+8+8);

    //   public static final float LENGTH = 24*INCHES;
    //   public static final float WIDTH = 24*INCHES;

    //   public final LXMatrix transform;

    //   // Orientation of a leaf assemblage, relative to parent branch
    //   public static class Orientation {

    //     // Offset from base of branch, y-axis points "up" the branch
    //     public final float x;
    //     public final float y;

    //     // Rotation in the x-y plane, relative to the branch
    //     // wwhere y is pointing "up" the branch
    //     public final float theta;

    //     // Tilt of the leaf assemblage about the axis of its normal
    //     // e.g. a "barrel roll" on the leaf assemblage
    //     public final float tilt;

    //     Orientation(float x, float y, float theta) {
    //       this.x = x;
    //       this.y = y;
    //       this.theta = theta;
    //       this.tilt = -QUARTER_PI + HALF_PI * (float) Math.random();
    //     }
    //   }

    //   // These positions indicate how a leaf is positioned on an assemblage,
    //   // assuming the assemblage is facing "up", the main stem is at (0, 0)
    //   // Positive x-values move to the right, and positive y-values move
    //   // up the branch, away from the base stem.
    //   //
    //   // Third argument is the rotation of the leaf on the x-y plane, 0
    //   // is the leaf pointing "up", HALF_PI is pointing to the left,
    //   // -HALF_PI is pointing to the right, etc.
    //   public static final Cluster.Orientation[] LEAVES = {
    //     new Cluster.Orientation(0, 4.5f*INCHES, -1.7f*INCHES, -HALF_PI - QUARTER_PI), // A
    //     new Cluster.Orientation(1, 5.5f*INCHES, 0f*INCHES, -HALF_PI), // B
    //     new Cluster.Orientation(2, 2.0f*INCHES, 3.5f*INCHES, -HALF_PI + QUARTER_PI), // C
    //   };

    //   public final Orientation orientation;
    //   public final List<Butterfly> butterflies;
    //   public final int channel;

    //   public Cluster(int channel, LXTransform t) {
    //     this(channel, t, new Orientation(0, 0, 0));
    //   }

    //   public Cluster(int channel, LXTransform t, Orientation orientation) {
    //     super(new Fixture(t));
    //     Fixture f = (Fixture) this.fixtures.get(0);
    //     this.transform = t.getMatrix();
    //     this.channel = channel;
    //     this.butterflies = Collections.unmodifiableList(f.butterflies);
    //     this.orientation = orientation;
    //   }

    //   private static class Fixture extends LXAbstractFixture {

    //     private final List<Butterfly> butterflies = new ArrayList<Butterfly>();

    //     Fixture(LXTransform t) {
    //       t.push();
    //       t.translate(0, 5*INCHES, 0);
    //       for (int i = 0; i < NUM_BUTTERFLIES; ++i) {
    //         Butterfly.Orientation leafOrientation = LEAVES[i];
    //         t.push();
    //         t.translate(butterflyOrientation.x, butterflyOrientation.y, (i % 3) * (.1f*INCHES));
    //         t.rotateZ(butterflyOrientation.theta);
    //         Butterfly butterfly = new Butterfly(t, butterflyOrientation);
    //         this.butterflies.add(leaf);
    //         addPoints(leaf);
    //         t.pop();
    //       }
    //       t.pop();
    //     }
    //   }
    // }

    public static class Butterfly extends StripsModel<Wing> {

        public static final int LARGE_NUM_LEDS = 40;
        public static final int NUM_WINGS = 4;
        public final LXPoint point;
        public static enum Type {
            SMALL, LARGE, SHARP_CURVY, CURVY
        }

        public final String id;
        public final Type type;
        public final LXVector[] coords;

        protected final List<Wing> wings = new ArrayList<>();
        private final List<Wing> wingsUnmodifiable = Collections.unmodifiableList(wings);

        // public final float x;
        // public final float y;
        // public final float z;
        // public final float rx;
        // public final float ry;
        // public final float rz;

        public Butterfly(String id, Type type, LXTransform transform) {
            super(new Fixture(id, type, transform));
            Fixture fixture = (Fixture)this.fixtures.get(0);
            this.id = id;
            this.type = type;
            this.coords = fixture.coords;
            this.point = this.points[0];

            // while (rx < 0) rx += 360;
            // while (ry < 0) ry += 360;
            // while (rz < 0) rz += 360;
            // rx = rx % 360;
            // ry = ry % 360;
            // rz = rz % 360;

            // this.x = x;
            // this.y = y;
            // this.z = z;
            // this.rx = rx;
            // this.ry = ry;
            // this.rz = rz;

            this.wings.addAll(fixture.wings);
        }

        public List<Wing> getWings() {
            return wingsUnmodifiable;
        }

        private static class Fixture extends LXAbstractFixture {

            public final LXVector[] coords = new LXVector[4];

            private final List<Wing> wings = new ArrayList<>();

            private Fixture(String id, Butterfly.Type type, LXTransform transform) {
                transform.push();
                if (transform.y() < 100) {
                    transform.translate(0, -30, 0);
                }
                // Precompute boundary coordinates for faster rendering, these
                // can be dumped into a VBO for a shader.
                transform.push();
                transform.translate(-10.5f, -8);
                this.coords[0] = transform.vector();
                transform.translate(0, 16);
                this.coords[1] = transform.vector();
                transform.translate(18, 0);
                this.coords[2] = transform.vector();
                transform.translate(0, -16);
                this.coords[3] = transform.vector();
                transform.pop();

                if (type == Butterfly.Type.LARGE || type == Butterfly.Type.SMALL) {
                    this.wings.add(new Wing(
                        id+"_upper_left_wing",
                        (type == Butterfly.Type.LARGE) ? Wing.Type.LARGE_UPPER : Wing.Type.SMALL_UPPER,
                        new float[] {0, 0, 0},
                        new float[] {0, 0, 40},
                        transform
                    ));

                    this.wings.add(new Wing(
                        id+"_upper_right_wing",
                        (type == Butterfly.Type.LARGE) ? Wing.Type.LARGE_UPPER : Wing.Type.SMALL_UPPER,
                        new float[] {-2, 0, 0},
                        new float[] {0, 180, 40},
                        transform
                    ));

                    this.wings.add(new Wing(
                        id+"_lower_left_wing",
                        (type == Butterfly.Type.LARGE) ? Wing.Type.LARGE_LOWER : Wing.Type.SMALL_LOWER,
                        new float[] {0, -5, 0},
                        new float[] {0, 0, 35},
                        transform
                    ));

                    this.wings.add(new Wing(
                        id+"_lower_right_wing",
                        (type == Butterfly.Type.LARGE) ? Wing.Type.LARGE_LOWER : Wing.Type.SMALL_LOWER,
                        new float[] {-2, -5, 0},
                        new float[] {0, 180, 35},
                        transform
                    ));

                    for (Wing wing : wings) {
                        for (LXPoint point : wing.points) {
                            this.points.add(point);
                        }
                    }
                }

                if (type == Butterfly.Type.SHARP_CURVY) {
                    this.wings.add(new Wing(
                        id+"_left_wing",
                        Wing.Type.SHARP_CURVY,
                        new float[] {0, 0, 0},
                        new float[] {0, 0, 0},
                        transform
                    ));

                    this.wings.add(new Wing(
                        id+"_right_wing",
                        Wing.Type.SHARP_CURVY,
                        new float[] {0, 0, 0},
                        new float[] {0, 180, 0},
                        transform
                    ));

                    for (Wing wing : wings) {
                        for (LXPoint point : wing.points) {
                            this.points.add(point);
                        }
                    }
                }

                if (type == Butterfly.Type.CURVY) {
                    this.wings.add(new Wing(
                        id+"_left_wing",
                        Wing.Type.CURVY,
                        new float[] {0, 0, 0},
                        new float[] {0, 0, 0},
                        transform
                    ));

                    this.wings.add(new Wing(
                        id+"_right_wing",
                        Wing.Type.CURVY,
                        new float[] {0, 0, 0},
                        new float[] {0, 180, 0},
                        transform
                    ));

                    for (Wing wing : wings) {
                        for (LXPoint point : wing.points) {
                            this.points.add(point);
                        }
                    }
                }

                transform.pop();
            }
        }
    }

    public static class Wing extends Strip {

        public static final int LARGE_UPPER_NUM_LEDS = 12;
        public static final int LARGE_LOWER_NUM_LEDS = 8;

        public enum Type {
            LARGE_UPPER, LARGE_LOWER, SMALL_UPPER, SMALL_LOWER, SHARP_CURVY, CURVY
        }

        public static class ButterflyMetrics extends Strip.Metrics {
            public final int LEDS_PER_METER = 30;
            public final float POINT_SPACING = INCHES_PER_METER / LEDS_PER_METER;
            public final float length;
            public final float arcWidth;

            public ButterflyMetrics(int numPoints, float arcWidth) {
                super(numPoints);
                this.length = numPoints * POINT_SPACING;
                this.arcWidth = arcWidth;
            }
        }

        public final Type type;

        public final String id;

        public final LXVector[] coords;

        public Wing(String id, Type type, float[] coordinates, float[] rotations, LXTransform transform) {
            super(id, new Strip.Metrics(Wing.getNumPointsOnWing(type)), new Fixture(type, coordinates, rotations, transform));
            Fixture fixture = (Fixture)this.fixtures.get(0);
            this.id = id;
            this.type = type;
            this.coords = fixture.coords;
        }

        private static int getNumPointsOnWing(Type type) {
            switch(type) {
                case LARGE_UPPER: return 12;
                case LARGE_LOWER: return 8;
                case SMALL_UPPER: return 8;
                case SMALL_LOWER: return 6;
                case SHARP_CURVY: return 46;
                case CURVY: return 60;
            }
            return 0;
        }

        private static class Fixture extends LXAbstractFixture {
            private final LXVector[] coords = new LXVector[4];

            private Fixture(Type type, float[] coordinates, float[] rotations, LXTransform transform) {
                transform.push();
                transform.translate(coordinates[0], coordinates[1], coordinates[2]);
                transform.rotateX(rotations[0] * PI / 180.);
                transform.rotateY(rotations[1] * PI / 180.);
                transform.rotateZ(rotations[2] * PI / 180.);

                if (type == Wing.Type.LARGE_UPPER || type == Wing.Type.SMALL_UPPER
                 || type == Wing.Type.LARGE_LOWER || type == Wing.Type.SMALL_LOWER) {
                    // Precompute boundary coordinates for faster rendering, these
                    // can be dumped into a VBO for a shader.
                    if (type == Wing.Type.LARGE_UPPER || type == Wing.Type.SMALL_UPPER) {
                        transform.push();
                        transform.rotateZ(-Math.PI / 4f);
                        transform.translate(-1, -1); //tweak
                        this.coords[0] = transform.vector();
                        transform.translate(0, 8);
                        this.coords[1] = transform.vector();
                        transform.translate(9, 0);
                        this.coords[2] = transform.vector();
                        transform.translate(0, -8);
                        this.coords[3] = transform.vector();
                        transform.pop();
                    } else {
                        transform.push();
                        transform.rotateZ(-Math.PI / 6.5f);
                        transform.translate(-0.75f, -2f); //tweak
                        this.coords[0] = transform.vector();
                        transform.translate(0, 6);
                        this.coords[1] = transform.vector();
                        transform.translate(5, 0);
                        this.coords[2] = transform.vector();
                        transform.translate(0, -6);
                        this.coords[3] = transform.vector();
                        transform.pop();
                    }

                    ButterflyMetrics metrics = null;
                    switch (type) {
                        case LARGE_UPPER: metrics = new ButterflyMetrics(6, 10); break;
                        case LARGE_LOWER: metrics = new ButterflyMetrics(4,  6); break;
                        case SMALL_UPPER: metrics = new ButterflyMetrics(4,  7); break; // adjust
                        case SMALL_LOWER: metrics = new ButterflyMetrics(3,  4); break; // adjust
                    }

                    // calculate positions
                    List<float[]> positions = new ArrayList<float[]>();
                    for (int i = 0; i < metrics.numPoints; i++) {
                        float t = i / (float)metrics.numPoints;
                        float x = bezierPoint(0, metrics.arcWidth*0.35f, metrics.arcWidth*0.5f, metrics.arcWidth, t);
                        float y = bezierPoint(0, metrics.arcWidth*-0.2f, metrics.arcWidth*-0.2f, 0, t);
                        positions.add(new float[] {x, y});
                    }

                    // add the points (up then wing and then back down the other side)
                    for (int i = 0; i < metrics.numPoints; i++) {
                        transform.push();
                        transform.translate(positions.get(i)[0], positions.get(i)[1], 0);
                        this.points.add(new LXPoint(transform.x(), transform.y(), transform.z()));
                        transform.pop();
                    }

                    for (int i = metrics.numPoints-1; i > -1; i--) {
                        transform.push();
                        transform.translate(positions.get(i)[0]+(-0.025f*i)+0.1f, positions.get(i)[1]+0.25f, 0);
                        this.points.add(new LXPoint(transform.x(), transform.y(), transform.z()));
                        transform.pop();
                    }
                }


                if (type == Wing.Type.SHARP_CURVY) {
                    transform.push();
                    transform.rotateZ(-Math.PI / 4f);
                    transform.translate(-1, -1); //tweak
                    this.coords[0] = transform.vector();
                    transform.translate(0, 8);
                    this.coords[1] = transform.vector();
                    transform.translate(9, 0);
                    this.coords[2] = transform.vector();
                    transform.translate(0, -8);
                    this.coords[3] = transform.vector();
                    transform.pop();

                    final float[][] positions = new float[][] {
                        new float[] {0f, 3f},new float[] {0.2f, 0.8f},new float[] {0.5f, 1.4f},new float[] {0.9f, 1.85f},
                        new float[] {1.4f, 2.3f},new float[] {1.8f, 2.7f},new float[] {2.4f, 3.2f},new float[] {2.75f, 3.7f},
                        new float[] {3.3f, 4f},new float[] {3.8f, 4.3f},new float[] {4.3f, 4.6f},new float[] {5f, 5f,},
                        new float[] {5.5f, 5.2f},new float[] {6f, 5.4f},new float[] {6.5f, 5.6f},new float[] {7f, 5.8f},
                        new float[] {7.4f, 6f},new float[] {7.5f, 5.6f},new float[] {7.6f, 5.2f},new float[] {7.3f, 4.7f},
                        new float[] {7f, 4f},new float[] {6.7f, 3.6f},new float[] {6.4f, 3.1f},new float[] {6.1f, 2.6f},
                        new float[] {6.1f, 1.9f},new float[] {5.7f, 1.4f},new float[] {5.2f, 1.3f},new float[] {4.5f, 1.1f},
                        new float[] {3.8f, 1f},new float[] {3.2f, 1f},new float[] {2.5f, 1f},new float[] {2f, 0.4f},
                        new float[] {2.6f, 0.1f},new float[] {3.3f, -0.2f},new float[] {3.85f, -0.6f},new float[] {4.3f, -1f},
                        new float[] {4.5f, -1.6f},new float[] {4.3f, -2.2f},new float[] {4f, -2.6f},new float[] {3.4f, -2.9f},
                        new float[] {2.8f, -3f},new float[] {2.2f, -2.7f},new float[] {1.8f, -2.3f},new float[] {1.3f, -1.7f},
                        new float[] {0.9f, -1.2f},new float[] {0.6f, -0.7f},
                    };

                    for (int i = 0; i < positions.length; i++) {
                        transform.push();
                        transform.translate(positions[i][0], positions[i][1], 0);
                        this.points.add(new LXPoint(transform.x(), transform.y(), transform.z()));
                        transform.pop();
                    }
                }

                if (type == Wing.Type.CURVY) {
                    transform.push();
                    transform.rotateZ(-Math.PI / 4f);
                    transform.translate(-1, -1); //tweak
                    this.coords[0] = transform.vector();
                    transform.translate(0, 8);
                    this.coords[1] = transform.vector();
                    transform.translate(9, 0);
                    this.coords[2] = transform.vector();
                    transform.translate(0, -8);
                    this.coords[3] = transform.vector();
                    transform.pop();

                    final float[][] positions = new float[][] {
                        new float[] {0f, 0f},new float[] {0f, 0.8f,},new float[] {0f, 1.6f},new float[] {0f, 2.5f},
                        new float[] {0f, 0f},new float[] {0f, 0.8f,},new float[] {0f, 1.6f},new float[] {0f, 2.5f},
                        new float[] {0f, 0f},new float[] {0f, 0.8f,},new float[] {0f, 1.6f},new float[] {0f, 2.5f},
                        new float[] {0f, 0f},new float[] {0f, 0.8f,},new float[] {0f, 1.6f},new float[] {0f, 2.5f},
                        new float[] {0f, 0f},new float[] {0f, 0.8f,},new float[] {0f, 1.6f},new float[] {0f, 2.5f},
                        new float[] {0f, 0f},new float[] {0f, 0.8f,},new float[] {0f, 1.6f},new float[] {0f, 2.5f},
                        new float[] {0f, 0f},new float[] {0f, 0.8f,},new float[] {0f, 1.6f},new float[] {0f, 2.5f},
                        new float[] {0f, 0f},new float[] {0f, 0.8f,},new float[] {0f, 1.6f},new float[] {0f, 2.5f},
                        new float[] {0f, 3.1f},new float[] {0f, 4f},new float[] {0f, 4.85f},new float[] {0f, 5.5f},
                        new float[] {0f, 6.3f},new float[] {0.2f, 7f},new float[] {0.7f, 7.6f},new float[] {1.2f, 8f},
                        new float[] {1.8f, 8.5f},new float[] {2.3f, 8.9f,},new float[] {3f, 9.4f},new float[] {9.5f, 9.9f},
                        new float[] {4f, 10.2f},new float[] {4.7f, 10.5f},new float[] {5.5f, 10.8f},new float[] {6f, 10.9f},
                        new float[] {6.8f, 10.8f},new float[] {7.2f, 10.2f},new float[] {7.4f, 9.5f},new float[] {7.3f, 8.9f},
                        new float[] {7.2f, 8.2f},new float[] {7.1f, 7.5f},new float[] {7f, 6.8f},new float[] {6f, 7f},
                        new float[] {6.8f, 5.2f},new float[] {6.5f, 4.7f},new float[] {5.9f, 4.4f},new float[] {5.2f, 4.1f},
                        new float[] {4.5f, 4f},new float[] {3.7f, 4f},new float[] {2.9f, 4f},new float[] {2.1f, 4f},
                        new float[] {1.3f, 4.2f},new float[] {0.4f, 4f},new float[] {0.7f, 2.3f},new float[] {1.6f, 2.3f},
                        new float[] {2.2f, 2.3f},new float[] {3f, 2.3f},new float[] {4f, 2.2f},new float[] {4.7f, 2f},
                        new float[] {5.4f, 1.9f},new float[] {6f, 1.6f},new float[] {6.4f, 1f},new float[] {6.8f, 0.2f},
                        new float[] {4.9f, -3.6f},new float[] {4f, -3.9f},new float[] {3.3f, -3.8f},new float[] {2.7f, -2.5f},
                        new float[] {2f, -3f},new float[] {1.4f, -2.3f},new float[] {1f, -0.8f},new float[] {0.5f, -1f}
                    };

                    for (int i = 0; i < positions.length; i++) {
                        transform.push();
                        transform.translate(positions[i][0], positions[i][1], 0);
                        this.points.add(new LXPoint(transform.x(), transform.y(), transform.z()));
                        transform.pop();
                    }
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
