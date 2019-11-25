package com.symmetrylabs.shows.banyan;

import java.util.*;

import com.symmetrylabs.slstudio.model.banyan.Panel;
import com.symmetrylabs.slstudio.model.banyan.InsideShardPanel;
import com.symmetrylabs.slstudio.model.banyan.TipShardPanel;
import heronarts.lx.transform.LXTransform;
import heronarts.lx.model.LXAbstractFixture;

import com.symmetrylabs.shows.tree.*;
import com.symmetrylabs.shows.tree.config.*;
import com.symmetrylabs.slstudio.model.SLModel;
import static com.symmetrylabs.util.MathConstants.*;


public class BanyanModel extends TreeModel {

	public static Star star;

	public BanyanModel(String showName, TreeConfig treeConfig, Star.Config starConfig) {
		super(showName, treeConfig);
	}

	public static class Star extends SLModel {
		public List<Panel> panels = new ArrayList<Panel>();

        public List<Panel> innerPanels = new ArrayList<Panel>();
        public List<Panel> outerPanels = new ArrayList<Panel>();

		public Star(Star.Config config) {
			super("star", new Fixture(config));
			BanyanModel.star = this;
			Fixture f = (Fixture) this.fixtures.get(0);
			this.panels.addAll(f.panels);
			this.innerPanels.addAll(f.innerPanels);
			this.outerPanels.addAll(f.outerPanels);
		}

		public static class Fixture extends LXAbstractFixture {
            private static final int NUM_SYMMETRY = 8;
            public List<Panel> panels = new ArrayList<Panel>();
            public List<Panel> innerPanels = new ArrayList<Panel>();
            public List<Panel> outerPanels = new ArrayList<Panel>();

			public Fixture(BanyanModel.Star.Config config) {
				LXTransform t = new LXTransform();
				t.translate(config.x, config.y, config.z);
				t.rotateY(config.yRot * PI / 180.);
				//...
                for (int i = 0; i < NUM_SYMMETRY; i++){
                    t.rotateZ((-PI/4));
                    t.push();
                    t.translate(-5, 2, 0);
                    InsideShardPanel shard = new InsideShardPanel("tip", t);
                    this.panels.add(shard);
                    this.innerPanels.add(shard);
                    t.rotateZ(PI);
                    t.rotateZ(-PI/8);
                    t.translate(-14, -34, 0);
//                    t.translate(0, -10, 0);
                    TipShardPanel tipper = new TipShardPanel("tipper", t);
                    this.panels.add(tipper);
                    this.outerPanels.add(tipper);
                    points.addAll(shard.getPoints());
                    points.addAll(tipper.getPoints());
                    t.pop();
                }
			}
		}
	
		public static class Config {
			float x, y, z, yRot;

			public Config(float x, float y, float z, float yRot) {
				this.x = x;
				this.y = y;
				this.z = z;
				this.yRot = yRot;
			}
		}
	}
}
