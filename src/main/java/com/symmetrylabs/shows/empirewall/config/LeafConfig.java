package com.symmetrylabs.shows.empirewall.config;

import static com.symmetrylabs.util.DistanceConstants.*;


public class LeafConfig {
    public static int counter = 0;

    public static final float DEFAULT_X = 0*FEET;
    public static final float MIN_X = -FEET;
    public static final float MAX_X =  19*FEET;

    public static final float DEFAULT_Y = 0*FEET;
    public static final float MIN_Y =  -FEET;
    public static final float MAX_Y =  13*FEET;

    public static final float DEFAULT_Z = 0*FEET;
    public static final float MIN_Z = FEET;
    public static final float MAX_Z =  -2*FEET;

    public static final float DEFAULT_X_ROT = 0;
    public static final float MIN_X_ROT = -180;
    public static final float MAX_X_ROT =  180;

    public static final float DEFAULT_Y_ROT = 0;
    public static final float MIN_Y_ROT = -180;
    public static final float MAX_Y_ROT =  180;

    public static final float DEFAULT_Z_ROT = 0;
    public static final float MIN_Z_ROT = -180;
    public static final float MAX_Z_ROT =  180;

    public float x;
    public float y;
    public float z;
    public float xRot;
    public float yRot;
    public float zRot;
    public int index;

    public LeafConfig(float x, float y, float z, float xRot, float yRot, float zRot) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.xRot = xRot;
        this.yRot = yRot;
        this.zRot = zRot;
        this.index = counter++;
    }

    public LeafConfig getCopy() {
        return new LeafConfig(x, y, z, xRot, yRot, zRot);
    }
}
