package com.symmetrylabs.slstudio.util;

import org.apache.commons.math3.util.FastMath;

import java.util.Random;

public final class MathUtils {

    public static final double DEG_TO_RAD = Math.PI / 180d;
    public static final double RAD_TO_DEG = 180d / Math.PI;

    public static final float DEG_TO_RAD_F = (float)DEG_TO_RAD;
    public static final float RAD_TO_DEG_F = (float)RAD_TO_DEG;

    private static Random internalRandom;

    public static final float random(float high) {
        // avoid an infinite loop when 0 or NaN are passed in
        if (high == 0 || high != high) {
            return 0;
        }

        if (internalRandom == null) {
            internalRandom = new Random();
        }

        // for some reason (rounding error?) Math.random() * 3
        // can sometimes return '3' (once in ~30 million tries)
        // so a check was added to avoid the inclusion of 'howbig'
        float value = 0;
        do {
            value = internalRandom.nextFloat() * high;
        } while (value == high);
        return value;
    }

    public static final float randomGaussian() {
        if (internalRandom == null) {
            internalRandom = new Random();
        }
        return (float) internalRandom.nextGaussian();
    }

    public static final float random(float low, float high) {
        if (low >= high) return low;
        float diff = high - low;
        float value = 0;
        // because of rounding error, can't just add low, otherwise it may hit high
        // https://github.com/processing/processing/issues/4551
        do {
            value = random(diff) + low;
        } while (value == high);
        return value;
    }

    public static final void randomSeed(long seed) {
        if (internalRandom == null) {
            internalRandom = new Random();
        }
        internalRandom.setSeed(seed);
    }

    public static float constrain(float x, float min, float max) {
        return FastMath.min(FastMath.max(x, min), max);
    }

    public static float max(float a, float b) { return FastMath.max(a, b); }
    public static float min(float a, float b) { return FastMath.min(a, b); }
    public static float sin(float rad) { return (float)FastMath.sin(rad); }
    public static float cos(float rad) { return (float)FastMath.cos(rad); }
    public static float radians(float deg) { return DEG_TO_RAD_F * deg; }
    public static float degrees(float rad) { return RAD_TO_DEG_F * rad; }
    public static float abs(float val) { return FastMath.abs(val); }
    public static float sqrt(float val) { return (float)FastMath.sqrt(val); }
    public static float pow(float val, int exp) { return (float)FastMath.pow(val, exp); }
    public static float pow(float val, float exp) { return (float)FastMath.pow(val, exp); }
    public static int ceil(float val) { return (int)FastMath.ceil(val); }
    public static int floor(float val) { return (int)FastMath.floor(val); }
    public static int round(float val) { return (int)FastMath.round(val); }

    public static float lerp(float a, float b, float f) {
        return (a * (1f - f)) + (b * f);
    }

    public static float dist(float x0, float y0, float z0, float x1, float y1, float z1) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float dz = z1 - z0;
        return sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static float dist(float x0, float y0, float x1, float y1) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        return sqrt(dx * dx + dy * dy);
    }

    public static final float map(float value, float start1, float stop1, float start2, float stop2) {
        float outgoing = start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
        return outgoing;
    }

    public static byte byteSubtract(int a, int b) {
        byte res = (byte)(a - b);
        return (byte)(res & (byte)((b&0xFF) <= (a&0xFF) ? -1 : 0));
    }

    public static byte byteMultiply(byte b, double s) {
        int res = (int)((b&0xFF) * s);
        byte hi = (byte)(res >> 8);
        byte lo = (byte)(res);
        return (byte)(lo | (byte)(hi==0 ? 0 : -1));
    }
}
