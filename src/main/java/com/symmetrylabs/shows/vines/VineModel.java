package com.symmetrylabs.shows.vines;

import java.util.*;

import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.shows.tree.TreeModel;
import static com.symmetrylabs.util.MathConstants.*;


public class VineModel extends SLModel {
	
	public final List<Vine> vines;
	public final List<TreeModel.Leaf> leaves;

	public VineModel(String modelId, VineConfig[] vineConfigs) {
		super(modelId, new Fixture(Arrays.asList(vineConfigs)));

		Fixture f = (Fixture) this.fixtures.get(0);
		this.vines  = Collections.unmodifiableList(f.vines);
		this.leaves = Collections.unmodifiableList(f.leaves);
	}

	private static class Fixture extends LXAbstractFixture {
		private final List<Vine> vines = new ArrayList<>();
		private final List<TreeModel.Leaf> leaves = new ArrayList<>();

		private Fixture(List<VineConfig> vineConfigs) {
		    int vineIndex = 1;
			for (VineConfig vineConfig : vineConfigs) {
				Vine vine = new Vine(String.format("Vine %d", vineIndex), vineConfig);
				vines.add(vine);
                leaves.addAll(vine.leaves);
                points.addAll(Arrays.asList(vine.points));
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
