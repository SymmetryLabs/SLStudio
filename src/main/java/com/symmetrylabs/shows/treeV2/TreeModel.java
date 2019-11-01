package com.symmetrylabs.shows.treeV2;

import java.util.*;

import heronarts.lx.parameter.StringParameter;
import heronarts.lx.transform.LXTransform;
import heronarts.lx.transform.LXVector;
import heronarts.lx.transform.LXMatrix;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXAbstractFixture;
import com.symmetrylabs.slstudio.model.SLModel;

import static com.symmetrylabs.util.DistanceConstants.*;
import static com.symmetrylabs.util.MathConstants.*;


public class TreeModel extends SLModel {

	public final List<Limb> limbs;
	public final List<Branch> branches;
	public final List<Twig> twigs;
	public final List<Leaf> leaves;

	public TreeModel(TreeConfig config) {
		super("Oslo", new Fixture(config));

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

		public final List<Limb> limbs = new ArrayList<>();

		private Fixture(TreeConfig treeConfig) {
			for (LimbConfig config : treeConfig.getLimbs()) {
				this.limbs.add(new Limb(config));
			}

			for (Limb limb : limbs) {
				for (LXPoint p : limb.points) {
					this.points.add(p);
				}
			}
		}
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

	public static class Limb extends SLModel {

		public final List<Limb> limbs;
		public final List<Branch> branches;
		public final List<Twig> twigs;
		public final List<Leaf> leaves;

		public Limb(LimbConfig config) {
			super("Limb", new Fixture(config.getLimbs(), config.getBranches()));

			Fixture f = (Fixture) this.fixtures.get(0);
			this.limbs = Collections.unmodifiableList(f.limbs);
			this.branches = Collections.unmodifiableList(f.branches);
			this.twigs = Collections.unmodifiableList(f.twigs);
			this.leaves = Collections.unmodifiableList(f.leaves);
		}

		private static class Fixture extends LXAbstractFixture {

			public final List<Limb> limbs = new ArrayList<>();
			public final List<Branch> branches = new ArrayList<>();
			public final List<Twig> twigs = new ArrayList<>();
			public final List<Leaf> leaves = new ArrayList<>();

			private Fixture(LimbConfig[] limbConfigs, BranchConfig[] branchConfigs) {
				for (LimbConfig config : limbConfigs) {
					Limb limb = new Limb(config);
					this.limbs.add(limb);

					for (Branch branch : limb.branches) {
						this.branches.add(branch);

						for (Twig twig : branch.twigs) {
							twigs.add(twig);

							for (Leaf leaf : twig.leaves) {
								leaves.add(leaf);
							}
						}
					}
				}

				for (BranchConfig config : branchConfigs) {
					Branch branch = new Branch(config);
					this.branches.add(branch);

					for (Twig twig : branch.twigs) {
						twigs.add(twig);

						for (Leaf leaf : twig.leaves) {
							leaves.add(leaf);
						}
					}
				}

				for (Twig twig : twigs) {
					for (LXPoint p : twig.points) {
						this.points.add(p);
					}
				}
			}
		}
	}

	public static class Branch extends SLModel {

		public static final int NUM_TWIGS = 8;

		private final BranchConfig config;

		private final String modelId;
		public final StringParameter controllerId;

		public final List<Twig> twigs;
		public final List<Leaf> leaves;

		public Branch(BranchConfig config) {
			super("Branch", new Fixture(config));
			this.config = config;
			this.modelId = config.getModelId();
			this.controllerId = new StringParameter("controllerID", config.getControllerId());

			Fixture f = (Fixture) this.fixtures.get(0);
			this.twigs = Collections.unmodifiableList(f.twigs);
			this.leaves = Collections.unmodifiableList(f.leaves);

			controllerId.addListener(p -> {
				config.setControllerId(controllerId.getString());
			});
		}

		public String getModelId() {
			return modelId;
		}

		public String getControllerId() {
			return  controllerId.getString();
		}

		public void setIpAddress(String controllerId) {
			this.controllerId.setValue(controllerId);
		}

		private static class Fixture extends LXAbstractFixture {

			public final List<Twig> twigs = new ArrayList<>();
			public final List<Leaf> leaves = new ArrayList<>();

			private Fixture(BranchConfig config) {
				LXTransform t = new LXTransform(config.getMatrix());

				for (int i = 0; i < NUM_TWIGS; i++) {
					t.push();
					twigs.add(new Twig(t, config.getTwigMatrix(i)));
					t.pop();
				}
			}
		}
	}

    /*
     * Twig
     *--------------------------------------------------------------*/
    public static class Twig extends SLModel {

        public static final int NUM_LEAVES = 15;
        public static final int NUM_LEDS = NUM_LEAVES * Leaf.NUM_LEDS;

        public float x;
        public float y;
        public float z;
        public int index;

        public final List<Leaf> leaves;

        // public static final Leaf.Config[] LEAVES = {
        //     new Leaf.Config(Leaf.Size.LARGE, 0, -5.0f*INCHES,  1.5f*INCHES,  95), // A
        //     new Leaf.Config(Leaf.Size.LARGE, 1, -7.0f*INCHES,  5.0f*INCHES,  75), // B
        //     new Leaf.Config(Leaf.Size.LARGE, 2, -3.5f*INCHES,  5.5f*INCHES,  25), // C
        //     new Leaf.Config(Leaf.Size.LARGE, 3, -7.0f*INCHES,  8.5f*INCHES, 105), // D
        //     new Leaf.Config(Leaf.Size.LARGE, 4, -8.0f*INCHES, 11.0f*INCHES,  60), // E
        //     new Leaf.Config(Leaf.Size.LARGE, 5, -3.5f*INCHES, 11.0f*INCHES,  35), // F
        //     new Leaf.Config(Leaf.Size.LARGE, 6, -3.0f*INCHES, 14.0f*INCHES,  45), // G
        //     new Leaf.Config(Leaf.Size.LARGE, 7, -2.5f*INCHES, 17.5f*INCHES,  0), // H (tip)
        //     null, // I
        //     null, // J
        //     null, // K
        //     null, // L
        //     null, // M
        //     null, // N
        //     null, // O
        // };

        // static {
        //   // The last eight leaves are just inverse of the first about the y-axis.
        //   for (int i = 0; i < 7; ++i) {
        //     Leaf.Config thisLeaf = LEAVES[i];
        //     int index = LEAVES.length - 1 - i;
        //     LEAVES[index] = new Leaf.Config(thisLeaf.size, index, -thisLeaf.x, thisLeaf.y, -thisLeaf.theta);
        //   }
        // }

        public Twig(LXTransform t, LXMatrix m) {
            super("Twig", new Fixture(t, m));

            Fixture f = (Fixture) this.fixtures.get(0);
            this.leaves = Collections.unmodifiableList(f.leaves);
            this.x = f.x;
            this.y = f.y;
            this.z = f.z;
        }

        public String toString() {
            return Integer.toString(index);
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

            private Fixture(LXTransform t, LXMatrix m) {
            	t.push(m);
            	this.x = t.x();
            	this.y = t.y();
            	this.z = t.z();

            	for (int i = 0; i < NUM_LEAVES; ++i) {
            		t.push(TwigConfig.getLeafMatrix(i));
            		Leaf leaf = new Leaf(t);
            		this.leaves.add(leaf);
            		addPoints(leaf);
            		t.pop();
            	}

                // for (int i = 0; i < NUM_LEAVES; ++i) {
                //   Leaf.Config leafConfig = LEAVES[i];
                //   t.push();	
                //   t.translate(leafConfig.x, leafConfig.y, (i % 3) * (.1f*INCHES));
                //   t.rotateZ(leafConfig.theta * PI / 180.);
                //   Leaf leaf = new Leaf(t, leafConfig);
                //   this.leaves.add(leaf);
                //   addPoints(leaf);
                //   t.pop();
                // }
                t.pop();
            }
        }
    }


	/*
     * Leaf
     *--------------------------------------------------------------*/
    public static class Leaf extends SLModel {

        public static final int NUM_LEDS = 10;
        public static final float LED_SPACING = 1.3f*INCHES;
        public static final float WIDTH = 4.75f*INCHES;
        public static final float LENGTH = 6.5f*INCHES;

        // public enum Size {
        //     SMALL, LARGE
        // }

        // // Config of a leaf relative to twig
        // public static class Config {

        //   public final int index;

        //   // X-Y position relative to leaf assemblage base
        //   // y-axis pointing "up" the leaf assemblage
        //   public final float x;
        //   public final float y;

        //   // Rotation about X-Y plane relative to parent assemblage
        //   public final float theta;

        //   // Tilt of the individual leaf
        //   public final float tilt;

        //   public final Leaf.Size size;

        //   Config() {
        //     this(Leaf.Size.LARGE, 0, 0, 0, 0);
        //   }

        //   Config(Leaf.Size size, int index, float x, float y, float theta) {
        //     this.index = index;
        //     this.x = x;
        //     this.y = y;
        //     this.theta = theta;
        //     this.tilt = -QUARTER_PI + HALF_PI * (float) Math.random();
        //     this.size = size;
        //   }
        // }

        // public final Config config;
        //public final Size size;
        public final LXPoint point;
        public LXVector[] coords = new LXVector[4];

        public float x;
        public float y;
        public float z;

        public Leaf(LXTransform t) {
            super("Leaf", new Fixture(t));
            Fixture f = (Fixture) this.fixtures.get(0);

            // this.config = config;
            // this.size = config.size;
            this.x = t.x();
            this.y = t.y();
            this.z = t.z();
            this.point = points[0];
            this.coords = f.coords;
        }

        private static class Fixture extends LXAbstractFixture {
            public final LXVector[] coords = new LXVector[4];

            private Fixture(LXTransform t) {
                t.push();
                t.rotateY(PI);
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
            }
        }
    }
}