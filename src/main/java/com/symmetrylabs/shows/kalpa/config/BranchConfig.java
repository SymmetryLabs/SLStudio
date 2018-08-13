package com.symmetrylabs.shows.kalpa.config;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;

import static com.symmetrylabs.util.DistanceConstants.*;


public class BranchConfig {
    public static final String DEFAULT_IP_ADDRESS = "0.0.0.0";
    public static final int DEFAULT_CHANNEL = 1;

    public static final float DEFAULT_X = 0*FEET;
    public static final float MIN_X = -15*FEET;
    public static final float MAX_X =  15*FEET;
    private static boolean X_ENABLED = true;

    public static final float DEFAULT_Y = 0*FEET;
    public static final float MIN_Y =  0*FEET;
    public static final float MAX_Y = 25*FEET;
    private static boolean Y_ENABLED = true;

    public static final float DEFAULT_Z = 0*FEET;
    public static final float MIN_Z = -15*FEET;
    public static final float MAX_Z =  15*FEET;
    private static boolean Z_ENABLED = true;

    public static final float DEFAULT_AZIMUTH = 0;
    public static final float MIN_AZIMUTH = -180;
    public static final float MAX_AZIMUTH =  180;
    private static boolean AZIMUTH_ENABLED = true;

    public static final float DEFAULT_ELEVATION = 0;
    public static final float MIN_ELEVATION = -180;
    public static final float MAX_ELEVATION =  180;
    private static boolean ELEVATION_ENABLED = true;

    public static final float DEFAULT_TILT = 0;
    public static final float MIN_TILT = -180;
    public static final float MAX_TILT =  180;
    private static boolean TILT_ENABLED = true;

    public String ipAddress;
    public int channel;
    public float x;
    public float y;
    public float z;
    public float azimuth;
    public float elevation;
    public float tilt;
    private TwigConfig[] twigs;

    public BranchConfig(float x, float y, float z, float azimuth, float elevation, float tilt, TwigConfig[] twigs) {
        this(DEFAULT_IP_ADDRESS, DEFAULT_CHANNEL, x, y, z, azimuth, elevation, tilt, twigs);
    }

    public BranchConfig(String ipAddress, int channel, float x, float y, float z, float azimuth, float elevation, float tilt, TwigConfig[] twigs) {
        this.ipAddress = ipAddress;
        this.channel = channel;
        this.x = x;
        this.y = y;
        this.z = z;
        this.azimuth = azimuth;
        this.elevation = elevation;
        this.tilt = tilt;
        this.twigs = twigs;
    }

    public List<TwigConfig> getTwigs() {
        return Collections.unmodifiableList(Arrays.asList(twigs));
    }

    public void setTwigs(TwigConfig[] configs) {
        twigs = configs;
    }

    public TwigConfig getTwigAtIndex(int i) {
        return twigs[i];
    }

    public static void setXEnabled(boolean enabled) {
        X_ENABLED = enabled;
    }

    public static boolean isXEnabled() {
        return X_ENABLED;
    }

    public static void setYEnabled(boolean enabled) {
        Y_ENABLED = enabled;
    }

    public static boolean isYEnabled() {
        return Y_ENABLED;
    }

    public static void setZEnabled(boolean enabled) {
        Z_ENABLED = enabled;
    }

    public static boolean isZEnabled() {
        return Z_ENABLED;
    }

    public static void setAzimuthEnabled(boolean enabled) {
        AZIMUTH_ENABLED = enabled;
    }

    public static boolean isAzimuthEnabled() {
        return AZIMUTH_ENABLED;
    }

    public static void setElevationEnabled(boolean enabled) {
        ELEVATION_ENABLED = enabled;
    }

    public static boolean isElevationEnabled() {
        return ELEVATION_ENABLED;
    }

    public static void setTiltEnabled(boolean enabled) {
        TILT_ENABLED = enabled;
    }

    public static boolean isTiltEnabled() {
        return TILT_ENABLED;
    }

    public BranchConfig getCopy() {
        return new BranchConfig(x, y, z, azimuth, elevation, tilt, twigs);
    }
}
