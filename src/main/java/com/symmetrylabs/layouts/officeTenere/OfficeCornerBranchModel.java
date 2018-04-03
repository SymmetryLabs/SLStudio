package com.symmetrylabs.layouts.officeTenere;

import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.LX;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXMatrix;
import heronarts.lx.transform.LXTransform;
import heronarts.lx.transform.LXVector;
import processing.core.PApplet;
import processing.data.JSONArray;
import processing.data.JSONObject;
import static com.symmetrylabs.util.DistanceConstants.*;

import static processing.core.PApplet.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.symmetrylabs.util.DistanceConstants.FEET;
import static com.symmetrylabs.util.DistanceConstants.INCHES;
import static com.symmetrylabs.util.MathConstants.HALF_PI;
import static com.symmetrylabs.util.MathConstants.PI;

public class OfficeCornerBranchModel extends SLModel {

    public enum ModelMode {
        MAJOR_LIMBS,
        STELLAR_IMPORT,
        UNIFORM_BRANCHES
    }

    public static final float TRUNK_DIAMETER = 3*FEET;
    public static final float LIMB_HEIGHT = 10*FEET;
    public static final int NUM_LIMBS = 12;
    public static final boolean SINGLE_BRANCH_MODE = false;
    public static final String STELLAR_FILE = "TenereExportTestMondayWithID.json";

    public final List<Limb> limbs;
    public final List<Branch> branches;
    public final List<LeafAssemblage> assemblages;
    public final List<Leaf> leaves;

    public OfficeCornerBranchModel(PApplet applet, ModelMode mode) {
        super(new Fixture(applet, mode));
        Fixture f = (Fixture) this.fixtures.get(0);
        this.branches = Collections.unmodifiableList(f.branches);
        this.limbs = Collections.unmodifiableList(f.limbs);

        // Collect up all the leaves for top-level reference
        final List<Leaf> leaves = new ArrayList<Leaf>();
        final List<LeafAssemblage> assemblages = new ArrayList<LeafAssemblage>(f.assemblages);
        for (Branch branch : this.branches) {
            for (LeafAssemblage assemblage : branch.assemblages) {
                assemblages.add(assemblage);
            }
        }
        for (LeafAssemblage assemblage : assemblages) {
            for (Leaf leaf : assemblage.leaves) {
                leaves.add(leaf);
            }
        }
        this.assemblages = Collections.unmodifiableList(assemblages);
        this.leaves = Collections.unmodifiableList(leaves);
    }

    private static class Fixture extends LXAbstractFixture {

        private final List<Branch> branches = new ArrayList<Branch>();
        private final List<LeafAssemblage> assemblages = new ArrayList<LeafAssemblage>();
        private final List<Limb> limbs = new ArrayList<Limb>();

        Fixture(PApplet applet, ModelMode mode) {
            // build the two branches

            // This list describes where each of the Twigs will be relative to the branch
            List<LXTransform> twigTransforms = new ArrayList<LXTransform>();

            LXTransform t = new LXTransform();
            t.translate(0,0,0);

            twigTransforms.add(t);
            twigTransforms.add(new LXTransform().translate(10*FEET, 0, 0));



            addBranch(new Branch.Orientation(0*FEET, 0*FEET, 0, HALF_PI, HALF_PI, 0));
            addBranch(new Branch.Orientation(5*FEET, 0*FEET, 0, HALF_PI*1.2f, HALF_PI*1.15f, PI*0.05f));
        }

        private void addLimb(float y, float azimuth, Limb.Size size) {
            Limb limb = new Limb(y, azimuth, size);
            this.limbs.add(limb);
            addPoints(limb);
            for (Branch branch : limb.branches) {
                this.branches.add(branch);
            }
        }

        private void addAssemblage(LXTransform t) {
            addAssemblage(new LeafAssemblage(0, t));
        }

        private void addAssemblage(LeafAssemblage assemblage) {
            this.assemblages.add(assemblage);
            addPoints(assemblage);
        }

        private void addBranch(StellarBranchConfig branchConfig) {
            addBranch(new Branch(branchConfig));
        }

        private void addBranch(Branch.Orientation orientation) {
            addBranch(new Branch(orientation));
        }

        private void addBranch(Branch branch) {
            this.branches.add(branch);
            addPoints(branch);
        }
    }

    public static class Limb extends SLModel {
        public enum Size {
            FULL,
            MEDIUM,
            SMALL
        };

        public static class Section {
            public final float radius;
            public final float len;
            public final float bend;

            public Section(float radius, float len, float bend) {
                this.radius = radius;
                this.len = len;
                this.bend = bend;
            }
        }

        public final static Section SECTION_1 = new Section(4.6875f*INCHES, 4f*FEET, 0);
        public final static Section SECTION_2 = new Section(3.75f*INCHES, 4f*FEET, 0);
        public final static Section SECTION_3 = new Section(3.75f*INCHES, 4f*FEET, PI/6);
        public final static Section SECTION_4 = new Section(3.75f*INCHES, 8f*FEET, PI/6);

        public final List<Branch> branches;

        public final float y;
        public final float azimuth;
        public final Size size;

        private static final float Y_BASE = -OfficeTreeModel.LIMB_HEIGHT + 5*FEET;

        public Limb(float y, float azimuth, Size size) {
            super(new Fixture(y + Y_BASE, azimuth, size));
            this.y = y + Y_BASE;
            this.azimuth = azimuth;
            this.size = size;
            Fixture f = (Fixture) this.fixtures.get(0);
            this.branches = Collections.unmodifiableList(f.branches);
        }

        private static class Fixture extends LXAbstractFixture {

            private final List<Branch> branches = new ArrayList<Branch>();

