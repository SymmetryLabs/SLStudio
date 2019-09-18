package com.symmetrylabs.shows.tree;

import java.util.*;
import java.lang.Integer;
import java.util.Arrays;

import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;
import heronarts.lx.transform.LXVector;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.shows.tree.config.*;
import static com.symmetrylabs.util.DistanceConstants.*;
import static com.symmetrylabs.util.MathConstants.*;


public class TreeModel extends SLModel {

    private TreeConfig config;
    public final List<Limb> limbs;
    public final List<Branch> branches;
    public final List<Twig> twigs;
    public final List<Leaf> leaves;

    private float yRotation = 0;

    public TreeModel(String showName) {
        this(showName, new TreeConfig());
    }

    public TreeModel(String showName, TreeConfig config) {
        super(showName, new Fixture(config));
        this.config = config;
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
      this.twigs = Collections.unmodifiableList(twigs);

      final List<Leaf> leaves = new ArrayList<>();
      for (Twig twig : twigs) {
          leaves.addAll(twig.leaves);
      }
      this.leaves = Collections.unmodifiableList(leaves);
    }

    private static class Fixture extends LXAbstractFixture {
        private final List<Limb> limbs = new ArrayList<>();

        private Fixture(TreeConfig config) {
            LXTransform t = new LXTransform();

            for (LimbConfig limbConfig : config.getLimbs()) {
                Limb limb = new Limb(t, limbConfig);
                limbs.add(limb);

                for (LXPoint p : limb.points) {
                    this.points.add(p);
                }
            }
        }
    }

    public void rotateY(float degrees) {
        this.yRotation = degrees;
    }

    public void reconfigure() {
        reconfigure(config);
    }

    public void reconfigure(TreeConfig config) {
        this.config = config;

        LXTransform t = new LXTransform();
        t.rotateY(yRotation * Math.PI / 180.);
        t.push();
        int i = 0;
        for (Limb limb : limbs) {
            limb.reconfigure(t, config.getLimbs().get(i++));
        }
        update(true, true);
        t.pop();
    }

    public TreeConfig getConfig() {
        return config;
    }

    public List<Limb> getLimbs() {
        return limbs;
    }
    public Limb[] getLimbsArray() {
        return limbs.toArray(new Limb[limbs.size()]);
    }

    public List<Branch> getBranches() {
        return branches;
    }
    public Branch[] getBranchesArray() {
        return branches.toArray(new Branch[branches.size()]);
    }

    public List<Twig> getTwigs() {
        return twigs;
    }
    public Twig[] getTwigsArray() {
        return twigs.toArray(new Twig[twigs.size()]);
    }

    public List<Leaf> getLeaves() {
        return leaves;
    }
    public Leaf[] getLeavesArray() {
        return leaves.toArray(new Leaf[leaves.size()]);
    }

    /**
     * Limb
     *--------------------------------------------------------------*/
    public static class Limb extends SLModel {

        private LimbConfig config;

        public float x;
        public float y;
        public float z;

        public float length;
        public float height;
        public float azimuth;
        public float elevation;
        public float tilt;

        public final List<Branch> branches;
        public final List<Twig> twigs;
        public final List<Leaf> leaves;

        public Limb(LXTransform t, LimbConfig config) {
            super("Limb", new Fixture(t, config));
            this.config = config;
            this.length = config.length;
            this.height = config.height;
            this.azimuth = config.azimuth;
            this.elevation = config.elevation;
            this.tilt = config.tilt;

            Fixture f = (Fixture) this.fixtures.get(0);
            this.branches = Collections.unmodifiableList(f.branches);
            this.twigs = Collections.unmodifiableList(f.twigs);
            this.leaves = Collections.unmodifiableList(f.leaves);
        }

        public void reconfigure(LXTransform t, LimbConfig config) {
            this.config = config;

            t.push();
            t.rotateY(config.azimuth * PI / 180.);
            t.translate(0, config.height, 0);
            t.rotateX(config.elevation * PI / 180.);
            t.rotateZ(config.tilt * PI / 180.);
            t.translate(0, config.length, 0);

            this.x = t.x();
            this.y = t.y();
            this.z = t.z();

            int i = 0;
            for (Branch branch : branches) {
                branch.reconfigure(t, config.getBranches().get(i++));
            }
            t.pop();
        }

