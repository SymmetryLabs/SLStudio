package com.symmetrylabs.shows.empirewall;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;

import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.slstudio.model.Strip;


public class EmpireWallStripLength extends SLPattern {
	public static final String GROUP_NAME = EmpireWallShow.SHOW_NAME;

	final VineModel model;
	final DiscreteParameter selectedStrip;
	final DiscreteParameter selectedPoint;

	public EmpireWallStripLength(LX lx) {
		super(lx);
		this.model = (VineModel) lx.model;
		this.selectedStrip = new DiscreteParameter("strip", 0, 0, model.strips.size());
		this.selectedPoint = new DiscreteParameter("point", 0, 0, 130);
		addParameter(selectedStrip);
		addParameter(selectedPoint);
	}

	public void run(double deltaMs) {
		setColors(0);

		Strip strip = model.strips.get(selectedStrip.getValuei());

		if (strip.points.length > selectedPoint.getValuei()) {
			LXPoint p = strip.points[selectedPoint.getValuei()];
			colors[p.index] = LXColor.GREEN;
		}
	}
}