            Fixture(float y, float azimuth, Size size) {
                LXTransform t = new LXTransform();
                t.translate(0, y, 0);
                t.rotateY(HALF_PI - azimuth);
                t.rotateX(HALF_PI - PI/12);
                if (size == Size.FULL) {
                    t.translate(0, SECTION_1.len, 0);
                }
                if (size != Size.SMALL) {
                    t.translate(0, SECTION_2.len, 0);
                }
                t.rotateX(-PI/6);
                t.translate(0, SECTION_3.len);
                t.rotateX(-PI/6);

                // Branch S.2 (3)
                t.push();
                t.rotateX(PI/4);
                addBranchCluster(t, azimuth, -PI/8);
                t.pop();

                t.translate(0, SECTION_4.len);

                // Double-branch S.2 (12)
                // First part (left)
                t.push();
                t.rotateY(-PI/3);
                t.rotateX(PI/4);
                addBranchCluster(t, azimuth + PI/3, -PI/6);
                t.pop();

                // Second part (right)
                t.push();
                t.rotateY(PI/3);
                t.rotateX(PI/4);
                addBranchCluster(t, azimuth - PI/3, PI/8);
                t.pop();
            }

            private void addBranchCluster(LXTransform t, float azimuth, float baseElevation) {
                // Loose interpretation of Branch S.2 (3)
                t.translate(0, 2f*FEET, 0);
                addBranch(t, azimuth, PI/3, baseElevation);
                t.translate(0, 0.5f*FEET, 0);
                addBranch(t, azimuth, -PI/3, baseElevation);
                t.translate(0, 0.5f*FEET, 0);
                addBranch(t, azimuth, PI/3, baseElevation);
                t.translate(0, 0.5f*FEET, 0);
                addBranch(t, azimuth, -PI/3, baseElevation);
                t.translate(0, 0.5f*FEET, 0);
                addBranch(t, azimuth, PI/3, baseElevation);
            }

            private void addBranch(LXTransform t, float azimuth, float offset, float baseElevation) {
                t.push();
                t.rotateZ(-offset);
                t.translate(0, 1*FEET, 0);
                Branch branch = new Branch(new Branch.Orientation(
                    t.x(),
                    t.y(),
                    t.z(),
                    azimuth + offset,
                    baseElevation + HALF_PI * (float) Math.random(),
                    (float) (LX.TWO_PI * (float) Math.random())
                ));
                addPoints(branch);
                this.branches.add(branch);
                t.pop();
            }
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
            // Left side top to bottom
            //0
            new LeafAssemblage.Orientation(0, 36*INCHES, 0, QUARTER_PI),
            //1
            new LeafAssemblage.Orientation(RIGHT_OFFSET, 16*INCHES, RIGHT_THETA),
            //2
            new LeafAssemblage.Orientation(LEFT_OFFSET, 14*INCHES, HALF_PI, PI),
            //3
            new LeafAssemblage.Orientation(LEFT_OFFSET + 6*INCHES, 21*INCHES, QUARTER_PI*0.5f, PI),
//            new LeafAssemblage.Orientation(RIGHT_OFFSET, 2*INCHES, RIGHT_THETA, PI),
            //4
            new LeafAssemblage.Orientation(LEFT_OFFSET, 8*INCHES, HALF_PI*1.2f, PI),
            //5
            new LeafAssemblage.Orientation(RIGHT_OFFSET - FEET, 32*INCHES, RIGHT_THETA),
            //6
            new LeafAssemblage.Orientation(RIGHT_OFFSET, 30*INCHES, RIGHT_THETA),

            // End node
            //7
            new LeafAssemblage.Orientation(LEFT_OFFSET + 4*INCHES, 20*INCHES, HALF_PI, QUARTER_PI),
//            new LeafAssemblage.Orientation(0, 44*INCHES, 0),

        };
        public static final LeafAssemblage.Orientation[] ASSEMBLAGES2 = {
            //8
            new LeafAssemblage.Orientation(RIGHT_OFFSET, 2*INCHES, RIGHT_THETA),
            //9
            new LeafAssemblage.Orientation(RIGHT_OFFSET, 14*INCHES, RIGHT_THETA),
            //10
            new LeafAssemblage.Orientation(LEFT_OFFSET - 36*INCHES, 15*INCHES, -QUARTER_PI*0.5f, PI),
            //11
            new LeafAssemblage.Orientation(0, 36*INCHES, RIGHT_THETA),

            // End node
            //12
            new LeafAssemblage.Orientation(12*INCHES, 44*INCHES, QUARTER_PI),

            // Left side top to bottom
            //13
            new LeafAssemblage.Orientation(5*INCHES, 30*INCHES, 0, QUARTER_PI),
            //14
            new LeafAssemblage.Orientation(LEFT_OFFSET, 20*INCHES, LEFT_THETA),
            //15
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

            // this just used to switch the mapping for the second branch in the office.. sort of a hack.
            private static int twigIndex = 0;

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
                switch (twigIndex){
                    case 0:
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
                        break;
                    case 1:
                        for (LeafAssemblage.Orientation assemblage : ASSEMBLAGES2) {
                            t.push();
                            t.translate(assemblage.x, assemblage.y, 0);
                            t.rotateZ(assemblage.theta);
                            t.rotateY(assemblage.tilt);
                            LeafAssemblage leafAssemblage = new LeafAssemblage(channel++, t, assemblage);
                            this.assemblages.add(leafAssemblage);
                            addPoints(leafAssemblage);
                            t.pop();
                        }
                        break;
                }
                twigIndex++;
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

            Orientation(float x, float y, float theta, float tilt) {
                this.x = x;
                this.y = y;
                this.theta = theta;
                this.tilt = tilt;
            }
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
