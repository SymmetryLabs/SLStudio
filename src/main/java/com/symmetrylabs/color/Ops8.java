package com.symmetrylabs.color;

/**
 * Operations on 8-bit-per-channel color values packed into 32-bit integers.
 * Unlike LXColor, all routines in Ops8 use numeric types consistently:
 *   - Integer components are always ints in the range from 0 to 255 (never bytes, never negative).
 *   - Floating-point components are always doubles in the range from 0.0 to 1.0
 *         (no single-precision floats, and never 0 to 100 or 0 to 360).
 *   - Rounding is always performed to the nearest integer (never by truncation).
 */
public class Ops8 {
    private Ops8() {
        throw new UnsupportedOperationException("This is a static utility class");
    }

    /** A function that blends a base color with an overlay color. */
    public interface BlendFunc {
        int apply(int base, int overlay, double alpha);
    }

    public static final int MAX = 0xff;
    public static final int BLACK = rgba(0, 0, 0, MAX);
    public static final int WHITE = rgba(MAX, MAX, MAX, MAX);

    public static int rgba(int r, int g, int b, int a) {
        return (clamp(a) << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }
    public static int gray(double level) { return multiply(WHITE, level); }

    public static int alpha(int argb) { return (argb & 0xff000000) >>> 24; }
    public static int red(int argb) { return (argb & 0x00ff0000) >>> 16; }
    public static int green(int argb) { return (argb & 0x0000ff00) >>> 8; }
    public static int blue(int argb) { return argb & 0x000000ff; }

    /**
     * Returns hue value of a 8-bit color, from 0 to 1.
     */
    public static double hue(int argb) {
        int r = red(argb);
        int g = green(argb);
        int b = blue(argb);
        double max = max(max(r, g), b);
        double min = min(min(r, g), b);
        if (max == 0) {
            return 0;
        }
        double range = max - min;
        if (range == 0) {
            return 0;
        }
        double h;
        double rc = (max - r) / range;
        double gc = (max - g) / range;
        double bc = (max - b) / range;
        if (r == max) {
            h = bc - gc;
        } else if (g == max) {
            h = 2.f + rc - bc;
        } else {
            h = 4.f + gc - rc;
        }
        h /= 6.f;
        if (h < 0) {
            h += 1.f;
        }
        return h;
    }

    /**
     * Returns the saturation of a 8-bit color, from 0 to 1.
     */
    public static double saturation(int argb) {
        int r = red(argb);
        int g = green(argb);
        int b = blue(argb);
        double max = max(max(r, g), b);
        double min = min(min(r, g), b);
        return (max == 0) ? 0 : (max - min) / max;
    }

    /**
     * Returns the brightness of a 8-bit color, from 0 to 1.
     */
    public static double brightness(int argb) {
        return level(argb);
    }

    /**
     * Returns a color given hue, saturation, and brightness.
     */
    public static int hsb(double hue, double saturation, double brightness) {
        return hsba(hue, saturation, brightness, 1);
    }

    /**
     * Returns a color given hue, saturation, brightness, and alpha.
     */
    public static int hsba(double hue, double saturation, double brightness, double alpha) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int)(brightness * MAX + 0.5);
        } else {
            double h = (hue - Math.floor(hue)) * 6.;
            double f = h - Math.floor(h);
            double p = brightness * (1. - saturation);
            double q = brightness * (1. - saturation * f);
            double t = brightness * (1. - (saturation * (1. - f)));
            switch ((int) h) {
            case 0:
                r = (int) (brightness * MAX + 0.5);
                g = (int) (t * MAX + 0.5);
                b = (int) (p * MAX + 0.5);
                break;
            case 1:
                r = (int) (q * MAX + 0.5);
                g = (int) (brightness * MAX + 0.5);
                b = (int) (p * MAX + 0.5);
                break;
            case 2:
                r = (int) (p * MAX + 0.5);
                g = (int) (brightness * MAX + 0.5);
                b = (int) (t * MAX + 0.5);
                break;
            case 3:
                r = (int) (p * MAX + 0.5);
                g = (int) (q * MAX + 0.5);
                b = (int) (brightness * MAX + 0.5);
                break;
            case 4:
                r = (int) (t * MAX + 0.5);
                g = (int) (p * MAX + 0.5);
                b = (int) (brightness * MAX + 0.5);
                break;
            case 5:
                r = (int) (brightness * MAX + 0.5);
                g = (int) (p * MAX + 0.5);
                b = (int) (q * MAX + 0.5);
                break;
            }
        }

        return rgba(r, g, b, (int)(alpha * MAX));
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

    /** Returns the maximum of the R, G, and B channels as a level from 0.0 to 1.0. */
    public static double level(int argb) {
        int value = max(max(red(argb), green(argb)), blue(argb));
        return (double) value / MAX;
    }

    /** Returns the average of the R, G, and B channels as a level from 0.0 to 1.0. */
    public static double mean(int argb) {
        return (double) (red(argb) + green(argb) + blue(argb)) / 3 / MAX;
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
                MAX
        );
    }

    private static int min(int a, int b) { return a < b ? a : b; }
    private static int max(int a, int b) { return a > b ? a : b; }
    private static int clamp(int x) { return (x < 0 ? 0 : x > MAX ? MAX : x); }

    // These void-typed methods prevent accidentally calling rgba() with a byte.
    public static void rgba(byte r, int g, int b, int a) { rejectByte(); }
    public static void rgba(int r, byte g, int b, int a) { rejectByte(); }
    public static void rgba(int r, int g, byte b, int a) { rejectByte(); }
    public static void rgba(int r, int g, int b, byte a) { rejectByte(); }
    private static void rejectByte() {
        throw new UnsupportedOperationException("Ops8 does not accept bytes");
    }
}
