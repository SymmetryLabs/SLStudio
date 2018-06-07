package com.symmetrylabs.layouts.tree.config;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;


public class LimbConfig {
    public float y;
    public float length;
    public float azimuth;
    public float elevation;
    public float tilt;
    private BranchConfig[] branches;

    public LimbConfig(float y, float length, float azimuth, float elevation, float tilt, BranchConfig[] branches) {
        this.y = y;
        this.length = length;
        this.azimuth = azimuth;
        this.elevation = elevation;
        this.tilt = tilt;
        this.branches = branches;
    }

    public List<BranchConfig> getBranches() {
        return Collections.unmodifiableList(Arrays.asList(branches));
    }

    public BranchConfig getBranchAtIndex(int i) {
        return branches[i];
    }
}