package com.symmetrylabs.shows.vines;

import java.util.*;

import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.shows.tree.TreeModel;
import com.symmetrylabs.slstudio.model.Strip;
import static com.symmetrylabs.util.MathConstants.*;


public class VineModel extends SLModel {
	
	public final List<Vine> vines;
	public final List<Strip> strips;
	public final List<TreeModel.Leaf> leaves;

	public VineModel(String modelId, List<Vine> vines, List<Strip> strips) {
		super(modelId, new Fixture(vines, strips));

		Fixture f = (Fixture) this.fixtures.get(0);
		this.vines   = Collections.unmodifiableList(f.vines);
		this.strips  = Collections.unmodifiableList(f.strips);
		this.leaves  = Collections.unmodifiableList(f.leaves);
	}

	private static class Fixture extends LXAbstractFixture {
		private final List<Vine> vines = new ArrayList<>();
		private final List<Strip> strips = new ArrayList<>();
		private final List<TreeModel.Leaf> leaves = new ArrayList<>();

		private Fixture(List<Vine> vines, List<Strip> strips) {
			for (Vine vine : vines) {
				this.vines.add(vine);
                this.leaves.addAll(vine.leaves);
                //points.addAll(Arrays.asList(vine.points));

                for (LXPoint p : vine.points) {
                	this.points.add(p);
                }
			}

			for (Strip strip : strips) {
				this.strips.add(strip);

				for (LXPoint p : strip.points) {
                	this.points.add(p);
                }
			}
		}
	}

	public static class Vine extends SLModel {

		public final List<TreeModel.Leaf> leaves;

		public Vine(String modelId, VineConfig vineConfig) {
			super(modelId, new Fixture(vineConfig));

			Fixture f = (Fixture) this.fixtures.get(0);
			this.leaves = Collections.unmodifiableList(f.leaves);
		}

		private static class Fixture extends LXAbstractFixture {
			private final List<TreeModel.Leaf> leaves = new ArrayList<>();

			private Fixture(VineConfig config) {
				LXTransform transform = new LXTransform();

				List<TreeModel.Leaf> vineLeaves = new ArrayList<>();
				for (LeafConfig leafConfig : config.leaves) {
					transform.push();
					transform.translate(leafConfig.xPos, leafConfig.yPos, leafConfig.zPos);
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
