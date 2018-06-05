package com.symmetrylabs.layouts.tree;

import static com.symmetrylabs.util.DistanceConstants.*;

import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXMatrix;
import heronarts.lx.transform.LXTransform;
import heronarts.lx.transform.LXVector;
import processing.core.PApplet;
import static processing.core.PApplet.*;

import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeModel extends SLModel {

    public final List<Limb> limbs;
    public final List<Branch> branches;
    public final List<Twig> twigs;
    public final List<Leaf> leaves;

    public TreeModel(TreeConfig config) {
        super(new Fixture(config));
        Fixture f = (Fixture) this.fixtures.get(0);
        this.limbs  = Collections.unmodifiableList(f.limbs);

      final List<Branch> branches = new ArrayList<>();
      for (Limb limb : limbs) {
          branches.addAll(limb.branches);
      }
      this.branches = Collections.unmodifiableList(branches);

      final List<Twig> twigs = new ArrayList<>();
      for (Branch branch : branches) {
          twigs.addAll(branch.twigs);
      }
      this.twigs = new Collections.unmodifiableList(twigs);

      final List<Leaf> leaves = new ArrayList<>();
      for (Twig twig : twigs) {
          leaves.addAll(twig.leaves);
      }
      this.leaves = Collections.unmodifiableList(leaves);
    }

    private static class Fixture extends LXAbstractFixture {
        private final List<Limb> limbs = new ArrayList<Limb>();

        private Fixture(TreeConfig config) {
            for (LimbConfig limbConfig : config) {
                // create a limb!
            }
        }
    }

    public static class Limb extends SLModel {

        public final LimbConfig config;

        public float y;
        public float length;
        public float azimuth;

        public final List<Branch> branches;
        public final List<Twig> twigs;
        public final List<Leaf> leaves;

        public Limb(LXTransform t, LimbConfig config) {
            super(new Fixture(t, config));
            this.config = config;
            
            this.y = config.y;
            this.length = config.length;
            this.azimuth = config.azimuth;

            Fixture f = (Fixture) this.fixtures.get(0);
            this.branches = Collections.unmodifiableList(f.branches);
            this.twigs = Collections.unmodifiableList(branch.twigs);
            this.leaves = Collections.unmodifiableList(twig.leaves);
        }


        // private static class Fixture extends LXAbstractFixture {

        //     private final List<Branch> branches = new ArrayList<Branch>();

        //     Fixture(float y, float azimuth, Size size) {
        //         LXTransform t = new LXTransform();
        //         t.translate(0, y, 0);
        //         t.rotateY(HALF_PI - azimuth);
        //         t.rotateX(HALF_PI - PI/12);
        //         if (size == Size.FULL) {
        //             t.translate(0, SECTION_1.len, 0);
        //         }
        //         if (size != Size.SMALL) {
        //             t.translate(0, SECTION_2.len, 0);
        //         }
        //         t.rotateX(-PI/6);
        //         t.translate(0, SECTION_3.len);
        //         t.rotateX(-PI/6);

        //         for (BranchConfig config : branchConfigs) {
        //             branches.add(new Branch(t, config));
        //         }
        //     }

        //     private void addBranch(LXTransform t, BranchConfig config) {
        //         addBranch(t, config.x, config.y, config.z, config.azimuth, config.elevation, config.tilt);
        //     }

        //     private void addBranch(LXTransform t, float x, float y, float z, float azimuth, float elevation, float tilt) {
        //         t.push();
        //         t.translate(config.x, config.y, config.z);
        //         t.rotateX(config.x * Math.PI / 180.);
        //         t.rotateY(config.y * Math.PI / 180.);
        //         t.rotateZ(config.z * Math.PI / 180.);
                
        //         Branch branch = new Branch(new Branch.Orientation(
        //             t.x(),
        //             t.y(),
        //             t.z(),
        //             azimuth,
        //             elevation,
        //             tilt
        //         ));

        //         branches.add(branch);
        //         t.pop();
        //     }
        }
    }

    /**
     * A branch is mounted on a major limb and houses many
     * leaf assemblages. This class is oriented in the x-y
     * plane with the branch pointing "upwards" in the y-axis.
     *
     * Leaf assemblages shoot off the left and right sides
     * as well as one out the top.
     */
    public static class Branch extends SLModel {
        public static final int NUM_ASSEMBLAGES = 8;
        public static final int NUM_LEAVES = NUM_ASSEMBLAGES * LeafAssemblage.NUM_LEAVES;
        public static final int NUM_LEDS = NUM_ASSEMBLAGES * LeafAssemblage.NUM_LEDS;
        public static final float LENGTH = 6*FEET;
        public static final float WIDTH = 7*FEET;

        public static class Orientation {

            // Base of the branch, in global space
            public final float x;
            public final float y;
            public final float z;

            // Azimuth and elevation of branch's normal vector (the direction it points)
            public final float azimuth;
            public final float elevation;

            // Tilt of the branch about its normal (think of the branch doing a "barrel roll")
            public final float tilt;

            public Orientation(float x, float y, float z, float azimuth, float elevation, float tilt) {
                this.x = x;
                this.y = y;
                this.z = z;
                this.azimuth = azimuth;
                this.elevation = elevation;
                this.tilt = tilt;
            }
        }

        // Orientation of this branch
        public final Orientation orientation;

        // IP address of this branch, if known
        public final String ip;

        public final List<LeafAssemblage> assemblages;
        public final List<Leaf> leaves;

        // Position of the branch in global space
        public final float x;
        public final float y;
        public final float z;

        // Azimuth and elevation of the branch in global space
        public final float azimuth;
        public final float elevation;

        private static final float RIGHT_THETA = -QUARTER_PI;
        private static final float LEFT_THETA = QUARTER_PI;

        private static final float RIGHT_OFFSET = 12*INCHES;
        private static final float LEFT_OFFSET = -12*INCHES;

        // Assemblage positions are relative to an assemblage
        // facing upwards. Each leaf assemblage
        public static final LeafAssemblage.Orientation[] ASSEMBLAGES = {
            // Right side bottom to top
            new LeafAssemblage.Orientation(RIGHT_OFFSET, 2*INCHES, RIGHT_THETA),
            new LeafAssemblage.Orientation(RIGHT_OFFSET, 14*INCHES, RIGHT_THETA),
            new LeafAssemblage.Orientation(RIGHT_OFFSET, 26*INCHES, RIGHT_THETA),
            new LeafAssemblage.Orientation(RIGHT_OFFSET, 38*INCHES, RIGHT_THETA),

            // End node
            new LeafAssemblage.Orientation(0, 44*INCHES, 0),

            // Left side top to bottom
            new LeafAssemblage.Orientation(LEFT_OFFSET, 32*INCHES, LEFT_THETA),
            new LeafAssemblage.Orientation(LEFT_OFFSET, 20*INCHES, LEFT_THETA),
            new LeafAssemblage.Orientation(LEFT_OFFSET, 8*INCHES, LEFT_THETA)
        };

        private static LXTransform getTransform(Orientation orientation) {
            return new LXTransform()
                .translate(orientation.x, orientation.y, orientation.z)
                .rotateY(HALF_PI - orientation.azimuth)
                .rotateX(HALF_PI - orientation.elevation)
                .rotateY(orientation.tilt);
        }

        public Branch(Orientation orientation) {
            this(getTransform(orientation), orientation);
        }

        public Branch(StellarBranchConfig branchConfig) {
            super(new Fixture(branchConfig));
            this.ip = branchConfig.ip;
            this.orientation = null;
            Fixture f = (Fixture) this.fixtures.get(0);
            this.assemblages = Collections.unmodifiableList(f.assemblages);
            List<Leaf> leaves = new ArrayList<Leaf>();
            for (LeafAssemblage assemblage : this.assemblages) {
                for (Leaf leaf : assemblage.leaves) {
                    leaves.add(leaf);
                }
            }
            this.leaves = Collections.unmodifiableList(leaves);
            this.x = this.leaves.get(0).x;
            this.y = this.leaves.get(0).y;
            this.z = this.leaves.get(0).z;
            this.azimuth = atan2(this.z, this.x);
            this.elevation = atan2(this.y, dist(0, 0, this.x, this.z));
        }

        public Branch(LXTransform t) {
            // TODO(mcslee): compute azim/elev/tilt from matrix?
            this(t, new Orientation(t.x(), t.y(), t.z(), 0, 0, 0));
        }

        public Branch(LXTransform t, Orientation orientation) {
            super(new Fixture(t));
            this.ip = null;
            this.orientation = orientation;
            this.x = t.x();
            this.y = t.y();
            this.z = t.z();
            this.azimuth = atan2(this.z, this.x);
            this.elevation = atan2(this.y, dist(0, 0, this.x, this.z));
            Fixture f = (Fixture) this.fixtures.get(0);
            this.assemblages = Collections.unmodifiableList(f.assemblages);
            List<Leaf> leaves = new ArrayList<Leaf>();
            for (LeafAssemblage assemblage : this.assemblages) {
                for (Leaf leaf : assemblage.leaves) {
                    leaves.add(leaf);
                }
            }
            this.leaves = Collections.unmodifiableList(leaves);
        }

        private static class Fixture extends LXAbstractFixture {

            private final List<LeafAssemblage> assemblages = new ArrayList<LeafAssemblage>();

            Fixture(StellarBranchConfig branchConfig) {
                for (int i = 0; i < branchConfig.channels.length; ++i) {
                    LXMatrix matrix = branchConfig.channels[i];
                    if (matrix != null) {
                        LeafAssemblage assemblage = new LeafAssemblage(i, new LXTransform(matrix));
                        this.assemblages.add(assemblage);
                        addPoints(assemblage);
                    }
                }
            }

            Fixture(LXTransform t) {
                int channel = 0;
                for (LeafAssemblage.Orientation assemblage : ASSEMBLAGES) {
                    t.push();
                    t.translate(assemblage.x, assemblage.y, 0);
                    t.rotateZ(assemblage.theta);
                    t.rotateY(assemblage.tilt);
                    LeafAssemblage leafAssemblage = new LeafAssemblage(channel++, t, assemblage);
                    this.assemblages.add(leafAssemblage);
                    addPoints(leafAssemblage);
                    t.pop();
                }
            }
        }
    }

    /**
     * An assemblage is a modular fixture with multiple leaves.
     */
    public static class LeafAssemblage extends SLModel {

        public static final int NUM_LEAVES = 15;
        public static final int NUM_LEDS = NUM_LEAVES * Leaf.NUM_LEDS;

        public static final float LENGTH = 26*INCHES;
        public static final float WIDTH = 24*INCHES;

        // Orientation of a leaf assemblage, relative to parent branch
        public static class Orientation {

            // Offset from base of branch, y-axis points "up" the branch
            public final float x;
            public final float y;

            // Rotation in the x-y plane, relative to the branch
            // wwhere y is pointing "up" the branch
            public final float theta;

            // Tilt of the leaf assemblage about the axis of its normal
            // e.g. a "barrel roll" on the leaf assemblage
            public final float tilt;

            Orientation(float x, float y, float theta) {
                this.x = x;
                this.y = y;
                this.theta = theta;
                this.tilt = -QUARTER_PI + HALF_PI * (float) Math.random();
            }
        }

        // These positions indicate how a leaf is positioned on an assemblage,
        // assuming the assemblage is facing "up", the main stem is at (0, 0)
        // Positive x-values move to the right, and positive y-values move
        // up the branch, away from the base stem.
        //
        // Third argument is the rotation of the leaf on the x-y plane, 0
        // is the leaf pointing "up", HALF_PI is pointing to the left,
        // -HALF_PI is pointing to the right, etc.
        public static final Leaf.Orientation[] LEAVES = {
            new Leaf.Orientation(0, 4.5f*INCHES, -1.7f*INCHES, -HALF_PI - QUARTER_PI), // A
            new Leaf.Orientation(1, 5.5f*INCHES, 0f*INCHES, -HALF_PI), // B
            new Leaf.Orientation(2, 2.0f*INCHES, 3.5f*INCHES, -HALF_PI + QUARTER_PI), // C
            new Leaf.Orientation(3, 3.5f*INCHES, 7.5f*INCHES, -HALF_PI), // D
            new Leaf.Orientation(4, 4.0f*INCHES, 11.2f*INCHES, -HALF_PI), // E
            new Leaf.Orientation(5, 3.0f*INCHES, 9.5f*INCHES, -HALF_PI + QUARTER_PI), // F
            new Leaf.Orientation(6, 3.5f*INCHES, 12.7f*INCHES, -HALF_PI + QUARTER_PI), // G
            new Leaf.Orientation(7, 0.0f*INCHES, 13.5f*INCHES, 0), // H
            null, // I
            null, // J
            null, // K
            null, // L
            null, // M
            null, // N
            null, // O
        };

        static {
            // Make sure we didn't bork that array editing manually!
            assert(LEAVES.length == NUM_LEAVES);

            // The last seven leaves are just inverse of the first about
            // the y-axis.
            for (int i = 0; i < 7; ++i) {
                Leaf.Orientation thisLeaf = LEAVES[i];
                int index = LEAVES.length - 1 - i;
                LEAVES[index] = new Leaf.Orientation(index, -thisLeaf.x, thisLeaf.y, -thisLeaf.theta);
            }
        }

        public final Orientation orientation;
        public final List<Leaf> leaves;
        public final int channel;

        public LeafAssemblage(int channel, LXTransform t) {
            this(channel, t, new Orientation(0, 0, 0));
        }

        public LeafAssemblage(int channel, LXTransform t, Orientation orientation) {
            super(new Fixture(t));
            Fixture f = (Fixture) this.fixtures.get(0);
            this.channel = channel;
            this.leaves = Collections.unmodifiableList(f.leaves);
            this.orientation = orientation;
        }

        private static class Fixture extends LXAbstractFixture {

            private final List<Leaf> leaves = new ArrayList<Leaf>();

            Fixture(LXTransform t) {
                t.push();
                // TODO(mcslee): do we want this?
                t.translate(0, 5*INCHES, 0);
                for (int i = 0; i < NUM_LEAVES; ++i) {
                    Leaf.Orientation leafOrientation = LEAVES[i];
                    t.push();
                    t.translate(leafOrientation.x, leafOrientation.y, (i % 3) * (.1f*INCHES));
                    t.rotateZ(leafOrientation.theta);
                    Leaf leaf = new Leaf(t, leafOrientation);
                    this.leaves.add(leaf);
                    addPoints(leaf);
                    t.pop();
                }
                t.pop();
            }
        }
    }

    /**
     * The base addressable fixture, a Leaf with LEDs embedded inside.
     * Currently modeled as a single point. Room for improvement!
     */
    public static class Leaf extends SLModel {
        public static final int NUM_LEDS = 7;
        public static final float LED_OFFSET = .75f*INCHES;
        public static final float LED_SPACING = 1.3f*INCHES;
        public static final float WIDTH = 4.75f*INCHES;
        public static final float LENGTH = 6.5f*INCHES;

        // Orientation of a leaf relative to leaf assemblage
        public static class Orientation {

            public final int index;

            // X-Y position relative to leaf assemblage base
            // y-axis pointing "up" the leaf assemblage
            public final float x;
            public final float y;

            // Rotation about X-Y plane relative to parent assemblage
            public final float theta;

            // Tilt of the individual leaf
            public final float tilt;

            Orientation(int index, float x, float y, float theta) {
                this.index = index;
                this.x = x;
                this.y = y;
                this.theta = theta;
                this.tilt = -QUARTER_PI + HALF_PI * (float) Math.random();
            }
        }

        public final LXPoint point;

        public final float x;
        public final float y;
        public final float z;

        public final LXVector[] coords = new LXVector[4];

        public final Orientation orientation;

        public Leaf() {
            this(new LXTransform());
        }

        public Leaf(LXTransform t) {
            this(t, new Orientation(0, 0, 0, 0));
        }

        public Leaf(LXTransform t, Orientation orientation) {
            super(new Fixture(t, orientation));
            this.orientation = orientation;
            this.x = t.x();
            this.y = t.y();
            this.z = t.z();
            this.point = this.points[0];

            // Precompute boundary coordinates for faster rendering, these
            // can be dumped into a VBO for a shader.
            t.push();
            t.translate(-WIDTH/2, 0);
            this.coords[0] = t.vector();
            t.translate(0, LENGTH);
            this.coords[1] = t.vector();
            t.translate(WIDTH, 0);
            this.coords[2] = t.vector();
            t.translate(0, -LENGTH);
            this.coords[3] = t.vector();
            t.pop();
        }

        private static class Fixture extends LXAbstractFixture {
            Fixture(LXTransform t, Orientation orientation) {
                t.push();
                t.translate(.1f*INCHES, LED_OFFSET, 0);
                addPoint(new LXPoint(t));
                t.translate(0, LED_SPACING, 0);
                addPoint(new LXPoint(t));
                t.translate(0, LED_SPACING, 0);
                addPoint(new LXPoint(t));
                t.translate(-.1f*INCHES, LED_SPACING, 0);
                addPoint(new LXPoint(t));
                t.translate(-.1f*INCHES, -LED_SPACING, 0);
                addPoint(new LXPoint(t));
                t.translate(0, -LED_SPACING, 0);
                addPoint(new LXPoint(t));
                t.translate(0, -LED_SPACING, 0);
                addPoint(new LXPoint(t));
                t.pop();
            }
        }
    }

    // Cheap mockup of a tree canopy until we get a better model
    // based upon actual mechanical drawings and fabricated dimensions.
    // This one just estimates a cloud of points distributed across
    // a hemisphere. Left here for reference.
    public static class Hemisphere extends SLModel {

        public static final float NUM_POINTS = 25000;
        public static final float INNER_RADIUS = 33*FEET;
        public static final float OUTER_RADIUS = 36*FEET;

        public Hemisphere() {
            super(new Fixture());
        }

        private static class Fixture extends LXAbstractFixture {
            Fixture() {
                for (int i = 0; i < NUM_POINTS; ++i) {
                    float azimuth = (98752234*i + 4871433);
                    float elevation = (i*234.351234f) % HALF_PI;
                    float radius = INNER_RADIUS + (i * 7*INCHES) % (OUTER_RADIUS - INNER_RADIUS);
                    double x = radius * Math.cos(azimuth) * Math.cos(elevation);
                    double z = radius * Math.sin(azimuth) * Math.cos(elevation);
                    double y = radius * Math.sin(elevation);
                    addPoint(new LXPoint(x, y, z));
                }
            }
        }
    }

    public static class StellarBranchConfig {
        final String ip;
        final LXMatrix[] channels = new LXMatrix[Branch.NUM_ASSEMBLAGES];

        public StellarBranchConfig(String ip) {
            this.ip = ip;
            for (int i = 0; i < channels.length; ++i) {
                channels[i] = null;
            }
        }
    }

    public static class StellarFixtureConfig {

        static final String CHANNEL_ZERO = "Leafs Chanel Zero";
        static final String CHANNEL_FOUR = "Leafs Chanel Four";
        static final int NO_FIXTURE_ID = -1;

        // TODO(mcslee): determine accurately what this is!
        static final float ASSEMBLAGE_Y_OFFSET = 4*INCHES;

        final String ip;
        final LXMatrix matrix;
        final int fixtureId;
        final int nextFixtureId;
        final int channel;

        public StellarFixtureConfig(JSONObject fixture, String fixtureType) {
            this.ip = fixture.getString("IP", null);
            this.fixtureId = fixture.getInt("id", NO_FIXTURE_ID);
            this.nextFixtureId = fixture.getInt("child", NO_FIXTURE_ID);
            this.channel = fixtureType.equals(CHANNEL_ZERO) ? 0 : 4;

            // Load matrix values
            JSONArray matrixArr = fixture.getJSONArray("Matrix");
            float[] m = new float[16];
            for (int mi = 0; mi < m.length; ++mi) {
                m[mi] = matrixArr.getFloat(mi);
            }

            // Construct matrix converted into LX space
            this.matrix = new LXMatrix()
            .scaleX(-1)
            .scale(INCHES_PER_METER * INCHES)
            .multiply(
                // NOTE: stellar indexes matrix vertically
                m[0], m[4], m[8], m[12],
                m[1], m[5], m[9], m[13],
                m[2], m[6], m[10], m[14],
                m[3], m[7], m[11], m[15]
            )
            .scale(METERS_PER_INCH / INCHES)
            .translate(0, ASSEMBLAGE_Y_OFFSET, 0);
        }
    }
}
