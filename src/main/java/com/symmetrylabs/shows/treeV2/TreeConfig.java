package com.symmetrylabs.shows.treeV2;

import java.util.List;
import java.util.ArrayList;


public class TreeConfig {
	private final LimbConfig[] limbs;

	public TreeConfig(LimbConfig[] limbs) {
		this.limbs = limbs;
	}

	public LimbConfig[] getLimbs() {
		return limbs;
	}
}