        public LimbConfig getConfig() {
            return config;
        }

        public List<Branch> getBranches() {
            return branches;
        }
        public Branch[] getBranchesArray() {
            return branches.toArray(new Branch[branches.size()]);
        }

        public List<Twig> getTwigs() {
            return twigs;
        }
        public Twig[] getTwigsArray() {
            return twigs.toArray(new Twig[twigs.size()]);
        }

        public List<Leaf> getLeaves() {
            return leaves;
        }
        public Leaf[] getLeavesArray() {
            return leaves.toArray(new Leaf[leaves.size()]);
        }

        private static class Fixture extends LXAbstractFixture {
            private final List<Branch> branches = new ArrayList<Branch>();
            private final List<Twig> twigs = new ArrayList<Twig>();
            private final List<Leaf> leaves = new ArrayList<Leaf>();
            private final float x, y, z;

            private Fixture(LXTransform t, LimbConfig config) {
                t.push();
                t.rotateY(config.azimuth * PI / 180.);
                t.translate(0, config.height, 0);
                t.rotateX(config.elevation * PI / 180.);
                t.rotateZ(config.tilt * PI / 180.);
                t.translate(0, config.length, 0);

                this.x = t.x();
                this.y = t.y();
                this.z = t.z();

                for (BranchConfig branchConfig : config.getBranches()) {
                    Branch branch = new Branch(t, branchConfig);
                    branches.add(branch);

                    for (LXPoint p : branch.points) {
                        this.points.add(p);
                    }
                    twigs.addAll(branch.getTwigs());
                    for (Twig twig : branch.getTwigs()) {
                        leaves.addAll(twig.getLeaves());
                    }
                }

                t.pop();
            }
        }
    }

    /**
     * Branch
     *--------------------------------------------------------------*/
    public static class Branch extends SLModel {

        public static final int NUM_TWIGS = 8; // needs to be remmoved (we need to refactor patterns for arbitrary lengths

        private BranchConfig config;

        public float x;
        public float y;
        public float z;
        public float azimuth;
        public float elevation;
        public float tilt;

        public final List<Twig> twigs;
        public final List<Leaf> leaves;

        public Branch(LXTransform t, BranchConfig config) {
            super("Branch", new Fixture(t, config));
            this.config = config;
            this.azimuth = config.azimuth;
            this.elevation = config.elevation;
            this.tilt = config.tilt;

            Fixture f = (Fixture) this.fixtures.get(0);
            this.twigs = Collections.unmodifiableList(f.twigs);
            this.leaves = Collections.unmodifiableList(f.leaves);
            this.x = f.x;
            this.y = f.y;
            this.z = f.z;

        }

        public void reconfigure(LXTransform t, BranchConfig config) {
            this.config = config;

            t.push();
            t.translate(config.x, config.y, config.z);
            t.rotateX(config.elevation * PI / 180.);
            t.rotateY(config.tilt * PI / 180.);
            t.rotateZ(config.azimuth * PI / 180.);

            t.push();
            if (getConfig().flipped) {
                t.rotateY(PI);
            }
            this.x = t.x();
            this.y = t.y();
            this.z = t.z();

            int i = 0;
            for (Twig twig : twigs) {
                twig.reconfigure(t, config.getTwigs().get(i++));
            }
            t.pop();
            t.pop();
        }

        public BranchConfig getConfig() {
            return config;
        }

        public Twig getTwigByWiringIndex(int index) {
            for (Twig twig : twigs) {
                if (index == twig.getConfig().index) return twig;
            }
            return null;
        }

        public List<Twig> getTwigs() {
            return twigs;
        }
        public Twig[] getTwigsArray() {
            return twigs.toArray(new Twig[twigs.size()]);
        }

        public List<Leaf> getLeaves() {
            return leaves;
        }
        public Leaf[] getLeavesArray() {
            return leaves.toArray(new Leaf[leaves.size()]);
        }

        private static class Fixture extends LXAbstractFixture {
            private final List<Twig> twigs = new ArrayList<Twig>();
            private final List<Leaf> leaves = new ArrayList<Leaf>();
            private final float x, y, z;

