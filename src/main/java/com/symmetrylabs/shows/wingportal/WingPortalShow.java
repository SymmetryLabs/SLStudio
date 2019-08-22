package com.symmetrylabs.shows.wingportal;

import heronarts.lx.LX;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.shows.Show;


public class WingPortalShow implements Show {
	public final static String SHOW_NAME = "wingportal";

	public SLModel buildModel() {
		return new WingPortalModel();
	}

	public void setupLx(final LX lx) {
		// pixlite yada yada
	}
}