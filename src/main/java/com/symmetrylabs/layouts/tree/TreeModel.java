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
            for (LimbConfig limbConfig : config.getLimbs()) {
                // create a limb!
            }
        }
    }

    public void reconfigure(TreeConfig config) {
        // todo
    }

    public TreeConfig getConfig() {
        return config; // (todo) return copy instead?
    }

    /**
     * Limb
     *--------------------------------------------------------------*/
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
            this.twigs    = Collections.unmodifiableList(f.twigs);
            this.leaves   = Collections.unmodifiableList(f.leaves);
        }

        private static class Fixture extends LXAbstractFixture {
            private final List<Branch> branches = new ArrayList<Branch>();
            private final List<Twig> twigs = new ArrayList<Twig>();
            private final List<Leaf> leaves = new ArrayList<Leaf>();

            private Fixture(LXTransform t, LimbConfig config) {

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
            this.x = config.x;
            this.y = config.y;
            this.z = config.z;
            this.azimuth = config.azimuth;
            this.elevation = config.elevation;
            this.tilt = config.tilt;

            Fixture f = (Fixture) this.fixtures.get(0);
            this.twigs   = Collections.unmodifiableList(f.twigs);
            this.leaves  = Collections.unmodifiableList(f.leaves);
        }

        private static class Fixture extends LXAbstractFixture {
            private final List<Twig> twigs = new ArrayList<Twig>();
            private final List<Leaf> leaves = new ArrayList<Leaf>();

            private Fixture(LXTransform t, BranchConfig config) {
                
            }
        }
    }

    /**
     * Twig
     *--------------------------------------------------------------*/
    public static class Twig extends SLModel {

        public TwigConfig config;

        public float x;
        public float y;
        public float z;
        public float theta;
        public float tilt;

        public final List<Leaf> leaves;

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

        private static class Fixture extends LXAbstractFixture {
            private final List<Leaf> leaves = new ArrayList<Leaf>();

            private Fixture(LXTransform t, TwigConfig config) {
                
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

        public final float x;
        public final float y;
        public final float z;

        public Leaf(LXTransform t) {
            super(new Fixture(t));
            this.x = t.x();
            this.y = t.y();
            this.z = t.z();
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
