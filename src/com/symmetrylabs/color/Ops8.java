package com.symmetrylabs.color;

/**
 * Operations on 8-bit-per-channel color values packed into 32-bit integers.
 * Unlike LXColor, all routines in Ops8 follow these consistent rules:
 *   - Integer components are always in the range from 0 to 255 (never negative).
 *   - Double components are always in the range from 0.0 to 1.0 (never 0 to 100 or 0 to 360).
 *   - All floating-point numbers are doubles (no single-precision floats).
 *   - Floating-point numbers are rounded always to the nearest integer (never truncated).
 */
public class Ops8 {
    private Ops8() {
        throw new UnsupportedOperationException("This is a static utility class");
    }

    /** A function that blends a base color with an overlay color. */
    public interface BlendFunc {
        int apply(int base, int overlay, double alpha);
    }

    private static int min(int a, int b) { return a < b ? a : b; }
    private static int max(int a, int b) { return a > b ? a : b; }
    private static int clamp(int x) { return (x < 0 ? 0 : x > 255 ? 255 : x); }

    public static int alpha(int argb) { return (argb & 0xff000000) >>> 24; }
    public static int red(int argb) { return (argb & 0x00ff0000) >>> 16; }
    public static int green(int argb) { return (argb & 0x0000ff00) >>> 8; }
    public static int blue(int argb) { return argb & 0x000000ff; }