            private Fixture(LXTransform t, BranchConfig config) {
                t.push();
                t.translate(config.x, config.y, config.z);
                t.rotateX(config.elevation * PI / 180.);
                t.rotateY(config.tilt * PI / 180.);
                t.rotateZ(config.azimuth * PI / 180.);

                this.x = t.x();
                this.y = t.y();
                this.z = t.z();

                for (TwigConfig twigConfig : config.getTwigs()) {
                    Twig twig = new Twig(t, twigConfig);
                    twigs.add(twig);
                    leaves.addAll(twig.getLeaves());

                    for (LXPoint p : twig.points) {
                        this.points.add(p);
                    }
                }

                t.pop();
            }
        }
    }

    /**
     * Twig
     *--------------------------------------------------------------*/
    public static class Twig extends SLModel {

        public static final int NUM_LEAVES = 15;
        public static final int NUM_LEDS = NUM_LEAVES * Leaf.NUM_LEDS;

        private TwigConfig config;

        public float x;
        public float y;
        public float z;
        public float azimuth;
        public float elevation;
        public float tilt;
        public int index;

        public final List<Leaf> leaves;

//        public static final Leaf.Config[] LEAVES = {
//          new Leaf.Config(Leaf.Size.LARGE, 0, -5.0f*INCHES,  1.5f*INCHES,  95), // A
//          new Leaf.Config(Leaf.Size.LARGE, 1, -7.0f*INCHES,  5.0f*INCHES,  75), // B
//          new Leaf.Config(Leaf.Size.SMALL, 2, -3.5f*INCHES,  5.5f*INCHES,  25), // C
//          new Leaf.Config(Leaf.Size.SMALL, 3, -7.0f*INCHES,  8.5f*INCHES, 105), // D
//          new Leaf.Config(Leaf.Size.LARGE, 4, -8.0f*INCHES, 11.0f*INCHES,  60), // E
//          new Leaf.Config(Leaf.Size.SMALL, 5, -3.5f*INCHES, 11.0f*INCHES,  35), // F
//          new Leaf.Config(Leaf.Size.LARGE, 6, -3.0f*INCHES, 14.0f*INCHES,  45), // G
//          new Leaf.Config(Leaf.Size.SMALL, 7, -2.5f*INCHES, 17.5f*INCHES,  20), // H
//          null, // I
//          null, // J
//          null, // K
//          null, // L
//          null, // M
//          null, // N
//          null, // O
//          null, // P
//        };

        public static final Leaf.Config[] LEAVES = {
            new Leaf.Config(Leaf.Size.LARGE, 0, -5.0f*INCHES,  1.5f*INCHES,  95), // A
            new Leaf.Config(Leaf.Size.LARGE, 1, -7.0f*INCHES,  5.0f*INCHES,  75), // B
            new Leaf.Config(Leaf.Size.LARGE, 2, -3.5f*INCHES,  5.5f*INCHES,  25), // C
            new Leaf.Config(Leaf.Size.LARGE, 3, -7.0f*INCHES,  8.5f*INCHES, 105), // D
            new Leaf.Config(Leaf.Size.LARGE, 4, -8.0f*INCHES, 11.0f*INCHES,  60), // E
            new Leaf.Config(Leaf.Size.LARGE, 5, -3.5f*INCHES, 11.0f*INCHES,  35), // F
            new Leaf.Config(Leaf.Size.LARGE, 6, -3.0f*INCHES, 14.0f*INCHES,  45), // G
            new Leaf.Config(Leaf.Size.LARGE, 7, -2.5f*INCHES, 17.5f*INCHES,  0), // H (tip)
            null, // I
            null, // J
            null, // K
            null, // L
            null, // M
            null, // N
            null, // O
        };

        static {
          // The last eight leaves are just inverse of the first about the y-axis.
          for (int i = 0; i < 7; ++i) {
            Leaf.Config thisLeaf = LEAVES[i];
            int index = LEAVES.length - 1 - i;
            LEAVES[index] = new Leaf.Config(thisLeaf.size, index, -thisLeaf.x, thisLeaf.y, -thisLeaf.theta);
          }
        }

