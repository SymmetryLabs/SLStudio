package com.symmetrylabs.shows.empirewall;

import java.util.*;

import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.shows.tree.TreeModel;
import com.symmetrylabs.shows.empirewall.config.*;
import com.symmetrylabs.slstudio.model.Strip;
import static com.symmetrylabs.util.MathConstants.*;


public class VineModel extends SLModel {

	private VineWallConfig config;
	
	public final List<Vine> vines;
	public final List<Strip> strips;
	public final List<TreeModel.Leaf> leaves;

	public VineModel(String modelId, VineWallConfig vineWallConfig, List<Strip> strips) {
		super(modelId, new Fixture(vineWallConfig.getVinesArray(), strips));
		this.config = vineWallConfig;

		Fixture f = (Fixture) this.fixtures.get(0);
		this.vines   = Collections.unmodifiableList(f.vines);
		this.strips  = Collections.unmodifiableList(f.strips);
		this.leaves  = Collections.unmodifiableList(f.leaves);
	}

	private static class Fixture extends LXAbstractFixture {
		private final List<Vine> vines = new ArrayList<>();
		private final List<Strip> strips = new ArrayList<>();
		private final List<TreeModel.Leaf> leaves = new ArrayList<>();

		private Fixture(VineConfig[] vinesConfig, List<Strip> strips) {
			for (VineConfig config : vinesConfig) {
				Vine vine = new Vine(config.id, config);
				this.vines.add(vine);
				this.leaves.addAll(vine.leaves);
				this.points.addAll(Arrays.asList(vine.points));
			}

			for (Strip strip : strips) {
				this.strips.add(strip);

				for (LXPoint p : strip.points) {
                	this.points.add(p);
                }
			}
		}
	}

	public void reconfigure() {
		reconfigure(config);
	}

	public void reconfigure(VineWallConfig config) {
		this.config = config;
		System.out.println("***************");
		int i = 0;
		for (Vine vine : vines) {
			vine.reconfigure(config.getVines().get(i++));
		}
		update(true, true);
	}

	public VineWallConfig getConfig() {
		return config;
	}

	public Vine[] getVinesArray() {
		return vines.toArray(new Vine[vines.size()]);
	}

	public static class Vine extends SLModel {

		public VineConfig config;

		public final List<TreeModel.Leaf> leaves;

		public Vine(String modelId, VineConfig vineConfig) {
			super(modelId, new Fixture(vineConfig));

			this.config = vineConfig;

			Fixture f = (Fixture) this.fixtures.get(0);
			this.leaves = Collections.unmodifiableList(f.leaves);
		}

		public void reconfigure(VineConfig config) {
			this.config = config;

			LXTransform t = new LXTransform();
			int leafIndex = 0;
			System.out.println("----------");
			for (TreeModel.Leaf leaf : leaves) {
				LeafConfig leafConfig = config.getLeaves().get(leafIndex++);
				System.out.println(leafConfig.index);
				t.push();
				t.translate(leafConfig.x, leafConfig.y, -leafConfig.z);
				t.rotateX(leafConfig.xRot * PI / 180.f);
				t.rotateY(leafConfig.yRot * PI / 180.f);
				t.rotateZ(leafConfig.zRot * PI / 180.f);

				leaf.reconfigure(t);
				t.pop();
			}
		}

		public TreeModel.Leaf[] getLeavesArray() {
			return leaves.toArray(new TreeModel.Leaf[leaves.size()]);
		}

		public VineConfig getConfig() {
			return config;
		}

		private static class Fixture extends LXAbstractFixture {
			private final List<TreeModel.Leaf> leaves = new ArrayList<>();

			private Fixture(VineConfig config) {
				LXTransform transform = new LXTransform();

				List<TreeModel.Leaf> vineLeaves = new ArrayList<>();
				for (LeafConfig leafConfig : config.getLeaves()) {
					transform.push();
					transform.translate(leafConfig.x, leafConfig.y, -leafConfig.z);
					transform.rotateX(leafConfig.xRot * PI / 180.);
					transform.rotateY(leafConfig.yRot * PI / 180.);
					transform.rotateZ(leafConfig.zRot * PI / 180.);

					TreeModel.Leaf leaf = new TreeModel.Leaf(transform);
					this.leaves.add(leaf);

					for (LXPoint point : leaf.points) {
						this.points.add(point);
					}

					transform.pop();
				}
			}
		}
	}
}
