/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx;

import java.awt.Color;

/**
 * Various utilities that operate on color values
 */
public class LXColor {

    public enum Blend {
        LERP,
        ADD,
        SUBTRACT,
        MULTIPLY,
        SCREEN,
        LIGHTEST,
        DARKEST
    }

    public static final int ALPHA_MASK = 0xff000000;
    public static final int RED_MASK = 0x00ff0000;
    public static final int GREEN_MASK = 0x0000ff00;
    public static final int BLUE_MASK = 0x000000ff;

    public static final int ALPHA_SHIFT = 24;
    public static final int RED_SHIFT = 16;
    public static final int GREEN_SHIFT = 8;

    public static byte alpha(int argb) {
        return (byte) ((argb & ALPHA_MASK) >>> ALPHA_SHIFT);
    }

    public static byte red(int argb) {
        return (byte) ((argb & RED_MASK) >>> RED_SHIFT);
    }

    public static byte green(int argb) {
        return (byte) ((argb & GREEN_MASK) >>> GREEN_SHIFT);
    }

    public static byte blue(int argb) {
        return (byte) (argb & BLUE_MASK);
    }

    /**
     * Hue of a color from 0-360
     *
     * @param rgb
     * @return Hue value from 0-360
     */
    public static float h(int rgb) {
        int r = (rgb & RED_MASK) >> RED_SHIFT;
        int g = (rgb & GREEN_MASK) >> GREEN_SHIFT;
        int b = rgb & BLUE_MASK;
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
        float range = max - min;
        float h;
        float rc = (max - r) / range;
        float gc = (max - g) / range;
        float bc = (max - b) / range;
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
        return 360.f * h;
    }

    /**
     * Saturation from 0-100
     *
     * @param rgb
     * @return Saturation value from 0-100
     */
    public static float s(int rgb) {
        int r = (rgb & RED_MASK) >> RED_SHIFT;
        int g = (rgb & GREEN_MASK) >> GREEN_SHIFT;
        int b = rgb & BLUE_MASK;
        int max = (r > g) ? r : g;
        if (b > max) {
            max = b;
        }
        int min = (r < g) ? r : g;
        if (b < min) {
            min = b;
        }
        return (max == 0) ? 0 : (max - min) * 100.f / max;
    }

    /**
     * Brightness from 0-100
     *
     * @param rgb
     * @return Brightness from 0-100
     */
    public static float b(int rgb) {
        int r = (rgb & RED_MASK) >> RED_SHIFT;
        int g = (rgb & GREEN_MASK) >> GREEN_SHIFT;
        int b = rgb & BLUE_MASK;
        int max = (r > g) ? r : g;
        if (b > max) {
            max = b;
        }
        return 100.f * max / 255.f;
    }

    /**
     * Utility to create a color from double values
     *
     * @param h Hue
     * @param s Saturation
     * @param b Brightness
     * @return Color value
     */
    public static final int hsb(double h, double s, double b) {
        return hsb((float) h, (float) s, (float) b);
    }

    /**
     * Utility to create a color from double values
     *
     * @param h Hue
     * @param s Saturation
     * @param b Brightness
     * @param a Alpha
     * @return Color value
     */
    public static final int hsba(double h, double s, double b, double a) {
        return hsba((float) h, (float) s, (float) b, (float) a);
    }

    /**
     * Create a color from HSB
     *
     * @param h Hue from 0-360
     * @param s Saturation from 0-100
     * @param b Brightness from 0-100
     * @return rgb color value
     */
    public static int hsb(float h, float s, float b) {
        return Color.HSBtoRGB((h % 360) / 360.f, s / 100.f, b / 100.f);
    }

    /**
     * Create a color from HSB
     *
     * @param h Hue from 0-360
     * @param s Saturation from 0-100
     * @param b Brightness from 0-100
     * @param a Alpha from 0-1
     * @return rgb color value
     */
    public static int hsba(float h, float s, float b, float a) {
        return
            (max(0xff, (int) (a * 0xff)) << ALPHA_SHIFT) |
            (hsb(h, s, b) & 0x00ffffff);
    }

    /**
     * Scales the brightness of an array of colors by some factor
     *
     * @param rgbs Array of color values
     * @param s Factor by which to scale brightness
     * @return Array of new color values
     */
    public static int[] scaleBrightness(int[] rgbs, float s) {
        int[] result = new int[rgbs.length];
        scaleBrightness(rgbs, s, result);
        return result;
    }

