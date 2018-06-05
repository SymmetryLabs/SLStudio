package com.symmetrylabs.layouts.tree.config;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;


public class LimbConfig {
    public float length;
    public float height;
    public float azimuth;
    public float elevation;
    private BranchConfig[] branches;

    public LimbConfig(float length, float height, float azimuth, float elevation, BranchConfig[] branches) {
        this.length = length;
        this.height = height;
        this.azimuth = azimuth;
        this.elevation = elevation;
        this.branches = branches;
    }

    public List<BranchConfig> getBranches() {
        return Collections.unmodifiableList(Arrays.asList(branches));
    }

    public BranchConfig getBranchAtIndex(int i) {
        return branches[i];
    }
}