    public static int rgba(int r, int g, int b, int a) {
        return (clamp(a) << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    /** Returns the brightest channel as a level from 0.0 to 1.0. */
    public static double level(int argb) {
        int value = max(max(red(argb), green(argb)), blue(argb));
        return (double) value / 255;
    }

    /** Returns a gray color value, given a gray level from 0.0 to 1.0. */
    public static int gray(double level) {
        int value = (int) (255 * level + 0.5);
        return rgba(value, value, value, 0xff);
    }

    /** Multiplies the R, G, and B components by a fraction from 0.0 to 1.0. */
    public static int multiply(int argb, double f) {
        return rgba(
                (int) (red(argb) * f + 0.5),
                (int) (green(argb) * f + 0.5),
                (int) (blue(argb) * f + 0.5),
                alpha(argb)
        );
    }

    /**
     * Adjusts an integer alpha value in the range from 0 to 0xff to fall in the
     * range from 0 to 0x100, such that 0x7f yields an effective alpha of exactly
     * 0.5, and 0xff yields an effective alpha of exactly 1.0.  Methods that take
     * an extended alpha argument are named with a trailing "X", like blendX().
     */
    private static int extendAlpha(int alpha) {
        return alpha + (alpha >= 0x7f ? 1 : 0);
    }

    /** Blends c1 toward c2, by the alpha of c2. */
    public static int blend(int c1, int c2) { return blendX(c1, c2, 0x100); }

    /** Blends c1 toward c2, by a fraction (0.0 to 1.0) of the alpha of c2. */
    public static int blend(int c1, int c2, double f) { return blendX(c1, c2, (int) (f * 0x100)); }

    /** Blends c1 toward c2, by a fraction (0 to 0x100) of the alpha of c2. */
    private static int blendX(int c1, int c2, int f) {
        int a = (f * alpha(c2)) >>> 8;
        int xa = extendAlpha(a), ia = 0x100 - xa;
        return rgba(
                (red(c1) * ia + red(c2) * xa) >>> 8,
                (green(c1) * ia + green(c2) * xa) >>> 8,
                (blue(c1) * ia + blue(c2) * xa) >>> 8,
                alpha(c1) + a
        );
    }

    /** Adds c2 to c1, channel by channel, using the alpha of c2. */
    public static int add(int c1, int c2) { return addX(c1, c2, 0x100); }

    /** Adds c2 to c1, blending by a fraction (0.0 to 1.0) of the alpha of c2. */
    public static int add(int c1, int c2, double f) { return addX(c1, c2, (int) (f * 0x100)); }

    /** Adds c2 to c1, blending by a fraction (0 to 0x100) of the alpha of c2. */
    private static int addX(int c1, int c2, int f) {
        int a = (f * alpha(c2)) >>> 8;
        int xa = extendAlpha(a);
        return rgba(
                red(c1) + (red(c2) * xa >>> 8),
                green(c1) + (green(c2) * xa >>> 8),
                blue(c1) + (blue(c2) * xa >>> 8),
                alpha(c1) + a
        );
    }

    /** Subtracts c2 from c1, channel by channel, using the alpha of c2. */
    public static int subtract(int c1, int c2) { return subtractX(c1, c2, 0x100); }

    /** Subtracts c2 from c1, blending by a fraction (0.0 to 1.0) of the alpha of c2. */
    public static int subtract(int c1, int c2, double f) { return subtractX(c1, c2, (int) (f * 0x100)); }

    /** Subtracts c2 from c1, blending by a fraction (0 to 0x100) of the alpha of c2. */
    private static int subtractX(int c1, int c2, int f) {
        int a = (f * alpha(c2)) >>> 8;
        int xa = extendAlpha(a);
        return rgba(
                red(c1) - (red(c2) * xa >>> 8),
                green(c1) - (green(c2) * xa >>> 8),
                blue(c1) - (blue(c2) * xa >>> 8),
                alpha(c1) + a
        );
    }

    /** Blends c1 toward the per-channel product of c1 and c2, by the alpha of c2. */
    public static int multiply(int c1, int c2) { return multiplyX(c1, c2, 0x100); }

    /** Blends c1 toward the per-channel product of c1 and c2, by a fraction (0.0 to 1.0) of the alpha of c2. */
    public static int multiply(int c1, int c2, double f) { return multiplyX(c1, c2, (int) (f * 0x100)); }

    /** Blends c1 toward the per-channel product of c1 and c2, by a fraction (0 to 0x100) of the alpha of c2. */
    private static int multiplyX(int c1, int c2, int f) {
        int a = (f * alpha(c2)) >>> 8;
        int xa = extendAlpha(a), ia = 0x100 - xa;
        int rProd = (red(c1) + 1) * red(c2) >>> 8;
        int gProd = (green(c1) + 1) * green(c2) >>> 8;
        int bProd = (blue(c1) + 1) * blue(c2) >>> 8;
        return rgba(
                (red(c1) * ia + rProd * xa) >>> 8,
                (green(c1) * ia + gProd * xa) >>> 8,
                (blue(c1) * ia + bProd * xa) >>> 8,
                alpha(c1) + a
        );
    }

    /** Blends c1 toward the per-channel inverse product of c1 and c2, by the alpha of c2. */
    public static int screen(int c1, int c2) { return screenX(c1, c2, 0x100); }

    /** Blends c1 toward the per-channel inverse product of c1 and c2, by a fraction (0.0 to 1.0) of the alpha of c2. */
    public static int screen(int c1, int c2, double f) { return screenX(c1, c2, (int) (f * 0x100)); }

    /** Blends c1 toward the per-channel inverse product of c1 and c2, by a fraction (0 to 0x100) of the alpha of c2. */
    private static int screenX(int c1, int c2, int f) {
        int a = (f * alpha(c2)) >>> 8;
        int xa = extendAlpha(a), ia = 0x100 - xa;
        int rProd = (red(c1) + 1) * red(c2) >>> 8;
        int gProd = (green(c1) + 1) * green(c2) >>> 8;
        int bProd = (blue(c1) + 1) * blue(c2) >>> 8;
        return rgba(
                (red(c1) * ia + (red(c1) + red(c2) - rProd) * xa) >>> 8,
                (green(c1) * ia + (green(c1) + green(c2) - gProd) * xa) >>> 8,
                (blue(c1) * ia + (blue(c1) + blue(c2) - bProd) * xa) >>> 8,
                alpha(c1) + a
        );
    }

    /** Blends c1 toward the per-channel lightest of c1 and c2, by a fraction (0.0 to 1.0) of the alpha of c2. */
    public static int lightest(int c1, int c2, double f) { return lightestX(c1, c2, (int) (f * 0x100)); }

    /** Blends c1 toward the per-channel lightest of c1 and c2, by a fraction (0 to 0x100) of the alpha of c2. */
    private static int lightestX(int c1, int c2, int f) {
        int a = (f * alpha(c2)) >>> 8;
        int xa = extendAlpha(a), ia = 0x100 - xa;
        return rgba(
                (red(c1) * ia + max(red(c1), red(c2)) * xa) >>> 8,
                (green(c1) * ia + max(green(c1), green(c2)) * xa) >>> 8,
                (blue(c1) * ia + max(blue(c1), blue(c2)) * xa) >>> 8,
                alpha(c1) + a
        );
    }

    /** Blends c1 toward the per-channel darkest of c1 and c2, by the alpha of c2. */
    public static int darkest(int c1, int c2) { return darkestX(c1, c2, 0x100); }

    /** Blends c1 toward the per-channel darkest of c1 and c2, by a fraction (0.0 to 1.0) of the alpha of c2. */
    public static int darkest(int c1, int c2, double f) { return darkestX(c1, c2, (int) (f * 0x100)); }

    /** Blends c1 toward the per-channel darkest of c1 and c2, by a fraction (0 to 0x100) of the alpha of c2. */
    private static int darkestX(int c1, int c2, int f) {
        int a = (f * alpha(c2)) >>> 8;
        int xa = extendAlpha(a), ia = 0x100 - xa;
        return rgba(
                (red(c1) * ia + min(red(c1), red(c2)) * xa) >>> 8,
                (green(c1) * ia + min(green(c1), green(c2)) * xa) >>> 8,
                (blue(c1) * ia + min(blue(c1), blue(c2)) * xa) >>> 8,
                alpha(c1) + a
        );
    }

    /** Blends c1 toward the absolute difference between c1 and c2, by the alpha of c2. */
    public static int difference(int c1, int c2) { return differenceX(c1, c2, 0x100); }

    /** Blends c1 toward the absolute difference between of c1 and c2, by a fraction (0.0 to 1.0) of the alpha of c2. */
    public static int difference(int c1, int c2, double f) { return differenceX(c1, c2, (int) (f * 0x100)); }

    /** Blends c1 toward the absolute difference between of c1 and c2, by a fraction (0 to 0x100) of the alpha of c2. */
    private static int differenceX(int c1, int c2, int f) {
        int a = (f * alpha(c2)) >>> 8;
        int xa = extendAlpha(a), ia = 0x100 - xa;
        int rDiff = Math.abs(red(c1) - red(c2));
        int gDiff = Math.abs(green(c1) - green(c2));
        int bDiff = Math.abs(blue(c1) - blue(c2));
        return rgba(
                (red(c1) * ia + rDiff * xa) >>> 8,
                (green(c1) * ia + gDiff * xa) >>> 8,
                (blue(c1) * ia + bDiff * xa) >>> 8,
                alpha(c1) + a
        );
    }

    /**
     * Blends c1 toward the halfway point between c1 and c2, by the specified
     * fraction (0.0 to 1.0).  The alpha of the result is always 0xff.
     */
    public static int dissolve(int c1, int c2, double f) { return dissolveX(c1, c2, (int) (f * 0x100)); }

    /**
     * Blends c1 toward the halfway point between c1 and c2, by the specified
     * fraction (0 to 0x100).  The alpha of the result is always 0xff.
     */
    private static int dissolveX(int c1, int c2, int f) {
        int xa = f/2, ia = 0x100 - xa;
        return rgba(
                (red(c1) * ia + red(c2) * xa) >>> 8,
                (green(c1) * ia + green(c2) * xa) >>> 8,
                (blue(c1) * ia + blue(c2) * xa) >>> 8,
                0xff
        );
    }
}
