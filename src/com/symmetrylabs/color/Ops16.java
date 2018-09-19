package com.symmetrylabs.color;

/**
 * Operations on 16-bit-per-channel color values packed into 64-bit integers.
 * Unlike LXColor, all routines in Ops16 use numeric types consistently:
 *   - Integer components are always ints in the range from 0 to 65535.
 *   - Floating-point components are always doubles in the range from 0.0 to 1.0
 *         (no single-precision floats, and never 0 to 100 or 0 to 360).
 *   - Rounding is always performed to the nearest integer (never by truncation).
 */
public class Ops16 {
    private Ops16() {
        throw new UnsupportedOperationException("This is a static utility class");
    }

    /** A function that blends a base color with an overlay color. */
    public interface BlendFunc {
        long apply(long base, long overlay, double alpha);
    }

    public static final int MAX = 0xffff;
    public static final long BLACK = rgba(0, 0, 0, MAX);
    public static final long WHITE = rgba(MAX, MAX, MAX, MAX);

    public static long rgba(int r, int g, int b, int a) {
        return (clamp(a) << 48) | (clamp(r) << 32) | (clamp(g) << 16) | clamp(b);
    }
    public static long gray(double level) { return multiply(WHITE, level); }

    public static int alpha(long argb) { return (int) ((argb & 0xffff_0000_0000_0000L) >>> 48); }
    public static int red(long argb) { return (int) ((argb & 0x0000_ffff_0000_0000L) >>> 32); }
    public static int green(long argb) { return (int) ((argb & 0x0000_0000_ffff_0000L) >>> 16); }
    public static int blue(long argb) { return (int) (argb & 0x0000_0000_0000_ffffL); }

