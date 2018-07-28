package com.symmetrylabs.layouts.tree.config;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;

import static com.symmetrylabs.util.DistanceConstants.*;


public class LimbConfig {
    public static final float DEFAULT_LENGTH = 0*FEET;
    public static final float MIN_LENGTH =  0*FEET;
    public static final float MAX_LENGTH = 25*FEET;
    public static boolean lengthEnabled = true;

    public static final float DEFAULT_HEIGHT = 10*FEET;
    public static final float MIN_HEIGHT =  0*FEET;
    public static final float MAX_HEIGHT = 40*FEET;
    public static boolean heightEnabled = true;

    public static final float DEFAULT_AZIMUTH = 0;
    public static final float MIN_AZIMUTH = -180;
    public static final float MAX_AZIMUTH =  180;
    public static boolean azimuthEnabled = true;

    public static final float DEFAULT_ELEVATION = 0;
    public static final float MIN_ELEVATION = -180;
    public static final float MAX_ELEVATION =  180;
    public static boolean elevationEnabled = true;

    public float length;
    public float height;
    public float azimuth;
    public float elevation;
    public float tilt;
    private BranchConfig[] branches;

    public LimbConfig(float length, float height, float azimuth, float elevation, float tilt, BranchConfig[] branches) {
        this.length = length;
        this.height = height;
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