        public Twig(LXTransform t, TwigConfig config) {
            super("Twig", new Fixture(t, config));
            this.config = config;
            this.azimuth = config.azimuth;
            this.elevation = config.elevation;
            this.tilt = config.tilt;
            this.index = config.index;

            Fixture f = (Fixture) this.fixtures.get(0);
            this.leaves = Collections.unmodifiableList(f.leaves);
            this.x = f.x;
            this.y = f.y;
            this.z = f.z;
        }

        public String toString() {
            return Integer.toString(index);
        }

        public void reconfigure(LXTransform t, TwigConfig config) {
            this.config = config;

            t.push();
            t.translate(config.x, config.y, config.z);
            t.rotateX(config.elevation * PI / 180.);
            t.rotateZ(config.azimuth * PI / 180.);
            t.rotateY(config.tilt * PI / 180.);

            this.x = t.x();
            this.y = t.y();
            this.z = t.z();

            for (Leaf leaf : leaves) {
              leaf.reconfigure(t);
            }

            t.pop();
        }

        public TwigConfig getConfig() {
            return config;
        }

        public List<Leaf> getLeaves() {
            return leaves;
        }
        public Leaf[] getLeavesArray() {
            return leaves.toArray(new Leaf[leaves.size()]);
        }

        public List<LXPoint> getPoints() {
            return Arrays.asList(points);
        }

        private static class Fixture extends LXAbstractFixture {
            private final List<Leaf> leaves = new ArrayList<Leaf>();
            private final float x, y, z;

            private Fixture(LXTransform t, TwigConfig config) {
                t.push();
                t.translate(config.x, config.y + 5*INCHES, config.z);
                t.rotateX(config.elevation * PI / 180.);
                t.rotateZ(config.azimuth * PI / 180.);
                t.rotateY(config.tilt * PI / 180.);

                this.x = t.x();
                this.y = t.y();
                this.z = t.z();

                for (int i = 0; i < NUM_LEAVES; ++i) {
                  Leaf.Config leafConfig = LEAVES[i];
                  t.push();
                  t.translate(leafConfig.x, leafConfig.y, (i % 3) * (.1f*INCHES));
                  t.rotateZ(leafConfig.theta * PI / 180.);
                  Leaf leaf = new Leaf(t, leafConfig);
                  this.leaves.add(leaf);
                  addPoints(leaf);
                  t.pop();
                }

                t.pop();
            }
        }
    }

    /**
     * Leaf
     *--------------------------------------------------------------*/
    public static class Leaf extends SLModel {

        public enum Size {
            SMALL, LARGE
        }

        public static final int NUM_LEDS = 10;
        public static final float LED_SPACING = 1.3f*INCHES;
        public static final float WIDTH = 4.75f*INCHES;
        public static final float LENGTH = 6.5f*INCHES;

        // Config of a leaf relative to twig
        public static class Config {

          public final int index;

          // X-Y position relative to leaf assemblage base
          // y-axis pointing "up" the leaf assemblage
          public final float x;
          public final float y;

          // Rotation about X-Y plane relative to parent assemblage
          public final float theta;

          // Tilt of the individual leaf
          public final float tilt;

          public final Leaf.Size size;

          Config() {
            this(Leaf.Size.LARGE, 0, 0, 0, 0);
          }

          Config(Leaf.Size size, int index, float x, float y, float theta) {
            this.index = index;
            this.x = x;
            this.y = y;
            this.theta = theta;
            this.tilt = -QUARTER_PI + HALF_PI * (float) Math.random();
            this.size = size;
          }
        }

        public final Config config;
        public final Size size;
        public final LXPoint point;
        public LXVector[] coords = new LXVector[4];

        public float x;
        public float y;
        public float z;

        public Leaf(LXTransform t) {
            this(t, new Config());
        }

        public Leaf(LXTransform t, Config config) {
            super("Leaf", new Fixture(t, config));
            Fixture f = (Fixture) this.fixtures.get(0);

            this.config = config;
            this.size = config.size;
            this.x = t.x();
            this.y = t.y();
            this.z = t.z();
            this.point = points[0];
            this.coords = f.coords;
        }

