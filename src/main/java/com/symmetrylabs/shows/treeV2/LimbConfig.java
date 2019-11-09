package com.symmetrylabs.shows.treeV2;

import java.util.List;
import java.util.ArrayList;


public class LimbConfig {
	private final LimbConfig[] limbs;
	private final BranchConfig[] branches;

	public LimbConfig(LimbConfig limb) {
		this(new LimbConfig[] {limb});
	}

	public LimbConfig(LimbConfig[] limbs) {
		this(limbs, new BranchConfig[]{});
	}

	public LimbConfig(BranchConfig[] branches) {
		this(new LimbConfig[]{}, branches);
	}

	public LimbConfig(LimbConfig[] limbs, BranchConfig[] branches) {
		this.limbs = limbs;
		this.branches = branches;
	}

	public LimbConfig(BranchConfig[] branches, LimbConfig[] limbs) {
		this.limbs = limbs;
		this.branches = branches;
	}

	public LimbConfig[] getLimbs() {
		return limbs;
	}

	public BranchConfig[] getBranches() {
		return branches;
	}
}