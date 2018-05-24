package com.symmetrylabs.util;

import java.util.Random;

/**
 * Perlin noise
 */
public class NoiseUtils {
    private static NoiseHelper noiseHelper = new NoiseHelper();

    public static float noise(float x) {
        return noiseHelper.noise(x);
    }

    public static float noise(float x, float y) {
        return noiseHelper.noise(x, y);
    }

    public static float noise(float x, float y, float z) {
        return noiseHelper.noise(x, y, z);
    }
}