    /**
     * Returns hue value of a 16-bit color, from 0 to 1.
     */
    public static double hue(long argb) {
        int r = red(argb);
        int g = green(argb);
        int b = blue(argb);
        int max = (r > g) ? r : g;
        if (b > max) {
            max = b;
        }
        int min = (r < g) ? r : g;
        if (b < min) {
            min = b;
        }
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
     * Returns the saturation of a 16-bit color, from 0 to 1.
     */
    public static double saturation(long argb) {
        int r = red(argb);
        int g = green(argb);
        int b = blue(argb);
        int max = (r > g) ? r : g;
        if (b > max) {
            max = b;
        }
        int min = (r < g) ? r : g;
        if (b < min) {
            min = b;
        }
        return (max == 0) ? 0 : (max - min) / max;
    }

    /**
     * Returns the brightness of a 16-bit color, from 0 to 1.
     */
    public static double brightness(long argb) {
        return level(argb);
    }

    /**
     * Returns a color given hue, saturation, and brightness.
     */
    public static long hsb(double hue, double saturation, double brightness) {
        return hsba(hue, saturation, brightness, 1);
    }

    /**
     * Returns a color given hue, saturation, brightness, and alpha.
     */
    public static long hsba(double hue, double saturation, double brightness, double alpha) {
        // hue is passed in as 0-1, calculation expects 0-360
        hue *= 360;

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

    /** Multiplies the R, G, and B components by a factor from 0.0 to 1.0. */
    public static long multiply(long argb, double v) {
        return rgba(
                (int) (red(argb) * v + 0.5),
                (int) (green(argb) * v + 0.5),
                (int) (blue(argb) * v + 0.5),
                alpha(argb)
        );
    }

    /** Returns the maximum of the R, G, and B channels as a level from 0.0 to 1.0. */
    public static double level(long argb) {
        int value = max(max(red(argb), green(argb)), blue(argb));
        return (double) value / MAX;
    }

    /** Returns the average of the R, G, and B channels as a level from 0.0 to 1.0. */
    public static double mean(long argb) {
        return (double) (red(argb) + green(argb) + blue(argb)) / 3 / MAX;
    }

    /**
     * Adjusts an integer alpha value in the range from 0 to 0xffff to fall in the
     * range from 0 to 0x10000, such that 0x7fff yields an effective alpha of exactly
     * 0.5, and 0xffff yields an effective alpha of exactly 1.0.  Methods that take
     * an extended alpha argument are named with a trailing "X", like blendX().
     */
    private static int extendAlpha(int alpha) {
        return alpha + (alpha >= 0x7fff ? 1 : 0);
    }

    /** Blends c1 toward c2, by the alpha of c2. */
    public static long blend(long c1, long c2) { return blendX(c1, c2, 0x10000); }

    /** Blends c1 toward c2, by a fraction (0.0 to 1.0) of the alpha of c2. */
    public static long blend(long c1, long c2, double f) { return blendX(c1, c2, (int) (f * 0x10000)); }

    /** Blends c1 toward c2, by a fraction (0 to 0x10000) of the alpha of c2. */
    private static long blendX(long c1, long c2, int f) {
        int a = (f * alpha(c2)) >>> 16;
        int xa = extendAlpha(a), ia = 0x10000 - xa;
        return rgba(
                (red(c1) * ia + red(c2) * xa) >>> 16,
                (green(c1) * ia + green(c2) * xa) >>> 16,
                (blue(c1) * ia + blue(c2) * xa) >>> 16,
                alpha(c1) + a
        );
    }

    /** Adds c2 to c1, channel by channel, using the alpha of c2. */
    public static long add(long c1, long c2) { return addX(c1, c2, 0x10000); }

    /** Adds c2 to c1, blending by a fraction (0.0 to 1.0) of the alpha of c2. */
    public static long add(long c1, long c2, double f) { return addX(c1, c2, (int) (f * 0x10000)); }

    /** Adds c2 to c1, blending by a fraction (0 to 0x10000) of the alpha of c2. */
    private static long addX(long c1, long c2, int f) {
        int a = (f * alpha(c2)) >>> 16;
        int xa = extendAlpha(a);
        return rgba(
                red(c1) + (red(c2) * xa >>> 16),
                green(c1) + (green(c2) * xa >>> 16),
                blue(c1) + (blue(c2) * xa >>> 16),
                alpha(c1) + a
        );
    }

    /** Subtracts c2 from c1, channel by channel, using the alpha of c2. */
    public static long subtract(long c1, long c2) { return subtractX(c1, c2, 0x10000); }

    /** Subtracts c2 from c1, blending by a fraction (0.0 to 1.0) of the alpha of c2. */
    public static long subtract(long c1, long c2, double f) { return subtractX(c1, c2, (int) (f * 0x10000)); }

    /** Subtracts c2 from c1, blending by a fraction (0 to 0x10000) of the alpha of c2. */
    private static long subtractX(long c1, long c2, int f) {
        int a = (f * alpha(c2)) >>> 16;
        int xa = extendAlpha(a);
        return rgba(
                red(c1) - (red(c2) * xa >>> 16),
                green(c1) - (green(c2) * xa >>> 16),
                blue(c1) - (blue(c2) * xa >>> 16),
                alpha(c1) + a
        );
    }

    /** Blends c1 toward the per-channel product of c1 and c2, by the alpha of c2. */
    public static long multiply(long c1, long c2) { return multiplyX(c1, c2, 0x10000); }

    /** Blends c1 toward the per-channel product of c1 and c2, by a fraction (0.0 to 1.0) of the alpha of c2. */
    public static long multiply(long c1, long c2, double f) { return multiplyX(c1, c2, (int) (f * 0x10000)); }

    /** Blends c1 toward the per-channel product of c1 and c2, by a fraction (0 to 0x10000) of the alpha of c2. */
    private static long multiplyX(long c1, long c2, int f) {
        int a = (f * alpha(c2)) >>> 16;
        int xa = extendAlpha(a), ia = 0x10000 - xa;
        int rProd = (red(c1) + 1) * red(c2) >>> 16;
        int gProd = (green(c1) + 1) * green(c2) >>> 16;
        int bProd = (blue(c1) + 1) * blue(c2) >>> 16;
        return rgba(
                (red(c1) * ia + rProd * xa) >>> 16,
                (green(c1) * ia + gProd * xa) >>> 16,
                (blue(c1) * ia + bProd * xa) >>> 16,
                alpha(c1) + a
        );
    }

    /** Blends c1 toward the per-channel inverse product of c1 and c2, by the alpha of c2. */
    public static long screen(long c1, long c2) { return screenX(c1, c2, 0x10000); }

    /** Blends c1 toward the per-channel inverse product of c1 and c2, by a fraction (0.0 to 1.0) of the alpha of c2. */
    public static long screen(long c1, long c2, double f) { return screenX(c1, c2, (int) (f * 0x10000)); }

    /** Blends c1 toward the per-channel inverse product of c1 and c2, by a fraction (0 to 0x10000) of the alpha of c2. */
    private static long screenX(long c1, long c2, int f) {
        int a = (f * alpha(c2)) >>> 16;
        int xa = extendAlpha(a), ia = 0x10000 - xa;
        int rProd = (red(c1) + 1) * red(c2) >>> 16;
        int gProd = (green(c1) + 1) * green(c2) >>> 16;
        int bProd = (blue(c1) + 1) * blue(c2) >>> 16;
        return rgba(
                (red(c1) * ia + (red(c1) + red(c2) - rProd) * xa) >>> 16,
                (green(c1) * ia + (green(c1) + green(c2) - gProd) * xa) >>> 16,
                (blue(c1) * ia + (blue(c1) + blue(c2) - bProd) * xa) >>> 16,
                alpha(c1) + a
        );
    }

    /** Blends c1 toward the per-channel lightest of c1 and c2, by a fraction (0.0 to 1.0) of the alpha of c2. */
    public static long lightest(long c1, long c2, double f) { return lightestX(c1, c2, (int) (f * 0x10000)); }

    /** Blends c1 toward the per-channel lightest of c1 and c2, by a fraction (0 to 0x10000) of the alpha of c2. */
    private static long lightestX(long c1, long c2, int f) {
        int a = (f * alpha(c2)) >>> 16;
        int xa = extendAlpha(a), ia = 0x10000 - xa;
        return rgba(
                (red(c1) * ia + max(red(c1), red(c2)) * xa) >>> 16,
                (green(c1) * ia + max(green(c1), green(c2)) * xa) >>> 16,
                (blue(c1) * ia + max(blue(c1), blue(c2)) * xa) >>> 16,
                alpha(c1) + a
        );
    }

    /** Blends c1 toward the per-channel darkest of c1 and c2, by the alpha of c2. */
    public static long darkest(long c1, long c2) { return darkestX(c1, c2, 0x10000); }

    /** Blends c1 toward the per-channel darkest of c1 and c2, by a fraction (0.0 to 1.0) of the alpha of c2. */
    public static long darkest(long c1, long c2, double f) { return darkestX(c1, c2, (int) (f * 0x10000)); }

    /** Blends c1 toward the per-channel darkest of c1 and c2, by a fraction (0 to 0x10000) of the alpha of c2. */
    private static long darkestX(long c1, long c2, int f) {
        int a = (f * alpha(c2)) >>> 16;
        int xa = extendAlpha(a), ia = 0x10000 - xa;
        return rgba(
                (red(c1) * ia + min(red(c1), red(c2)) * xa) >>> 16,
                (green(c1) * ia + min(green(c1), green(c2)) * xa) >>> 16,
                (blue(c1) * ia + min(blue(c1), blue(c2)) * xa) >>> 16,
                alpha(c1) + a
        );
    }

    /** Blends c1 toward the absolute difference between c1 and c2, by the alpha of c2. */
    public static long difference(long c1, long c2) { return differenceX(c1, c2, 0x10000); }

    /** Blends c1 toward the absolute difference between of c1 and c2, by a fraction (0.0 to 1.0) of the alpha of c2. */
    public static long difference(long c1, long c2, double f) { return differenceX(c1, c2, (int) (f * 0x10000)); }

    /** Blends c1 toward the absolute difference between of c1 and c2, by a fraction (0 to 0x10000) of the alpha of c2. */
    private static long differenceX(long c1, long c2, int f) {
        int a = (f * alpha(c2)) >>> 16;
        int xa = extendAlpha(a), ia = 0x10000 - xa;
        int rDiff = Math.abs(red(c1) - red(c2));
        int gDiff = Math.abs(green(c1) - green(c2));
        int bDiff = Math.abs(blue(c1) - blue(c2));
        return rgba(
                (red(c1) * ia + rDiff * xa) >>> 16,
                (green(c1) * ia + gDiff * xa) >>> 16,
                (blue(c1) * ia + bDiff * xa) >>> 16,
                alpha(c1) + a
        );
    }

    /**
     * Blends c1 toward the halfway point between c1 and c2, by the specified
     * fraction (0.0 to 1.0).  The alpha of the result is always 0xffff.
     */
    public static long dissolve(long c1, long c2, double f) { return dissolveX(c1, c2, (int) (f * 0x10000)); }

    /**
     * Blends c1 toward the halfway point between c1 and c2, by the specified
     * fraction (0 to 0x10000).  The alpha of the result is always 0xffff.
     */
    private static long dissolveX(long c1, long c2, int f) {
        int xa = f/2, ia = 0x10000 - xa;
        return rgba(
                (red(c1) * ia + red(c2) * xa) >>> 16,
                (green(c1) * ia + green(c2) * xa) >>> 16,
                (blue(c1) * ia + blue(c2) * xa) >>> 16,
                MAX
        );
    }

    private static int min(int a, int b) { return a < b ? a : b; }
    private static int max(int a, int b) { return a > b ? a : b; }
    private static long clamp(int x) { return (x < 0 ? 0 : x > MAX ? MAX : x); }
}
