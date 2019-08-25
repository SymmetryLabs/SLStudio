package com.symmetrylabs.shows.wingportal;

import heronarts.lx.LX;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;


public class WingPortalShow implements Show {
	public final static String SHOW_NAME = "wingportal";

	public SLModel buildModel() {
		return new WingPortalModel();
	}

	public void setupLx(final LX lx) {
		// pixlite yada yada

		WingPortalModel model = (WingPortalModel) lx.model;

		SimplePixlite pix1 = new SimplePixlite(lx, "192.168.0.50");
		pix1.addPixliteOutput((new PointsGrouping("1")).addPoints(model.strips.get(13).points));
		pix1.addPixliteOutput((new PointsGrouping("2")).addPoints(model.strips.get(12).points));
		pix1.addPixliteOutput((new PointsGrouping("3")).addPoints(model.strips.get(11).points));
		pix1.addPixliteOutput((new PointsGrouping("4")).addPoints(model.strips.get(10).points));
		pix1.addPixliteOutput((new PointsGrouping("5")).addPoints(model.strips.get(9).points));
		pix1.addPixliteOutput((new PointsGrouping("6")).addPoints(model.strips.get(8).points));
		pix1.addPixliteOutput((new PointsGrouping("7")).addPoints(model.strips.get(7).points));
		
		pix1.addPixliteOutput((new PointsGrouping("9")).addPoints(model.strips.get(14).points));
		pix1.addPixliteOutput((new PointsGrouping("10")).addPoints(model.strips.get(15).points));
		pix1.addPixliteOutput((new PointsGrouping("11")).addPoints(model.strips.get(16).points));
		pix1.addPixliteOutput((new PointsGrouping("12")).addPoints(model.strips.get(17).points));
		pix1.addPixliteOutput((new PointsGrouping("13")).addPoints(model.strips.get(18).points));
		pix1.addPixliteOutput((new PointsGrouping("14")).addPoints(model.strips.get(19).points));
		pix1.addPixliteOutput((new PointsGrouping("15")).addPoints(model.strips.get(20).points));
		pix1.enabled.setValue(true);
		lx.addOutput(pix1);

		// SimplePixlite pix2 = new SimplePixlite(lx, "192.168.0.51");

		// pix2.enabled.setValue(true);
		// lx.addOutput(pix2);	
	}
}