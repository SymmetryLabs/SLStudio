package com.symmetrylabs.shows.vines;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;


public class VineConfig {
	public List<LeafConfig> leaves = new ArrayList<LeafConfig>();

	public VineConfig(LeafConfig[] leaves) {
		this.leaves = Arrays.asList(leaves);
	}
}