    /**
     * Scales the brightness of an array of colors by some factor
     *
     * @param rgbs Array of color values
     * @param s Factor by which to scale brightness
     * @param result Array to write results into, if null, input array is modified
     */
    public static void scaleBrightness(int[] rgbs, float s, int[] result) {
        int r, g, b, rgb;
        float[] hsb = new float[3];
        if (result == null) {
            result = rgbs;
        }
        for (int i = 0; i < rgbs.length; ++i) {
            rgb = rgbs[i];
            r = (rgb & RED_MASK) >> RED_SHIFT;
            g = (rgb & GREEN_MASK) >> GREEN_SHIFT;
            b = rgb & BLUE_MASK;
            Color.RGBtoHSB(r, g, b, hsb);
            result[i] = Color.HSBtoRGB(hsb[0], hsb[1], Math.min(1, hsb[2] * s));
        }
    }

    /**
     * Scales the brightness of a color by a factor
     *
     * @param rgb Color value
     * @param s Factory by which to scale brightness
     * @return New color
     */
    public static int scaleBrightness(int rgb, float s) {
        int r = (rgb & RED_MASK) >> RED_SHIFT;
        int g = (rgb & GREEN_MASK) >> GREEN_SHIFT;
        int b = rgb & BLUE_MASK;
        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        return Color.HSBtoRGB(hsb[0], hsb[1], Math.min(1, hsb[2] * s));
    }

    /**
     * Utility function to invoke Color.RGBtoHSB without requiring the caller to
     * manually unpack bytes from an integer color.
     *
     * @param rgb ARGB integer color
     * @param hsb Array into which results should be placed
     * @return Array of hsb values, or null if hsb parameter was provided
     */
    public static float[] RGBtoHSB(int rgb, float[] hsb) {
        int r = (rgb & RED_MASK) >> RED_SHIFT;
        int g = (rgb & GREEN_MASK) >> GREEN_SHIFT;
        int b = rgb & BLUE_MASK;
        return Color.RGBtoHSB(r, g, b, hsb);
    }

    public static int blend(int c1, int c2) {
        return blend(c1, c2, Blend.LERP);
    }

    public static int blend(int c1, int c2, Blend blendMode) {
        switch (blendMode) {
        case ADD:
            return add(c1, c2);
        case SUBTRACT:
            return subtract(c1, c2);
        case MULTIPLY:
            return multiply(c1, c2);
        case SCREEN:
            return screen(c1, c2);
        case LIGHTEST:
            return lightest(c1, c2);
        case DARKEST:
            return darkest(c1, c2);
        case LERP:
            return lerp(c1, c2);
        }
        throw new RuntimeException("Unimplemented blend mode: " + blendMode);
    }

    public static int lerp(int c1, int c2) {
        return lerp(c1, c2, (c2 & ALPHA_MASK) >>> ALPHA_SHIFT);
    }

    public static int lerp(int c1, int c2, float amount) {
        return lerp(c1, c2, (int) (amount * 0xff));
    }

    public static int lerp(int c1, int c2, double amount) {
        return lerp(c1, c2, (int) (amount * 0xff));
    }

    public static int lerp(int c1, int c2, int alpha) {
        int c1a = (c1 & ALPHA_MASK) >>> ALPHA_SHIFT;
        int c2a = (c2 & ALPHA_MASK) >>> ALPHA_SHIFT;
        return
            (min(0xff, c1a + c2a) << ALPHA_SHIFT) |
            lerp(c1, c2, alpha, RED_MASK) |
            lerp(c1, c2, alpha, GREEN_MASK) |
            lerp(c1, c2, alpha, BLUE_MASK);
    }

    public static int add(int c1, int c2) {
        int c1a = (c1 & ALPHA_MASK) >>> ALPHA_SHIFT;
        int c2a = (c2 & ALPHA_MASK) >>> ALPHA_SHIFT;
        return
            (min(0xff, c1a + c2a) << ALPHA_SHIFT) |
            (min(RED_MASK, (c1 & RED_MASK) + (((c2 & RED_MASK) * (c2a + 1)) >>> 8)) & RED_MASK) |
            (min(GREEN_MASK, (c1 & GREEN_MASK) + (((c2 & GREEN_MASK) * (c2a + 1)) >>> 8)) & GREEN_MASK) |
            min(BLUE_MASK, (c1 & BLUE_MASK) + (((c2 & BLUE_MASK) * (c2a + 1)) >>> 8));
    }

