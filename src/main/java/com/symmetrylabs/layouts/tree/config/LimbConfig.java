package com.symmetrylabs.layouts.tree.config;

public class LimbConfig {
    float y;
    float length;
    float azimuth;
    float elevation;
    float tilt;
    private List<BranchConfig> branches;

    public LimbConfig(float y, float length, float azimuth, float elevation, float tilt, List<BranchConfig> branches) {
        this.y = y;
        this.length = lenght;
        this.azimuth = azimuth;
        this.elevation = elevation;
        this.tilt = tilt;
        this.branches = Collections.unmodifiableList(branches);
    }

    public List<BranchConfig> getBranches() {
        return branches;
    }

    public BranchConfig getBranchAtIndex(int i) {
        return branches.get(i);
    }
}