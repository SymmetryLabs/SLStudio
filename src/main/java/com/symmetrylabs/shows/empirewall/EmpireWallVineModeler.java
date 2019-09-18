package com.symmetrylabs.shows.empirewall;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;

import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.shows.tree.TreeModel;


public class EmpireWallVineModeler extends SLPattern {
	public static final String GROUP_NAME = EmpireWallShow.SHOW_NAME;

	final VineModel model;

	final VineWallModelingTool modeler;

	public EmpireWallVineModeler(LX lx) {
		super(lx);
		this.model = (VineModel) lx.model;
		this.modeler = VineWallModelingTool.getInstance(lx);
	}

	public void run(double deltaMs) {
		setColors(0);

		VineModel.Vine vine = model.vines.get(modeler.selectedVine.getValuei());

		for (LXPoint p : vine.points) {
			colors[p.index] = LXColor.RED;
		}

		int leafIndex = modeler.selectedLeaves[modeler.selectedVine.getValuei()].getValuei();

		int i = 0;
		for (TreeModel.Leaf leaf : vine.leaves) {
			if (i == leafIndex) {
				for (LXPoint p : leaf.points) {
					colors[p.index] = LXColor.GREEN;
				}
			}
			if (i < leafIndex) {
				for (LXPoint p : leaf.points) {
					colors[p.index] = LXColor.BLUE;
				}
			}
			i++;
		}
	}
}