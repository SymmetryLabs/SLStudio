package com.symmetrylabs.util;

import org.apache.commons.math3.util.FastMath;

import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;

public class DistanceUtils {
    public static float manhattanDistance(float ax, float ay, float az, float bx, float by, float bz) {
        float x_diff = ax - bx;
        float y_diff = ay - by;
        float z_diff = az - bz;
        return FastMath.abs(x_diff) + FastMath.abs(y_diff) + FastMath.abs(z_diff);
    }

    public static float manhattanDistance(LXPoint a, LXPoint b) {
        return manhattanDistance(a.x, a.y, a.z, b.x, b.y, b.z);
    }

    public static float manhattanDistance(LXVector a, LXVector b) {
        return manhattanDistance(a.x, a.y, a.z, b.x, b.y, b.z);
    }

    public static float squaredEuclideanDistance(float ax, float ay, float az, float bx, float by, float bz) {
        float x_diff = ax - bx;
        float y_diff = ay - by;
        float z_diff = az - bz;
        return x_diff * x_diff + y_diff * y_diff + z_diff * z_diff;
    }

    public static float squaredEuclideanDistance(LXPoint a, LXPoint b) {
        return squaredEuclideanDistance(a.x, a.y, a.z, b.x, b.y, b.z);
    }

    public static float squaredEuclideanDistance(LXVector a, LXVector b) {
        return squaredEuclideanDistance(a.x, a.y, a.z, b.x, b.y, b.z);
    }

    public static float euclideanDistance(float ax, float ay, float az, float bx, float by, float bz) {
        return (float)FastMath.sqrt(squaredEuclideanDistance(ax, ay, az, bx, by, bz));
    }

    public static float euclideanDistance(LXPoint a, LXPoint b) {
        return euclideanDistance(a.x, a.y, a.z, b.x, b.y, b.z);
    }

    public static float euclideanDistance(LXVector a, LXVector b) {
        return euclideanDistance(a.x, a.y, a.z, b.x, b.y, b.z);
    }

    public static float metersToInches(float meters) {
        return meters / 0.0254f;
    }

    public static float inchesToMeters(float inches) {
        return inches * 39.37007874f;
    }
}
