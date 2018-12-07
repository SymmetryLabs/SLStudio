package com.symmetrylabs.shows.tree.config;

import static com.symmetrylabs.util.DistanceConstants.*;


public class TwigConfig {
    public static final float DEFAULT_X = 0*FEET;
    public static final float MIN_X = -4*FEET;
    public static final float MAX_X =  4*FEET;
    private static boolean X_ENABLED = true;

    public static final float DEFAULT_Y = 0*FEET;
    public static final float MIN_Y =  0*FEET;
    public static final float MAX_Y =  8*FEET;
    private static boolean Y_ENABLED = true;

    public static final float DEFAULT_Z = 0*FEET;
    public static final float MIN_Z = -3*FEET;
    public static final float MAX_Z =  3*FEET;
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

    public float x;
    public float y;
    public float z;
    public float azimuth;
    public float elevation;
    public float tilt;
    public int index;

    public TwigConfig(float x, float y, float z, float azimuth, float elevation, float tilt, int index) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.azimuth = azimuth;
        this.elevation = elevation;
        this.tilt = tilt;
        this.index = index;
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

    public TwigConfig getCopy() {
        return new TwigConfig(x, y, z, azimuth, elevation, tilt, index);
    }
}
