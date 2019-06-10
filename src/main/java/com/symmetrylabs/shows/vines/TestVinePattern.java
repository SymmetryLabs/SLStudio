package com.symmetrylabs.shows.vines;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.shows.vines.VineModel;
import com.symmetrylabs.shows.tree.TreeModel;


public class TestVinePattern extends LXPattern {

	private final VineModel model;
	public final BooleanParameter selectAllVines;
	public final CompoundParameter selectedVine;
	
	public TestVinePattern(LX lx) {
		super(lx);
		this.model = (VineModel) lx.model;
		this.selectAllVines = new BooleanParameter("select all", true);
		this.selectedVine = new CompoundParameter("select vine", 1, 1, model.vines.size());

		addParameter(selectAllVines);
		addParameter(selectedVine);
	}

	public void run(double deltaMs) {
		setColors(0);
		
		if (selectAllVines.isOn()) {
			int hue = 0;
			for (VineModel.Vine vine : model.vines) {
				for (LXPoint p : vine.points) {
					colors[p.index] = lx.hsb(hue, 100, 100);
				}
				hue += 100;
			}
	
			colors[0] = 0;
		} else {
			VineModel.Vine vine = model.vines.get((int)(selectedVine.getValuef() - 1));

			int hue = 0;
			for (TreeModel.Leaf leaf : vine.leaves) {
				for (LXPoint p : leaf.points) {
					colors[p.index] = lx.hsb(hue, 100, 100);
				}
				hue += 100;
			}
			
			colors[0] = 0;
		}
	}
}