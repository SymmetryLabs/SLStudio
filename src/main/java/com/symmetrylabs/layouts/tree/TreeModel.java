package com.symmetrylabs.layouts.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import processing.core.PApplet;
import processing.data.JSONArray;
import processing.data.JSONObject;
import static processing.core.PApplet.*;

import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXMatrix;
import heronarts.lx.transform.LXTransform;
import heronarts.lx.transform.LXVector;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.layouts.tree.config.*;
import static com.symmetrylabs.util.DistanceConstants.*;


public class TreeModel extends SLModel {

    private TreeConfig config;
    public final List<Limb> limbs;
    public final List<Branch> branches;
    public final List<Twig> twigs;
    public final List<Leaf> leaves;

    public TreeModel(TreeConfig config) {
        super(new Fixture(config));
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
        private final List<Limb> limbs = new ArrayList<Limb>();

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

    public void reconfigure(TreeConfig config) {
        LXTransform t = new LXTransform();

        int i = 0;
        for (Limb limb : limbs) {
            limb.reconfigure(t, config.getLimbs().get(i++));
        }
    }

    public TreeConfig getConfig() {
        return config; // (todo) return copy instead?
    }

    /**
     * Limb
     *--------------------------------------------------------------*/
    public static class Limb extends SLModel {

        public final LimbConfig config;

        public float x;
        public float y;
        public float z;

        public float height;
        public float azimuth;
        public float elevation;

        public final List<Branch> branches;
        public final List<Twig> twigs;
        public final List<Leaf> leaves;

        public Limb(LXTransform t, LimbConfig config) {
            super(new Fixture(t, config));
            this.config = config;
            this.height = config.height;
            this.azimuth = config.azimuth;
            this.elevation = elevation;

            Fixture f = (Fixture) this.fixtures.get(0);
            this.branches = Collections.unmodifiableList(f.branches);
            this.twigs    = Collections.unmodifiableList(f.twigs);
            this.leaves   = Collections.unmodifiableList(f.leaves);
        }

        public void reconfigure(LXTransform t, LimbConfig config) {
            t.push();
            t.translate(0, config.height, 0);
            t.rotateY(config.azimuth * PI / 180.);
            t.rotateZ(config.elevation * PI / 180.);

            this.x = t.x();
            this.y = t.y();
            this.z = t.z();

            int i = 0;
            for (Branch branch : branches) {
                branch.reconfigure(t, config.getBranches().get(i++));
            }
            t.pop();
        }

        private static class Fixture extends LXAbstractFixture {
            private final List<Branch> branches = new ArrayList<Branch>();
            private final List<Twig> twigs = new ArrayList<Twig>();
            private final List<Leaf> leaves = new ArrayList<Leaf>();

            private Fixture(LXTransform t, LimbConfig config) {
                t.push();
                t.translate(0, config.height, 0);
                t.rotateY(config.azimuth * PI / 180.);
                t.rotateZ(config.elevation * PI / 180.);

                this.x = t.x();
                this.y = t.y();
                this.z = t.z();

                for (BranchConfig branchConfig : config.getBranches()) {
                    Branch branch = new Branch(t, branchConfig);
                    branches.add(branch);

                    for (LXPoint p : branch.points) {
                        this.points.add(p);
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

        public BranchConfig config;

        public float x;
        public float y;
        public float z;
        public float azimuth;
        public float elevation;
        public float tilt;

        public final List<Twig> twigs;
        public final List<Leaf> leaves;

        public Branch(LXTransform t, BranchConfig config) {
            super(new Fixture(t, config));
            this.config = config;
            this.azimuth = config.azimuth;
            this.elevation = config.elevation;
            this.tilt = config.tilt;

            Fixture f = (Fixture) this.fixtures.get(0);
            this.twigs   = Collections.unmodifiableList(f.twigs);
            this.leaves  = Collections.unmodifiableList(f.leaves);
        }

        public void reconfigure(LXTransform t, BranchConfig config) {
            t.push();
            t.translate(config.x, config.y, config.z);
            t.rotateX(config.tilt * PI / 180.);
            t.rotateY(config.azimuth * PI / 180.);
            t.rotateZ(config.elevation * PI / 180.);

            this.x = t.x();
            this.y = t.y();
            this.z = t.z();

            int i = 0;
            for (Twig twig : twigs) {
                twig.reconfigure(t, config.getTwigs().get(i++));
            }
            t.pop();
        }

        private static class Fixture extends LXAbstractFixture {
            private final List<Twig> twigs = new ArrayList<Twig>();
            private final List<Leaf> leaves = new ArrayList<Leaf>();

            private Fixture(LXTransform t, BranchConfig config) {
                t.push();
                t.translate(config.x, config.y, config.z);
                t.rotateX(config.tilt * PI / 180.);
                t.rotateY(config.azimuth * PI / 180.);
                t.rotateZ(config.elevation * PI / 180.);

                this.x = t.x();
                this.y = t.y();
                this.z = t.z();

                for (TwigConfig twigConfig : config.getTwigs()) {
                    Twig twig = new Twig(t, twigConfig);
                    twigs.add(twig);

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

        public TwigConfig config;

        public float x;
        public float y;
        public float z;
        public float theta;
        public float tilt;

        public final List<Leaf> leaves;

        public static final Leaf.Orientation[] LEAVES = {
          new Leaf.Orientation(0,  4.5f*INCHES, -1.7f*INCHES, -HALF_PI - QUARTER_PI), // A
          new Leaf.Orientation(1,  5.5f*INCHES,    0f*INCHES, -HALF_PI), // B
          new Leaf.Orientation(2,  2.0f*INCHES,  3.5f*INCHES, -HALF_PI + QUARTER_PI), // C
          new Leaf.Orientation(3,  3.5f*INCHES,  7.5f*INCHES, -HALF_PI), // D
          new Leaf.Orientation(4,  4.0f*INCHES, 11.2f*INCHES, -HALF_PI), // E
          new Leaf.Orientation(5,  3.0f*INCHES,  9.5f*INCHES, -HALF_PI + QUARTER_PI), // F
          new Leaf.Orientation(6,  3.5f*INCHES, 12.7f*INCHES, -HALF_PI + QUARTER_PI), // G
          new Leaf.Orientation(7,  0.0f*INCHES, 13.5f*INCHES, 0), // H
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

        public Twig(LXTransform t, TwigConfig config) {
            super(new Fixture(t, config));
            this.config = config;
            this.x = config.x;
            this.y = config.y;
            this.z = config.z;
            this.theta = theta;
            this.tilt = tilt;

            Fixture f = (Fixture) this.fixtures.get(0);
            this.leaves = Collections.unmodifiableList(f.leaves);
        }

        public void reconfigure(LXTransform t, TwigConfig config) {
            t.push();
            t.translate(config.x, config.y, config.z);
            t.rotateY(config.tilt * PI / 180.);
            t.rotateZ(config.theta * PI / 180.);

            for (Leaf leaf : leaves) {
              leaf.reconfigure(t);
            }

            t.pop();
        }

        private static class Fixture extends LXAbstractFixture {
            private final List<Leaf> leaves = new ArrayList<Leaf>();

            private Fixture(LXTransform t, TwigConfig config) {
                t.push();
                t.translate(config.x, config.y + 5*INCHES, config.z);
                t.rotateY(config.tilt * PI / 180.);
                t.rotateZ(config.theta * PI / 180.);

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
     * Leaf
     *--------------------------------------------------------------*/
    public static class Leaf extends SLModel {
        public static final int NUM_LEDS = 7;
        public static final float LED_OFFSET = .75f*INCHES;
        public static final float LED_SPACING = 1.3f*INCHES;
        public static final float WIDTH = 4.75f*INCHES;
        public static final float LENGTH = 6.5f*INCHES;

        // Orientation of a leaf relative to twig
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

        public final Orientation orientation;

        public float x;
        public float y;
        public float z;

        public Leaf(LXTransform t, Orientation orientation) {
            super(new Fixture(t));
            this.orientation = orientation;
            this.x = t.x();
            this.y = t.y();
            this.z = t.z();
        }

        public void reconfigure(LXTransform t) {
            this.x = t.x();
            this.y = t.y();
            this.z = t.z();

            t.push();
            t.translate(orientation.x, orientation.y, 0);
            t.rotateZ(orientation.theta);
            t.rotateY(orientation.tilt);

            t.translate(.1f*INCHES, LED_OFFSET, 0);
            points[0].update(t.x(), t.y(), t.z());
            t.translate(0, LED_SPACING, 0);
            points[1].update(t.x(), t.y(), t.z());
            t.translate(0, LED_SPACING, 0);
            points[2].update(t.x(), t.y(), t.z());
            t.translate(-.1f*INCHES, LED_SPACING, 0);
            points[3].update(t.x(), t.y(), t.z());
            t.translate(-.1f*INCHES, -LED_SPACING, 0);
            points[4].update(t.x(), t.y(), t.z());
            t.translate(0, -LED_SPACING, 0);
            points[5].update(t.x(), t.y(), t.z());
            t.translate(0, -LED_SPACING, 0);
            points[6].update(t.x(), t.y(), t.z());
            t.pop();
        }

        private static class Fixture extends LXAbstractFixture {
            private Fixture(LXTransform t) {
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
}
