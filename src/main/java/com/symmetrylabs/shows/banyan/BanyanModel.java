package com.symmetrylabs.shows.banyan;

import java.util.*;
import java.lang.Integer;
import java.util.Arrays;

import heronarts.lx.transform.LXTransform;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.EmptyFixture;

import com.symmetrylabs.shows.tree.*;
import com.symmetrylabs.shows.tree.config.*;
import com.symmetrylabs.slstudio.model.SLModel;
import static com.symmetrylabs.util.MathConstants.*;


public class BanyanModel extends TreeModel {

	public Star star;

	public BanyanModel(String showName, TreeConfig treeConfig, Star.Config starConfig) {
		super(showName, treeConfig, new Star(starConfig));

	}

	public static class Star extends SLModel {
		public List<Panel> panels = new ArrayList<Panel>();

		public Star(Star.Config config) {
			super("star", new Fixture(config));
			//...
		}

		public static class Fixture extends LXAbstractFixture {
			public List<Panel> panels = new ArrayList<Panel>();

			public Fixture(BanyanModel.Star.Config config) {
				LXTransform t = new LXTransform();
				t.translate(config.x, config.y, config.z);
				t.rotateY(config.yRot * PI / 180.);
				//...
			}
		}
	
		public static class Panel extends SLModel {

			public Panel() {
				super("star panel", new EmptyFixture()); // temp, just to get it to build
				//...
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