    public static int subtract(int c1, int c2) {
        int c1a = (c1 & ALPHA_MASK) >>> ALPHA_SHIFT;
        int c2a = (c2 & ALPHA_MASK) >>> ALPHA_SHIFT;
        return
            (min(0xff, c1a + c2a) << ALPHA_SHIFT) |
            (max(GREEN_MASK, (c1 & RED_MASK) - (((c2 & RED_MASK) * (c2a + 1)) >>> 8)) & RED_MASK) |
            (max(BLUE_MASK, (c1 & GREEN_MASK) - (((c2 & GREEN_MASK) * (c2a + 1)) >>> 8)) & GREEN_MASK) |
            max(0, (c1 & BLUE_MASK) - (((c2 & BLUE_MASK) * (c2a + 1)) >>> 8));
    }

    public static int multiply(int c1, int c2) {
        int c1a = (c1 & ALPHA_MASK) >>> ALPHA_SHIFT;
        int c2a = (c2 & ALPHA_MASK) >>> ALPHA_SHIFT;
        int c1r = (c1 & RED_MASK) >> RED_SHIFT;
        int c2r = (c2 & RED_MASK) >> RED_SHIFT;
        int c1g = (c1 & GREEN_MASK) >> GREEN_SHIFT;
        int c2g = (c2 & GREEN_MASK) >> GREEN_SHIFT;
        int c1b = c1 & BLUE_MASK;
        int c2b = c2 & BLUE_MASK;
        return
            (min(0xff, c1a + c2a) << ALPHA_SHIFT) |
            lerp(c1, (c1r * c2r) << 8, c2a, RED_MASK) |
            lerp(c1, (c1g * c2g), c2a, GREEN_MASK) |
            lerp(c1, (c1b * c2b) >> 8, c2a, BLUE_MASK);
    }

    public static int screen(int c1, int c2) {
        int c1a = (c1 & ALPHA_MASK) >>> ALPHA_SHIFT;
        int c2a = (c2 & ALPHA_MASK) >>> ALPHA_SHIFT;
        int c1r = (c1 & RED_MASK) >> RED_SHIFT;
        int c2r = (c2 & RED_MASK) >> RED_SHIFT;
        int c1g = (c1 & GREEN_MASK) >> GREEN_SHIFT;
        int c2g = (c2 & GREEN_MASK) >> GREEN_SHIFT;
        int c1b = c1 & BLUE_MASK;
        int c2b = c2 & BLUE_MASK;
        return
            (min(0xff, c1a + c2a) << ALPHA_SHIFT) |
            lerp(c1, RED_MASK - ((0xff - c1r) * (0xff - c2r) << 8), c2a, RED_MASK) |
            lerp(c1, GREEN_MASK - ((0xff - c1g) * (0xff - c2g)), c2a, GREEN_MASK) |
            lerp(c1, BLUE_MASK - (((0xff - c1b) * (0xff - c2b)) >> 8), c2a, BLUE_MASK);
    }

    public static int lightest(int c1, int c2) {
        int c1a = (c1 & ALPHA_MASK) >>> ALPHA_SHIFT;
        int c2a = (c2 & ALPHA_MASK) >>> ALPHA_SHIFT;
        return
            (min(0xff, c1a + c2a) << ALPHA_SHIFT) |
            (max(c1 & RED_MASK, (((c2 & RED_MASK) * (c2a + 1)) >>> 8)) & RED_MASK) |
            (max(c1 & GREEN_MASK, (((c2 & GREEN_MASK) * (c2a + 1)) >>> 8)) & GREEN_MASK) |
            max(c1 & BLUE_MASK, (((c2 & BLUE_MASK) * (c2a + 1)) >>> 8));
    }

    public static int darkest(int c1, int c2) {
        int c1a = (c1 & ALPHA_MASK) >>> ALPHA_SHIFT;
        int c2a = (c2 & ALPHA_MASK) >>> ALPHA_SHIFT;
        return
            (min(0xff, c1a + c2a) << ALPHA_SHIFT) |
            lerp(c1, min(c1 & RED_MASK, c2 & RED_MASK), c2a, RED_MASK) |
            lerp(c1, min(c1 & GREEN_MASK, c2 & GREEN_MASK), c2a, GREEN_MASK) |
            lerp(c1, min(c1 & BLUE_MASK, c2 & BLUE_MASK), c2a, BLUE_MASK);
    }

    private static int min(int a, int b) {
        return (a < b) ? a : b;
    }

    private static int max(int a, int b) {
        return (a > b) ? a : b;
    }

    private static int lerp(int a, int b, int alpha, int mask) {
        int am = a & mask, bm = b & mask;
        return (am + (((alpha+1)*(bm-am)) >>> 8)) & mask;
    }

}