        public void reconfigure(LXTransform t) {
            this.x = t.x();
            this.y = t.y();
            this.z = t.z();

            t.push();
            t.translate(config.x, config.y, 0);
            t.rotateZ(config.theta * PI / 180.);
            t.rotateY(config.tilt * PI / 180.);

            int i = 0;
            if (config.size == Size.LARGE) {
                t.translate(-.05f*INCHES, 0, 0);
                points[i++].update(t.x(), t.y(), t.z());
                t.translate(0, LED_SPACING, 0);
                points[i++].update(t.x(), t.y(), t.z());
                t.translate(0, LED_SPACING, 0);
                points[i++].update(t.x(), t.y(), t.z());
                t.translate(0, LED_SPACING, 0);
                points[i++].update(t.x(), t.y(), t.z());
                t.translate(0, LED_SPACING, 0);
                points[i++].update(t.x(), t.y(), t.z());

                t.translate(.1f*INCHES, 0, 0);
                points[i++].update(t.x(), t.y(), t.z());
                t.translate(0, -LED_SPACING, 0);
                points[i++].update(t.x(), t.y(), t.z());
                t.translate(0, -LED_SPACING, 0);
                points[i++].update(t.x(), t.y(), t.z());
                t.translate(0, -LED_SPACING, 0);
                points[i++].update(t.x(), t.y(), t.z());
                t.translate(0, -LED_SPACING, 0);
                points[i++].update(t.x(), t.y(), t.z());
            }
            t.pop();

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
//            else if (config.size == Size.SMALL) {
//                t.translate(-.05f*INCHES, 0, 0);
//                points[i++].update(t.x(), t.y(), t.z());
//                t.translate(0, LED_SPACING, 0);
//                points[i++].update(t.x(), t.y(), t.z());
//                t.translate(0, LED_SPACING, 0);
//                points[i++].update(t.x(), t.y(), t.z());
//
//                t.translate(.1f*INCHES, 0, 0);
//                points[i++].update(t.x(), t.y(), t.z());
//                t.translate(0, -LED_SPACING, 0);
//                points[i++].update(t.x(), t.y(), t.z());
//                t.translate(0, -LED_SPACING, 0);
//                points[i++].update(t.x(), t.y(), t.z());
//            }
        }

        private static class Fixture extends LXAbstractFixture {
            public final LXVector[] coords = new LXVector[4];

            private Fixture(LXTransform t, Config config) {
                t.push();
                if (config.size == Size.LARGE) {
                    t.translate(-.05f*INCHES, 0, 0);
                    addPoint(new LXPoint(t));
                    t.translate(0, LED_SPACING, 0);
                    addPoint(new LXPoint(t));
                    t.translate(0, LED_SPACING, 0);
                    addPoint(new LXPoint(t));
                    t.translate(0, LED_SPACING, 0);
                    addPoint(new LXPoint(t));
                    t.translate(0, LED_SPACING, 0);
                    addPoint(new LXPoint(t));

                    t.translate(.1f*INCHES, 0, 0);
                    addPoint(new LXPoint(t));
                    t.translate(0, -LED_SPACING, 0);
                    addPoint(new LXPoint(t));
                    t.translate(0, -LED_SPACING, 0);
                    addPoint(new LXPoint(t));
                    t.translate(0, -LED_SPACING, 0);
                    addPoint(new LXPoint(t));
                    t.translate(0, -LED_SPACING, 0);
                    addPoint(new LXPoint(t));
                }
                t.pop();

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
//                else if (config.size == Size.SMALL) {
//                    t.translate(-.05f*INCHES, 0, 0);
//                    addPoint(new LXPoint(t));
//                    t.translate(0, LED_SPACING, 0);
//                    addPoint(new LXPoint(t));
//                    t.translate(0, LED_SPACING, 0);
//                    addPoint(new LXPoint(t));
//
//                    t.translate(.1f*INCHES, 0, 0);
//                    addPoint(new LXPoint(t));
//                    t.translate(0, -LED_SPACING, 0);
//                    addPoint(new LXPoint(t));
//                    t.translate(0, -LED_SPACING, 0);
//                    addPoint(new LXPoint(t));
//                }
            }
        }
    }
}
