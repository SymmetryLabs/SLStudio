package com.symmetrylabs.shows.empirewall;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;

import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.slstudio.model.Strip;


public class EmpireWallStripSelect extends SLPattern {
	public static final String GROUP_NAME = EmpireWallShow.SHOW_NAME;

	final VineModel model;
	final DiscreteParameter selectedStrip;

	public EmpireWallStripSelect(LX lx) {
		super(lx);
		this.model = (VineModel) lx.model;
		this.selectedStrip = new DiscreteParameter("strip", 0, 0, model.strips.size());
		addParameter(selectedStrip);
	}

	public void run(double deltaMs) {
		setColors(0);

		Strip strip = model.strips.get(selectedStrip.getValuei());

		for (LXPoint p : strip.points) {
			colors[p.index] = LXColor.GREEN;
		}
	}
}