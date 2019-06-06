/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.color;

import java.awt.Color;

/** Various utilities that operate on 32-bit integers representing RGBA colors */
public final class LXColor {
    private LXColor() {
        throw new UnsupportedOperationException("This is a static utility class");
    }

    /** Color blending modes */
    @Deprecated
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

    public static final int BLACK = ALPHA_MASK;
    public static final int WHITE = ALPHA_MASK | RED_MASK | GREEN_MASK | BLUE_MASK;
    public static final int RED = ALPHA_MASK | RED_MASK;
    public static final int GREEN = ALPHA_MASK | GREEN_MASK;
    public static final int BLUE = ALPHA_MASK | BLUE_MASK;

    public static final int ALPHA_SHIFT = 24;
    public static final int RED_SHIFT = 16;
    public static final int GREEN_SHIFT = 8;

    /** Deprecated: Result is signed (-128 to 127) and dangerous in color arithmetic. */
    @Deprecated
    public static byte alpha(int argb) {
        return alphaByteUnsafe(argb);
    }

    /** Deprecated: Result is signed (-128 to 127) and dangerous in color arithmetic. */
    @Deprecated
    public static byte red(int argb) {
        return redByteUnsafe(argb);
    }

    /** Deprecated: Result is signed (-128 to 127) and dangerous in color arithmetic. */
    @Deprecated
    public static byte green(int argb) {
        return greenByteUnsafe(argb);
    }

    /** Deprecated: Result is signed (-128 to 127) and dangerous in color arithmetic. */
    @Deprecated
    public static byte blue(int argb) {
        return blueByteUnsafe(argb);
    }

    /**
     * Return the byte representing the alpha channel in this color.
     *
     * Marked as unsafe because arithmetic on these values is almost guaranteed to be
     * incorrect; Java bytes are signed so high alpha values will end up being negative bytes.
     */
    public static byte alphaByteUnsafe(int argb) {
        return (byte) ((argb & ALPHA_MASK) >>> ALPHA_SHIFT);
    }

    /**
     * Return the byte representing the red channel in this color.
     *
     * Marked as unsafe because arithmetic on these values is almost guaranteed to be
     * incorrect; Java bytes are signed so high red values will end up being negative bytes.
     */
    public static byte redByteUnsafe(int argb) {
        return (byte) ((argb & RED_MASK) >>> RED_SHIFT);
    }

    /**
     * Return the byte representing the green channel in this color.
     *
     * Marked as unsafe because arithmetic on these values is almost guaranteed to be
     * incorrect; Java bytes are signed so high green values will end up being negative bytes.
     */
    public static byte greenByteUnsafe(int argb) {
        return (byte) ((argb & GREEN_MASK) >>> GREEN_SHIFT);
    }

    /**
     * Return the byte representing the blue channel in this color.
     *
     * Marked as unsafe because arithmetic on these values is almost guaranteed to be
     * incorrect; Java bytes are signed so high blue values will end up being negative bytes.
     */
    public static byte blueByteUnsafe(int argb) {
        return (byte) (argb & BLUE_MASK);
    }

    /**
     * Hue of a color from 0-360
     *
     * @param rgb Color value
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
        if (range == 0) {
            return 0;
        }
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
     * @param rgb Color value
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
     * @param rgb Color value
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

    /** Converts a brightness value (0 to 100) to a gray ARGB color value. */
    public static int gray(double brightness) {
        int b = 0xff & (int) (255 * (brightness / 100));
        return
            0xff000000 |
            ((b & 0xff) << RED_SHIFT) |
            ((b & 0xff) << GREEN_SHIFT) |
            (b & 0xff);
    }

    public static int gray(float brightness) {
        int b = 0xff & (int) (255 * (brightness / 100.f));
        return
            0xff000000 |
            ((b & 0xff) << RED_SHIFT) |
            ((b & 0xff) << GREEN_SHIFT) |
            (b & 0xff);
    }

    /** Packs R, G, and B values from 0 to 255 into an ARGB color value. */
    public static int rgb(int r, int g, int b) {
        return rgba(r, g, b, 255);
    }

    /** Packs R, G, B, and A values from 0 to 255 into an ARGB color value. */
    public static int rgba(int r, int g, int b, int a) {
        return
                ((a & 0xff) << ALPHA_SHIFT) |
                        ((r & 0xff) << RED_SHIFT) |
                        ((g & 0xff) << GREEN_SHIFT) |
                        (b & 0xff);
    }

    /**
     * Computes an ARGB color value from a hue (0 to 360), saturation (0 to 100),
     * and brightness (0 to 100).
     */
    public static int hsb(double h, double s, double b) {
        return hsb((float) h, (float) s, (float) b);
    }

    /**
     * Computes an ARGB color value from a hue (0 to 360), saturation (0 to 100),
     * brightness (0 to 100), and alpha (0.0 to 1.0).
     */
    public static int hsba(double h, double s, double b, double a) {
        return hsba((float) h, (float) s, (float) b, (float) a);
    }

    private static final float H_COEFF = 1 / 360.f;
    private static final float S_COEFF = 1 / 100.f;
    private static final float B_COEFF = 1 / 100.f;

    /**
     * Computes an ARGB color value from a hue (0 to 360), saturation (0 to 100),
     * and brightness (0 to 100).
     */
    public static int hsb(float h, float s, float b) {
        return _hsbImpl(h * H_COEFF, s * S_COEFF, b * B_COEFF);
    }

    /**
     * Computes an ARGB color value from a hue (0.0 to 1.0), saturation (0.0 to 1.0),
     * and brightness (0.0 to 1.0).
     */
    public static int _hsbImpl(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.f + 0.5f);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6.0f;
            float f = h - (float) java.lang.Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
            case 0:
                r = (int) (brightness * 255.0f + 0.5f);
                g = (int) (t * 255.0f + 0.5f);
                b = (int) (p * 255.0f + 0.5f);
                break;
            case 1:
                r = (int) (q * 255.0f + 0.5f);
                g = (int) (brightness * 255.0f + 0.5f);
                b = (int) (p * 255.0f + 0.5f);
                break;
            case 2:
                r = (int) (p * 255.0f + 0.5f);
                g = (int) (brightness * 255.0f + 0.5f);
                b = (int) (t * 255.0f + 0.5f);
                break;
            case 3:
                r = (int) (p * 255.0f + 0.5f);
                g = (int) (q * 255.0f + 0.5f);
                b = (int) (brightness * 255.0f + 0.5f);
                break;
            case 4:
                r = (int) (t * 255.0f + 0.5f);
                g = (int) (p * 255.0f + 0.5f);
                b = (int) (brightness * 255.0f + 0.5f);
                break;
            case 5:
                r = (int) (brightness * 255.0f + 0.5f);
                g = (int) (p * 255.0f + 0.5f);
                b = (int) (q * 255.0f + 0.5f);
                break;
            }
        }
        return 0xff000000 | (r << 16) | (g << 8) | (b << 0);
    }

    /**
     * Computes an ARGB color value from a hue (0 to 360), saturation (0 to 100),
     * and alpha (0.0 to 1.0).  Brightness is assumed to be 100%.
     */
    public static int hsa(float h, float s, float a) {
        return hsba(h, s, 100, a);
    }

    /**
     * Computes an ARGB color value from a hue (0 to 360), saturation (0 to 100),
     * brightness (0 to 100), and alpha (0.0 to 1.0).
     */
    public static int hsba(float h, float s, float b, float a) {
        return
            (min(0xff, (int) (a * 0xff)) << ALPHA_SHIFT) |
            (hsb(h, s, b) & 0x00ffffff);
    }

    /**
     * Scales the brightness of an array of colors by some factor
     *
     * @param rgbs Array of color values
     * @param s Factor by which to scale brightness
     * @return Array of new color values
     */
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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

    /**
     * Blends two colors using a given blend mode, based on the alpha channel of c2.
     * Deprecated: Use the LXBlend subclasses or lx.blend.Ops{8,16} instead.
     */
    @Deprecated
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

    /** Interpolates from c1 toward c2, by an amount according to the alpha of c2. */
    public static int lerp(int c1, int c2) {
        return lerp(c1, c2, (c2 & ALPHA_MASK) >>> ALPHA_SHIFT);
    }

    /** Interpolates from c1 toward c2, by an amount from 0.0 to 1.0. */
    public static int lerp(int c1, int c2, float amount) {
        return lerp(c1, c2, (int) (amount * 0xff));
    }

    /** Interpolates from c1 toward c2, by an amount from 0.0 to 1.0. */
    public static int lerp(int c1, int c2, double amount) {
        return lerp(c1, c2, (int) (amount * 0xff));
    }

    /** Interpolates from c1 toward c2, by an amount from 0 to 0xff. */
    public static int lerp(int c1, int c2, int alpha) {
        int c1a = (c1 & ALPHA_MASK) >>> ALPHA_SHIFT;
        int c2a = (c2 & ALPHA_MASK) >>> ALPHA_SHIFT;
        return
            (min(0xff, c1a + c2a) << ALPHA_SHIFT) |
            lerp(c1, c2, alpha, RED_MASK) |
            lerp(c1, c2, alpha, GREEN_MASK) |
            lerp(c1, c2, alpha, BLUE_MASK);
    }

    /** Adds the specified colors. */
    @Deprecated
    public static int add(int c1, int c2) {
        int c1a = (c1 & ALPHA_MASK) >>> ALPHA_SHIFT;
        int c2a = (c2 & ALPHA_MASK) >>> ALPHA_SHIFT;
        return
            (min(0xff, c1a + c2a) << ALPHA_SHIFT) |
            (min(RED_MASK, (c1 & RED_MASK) + (((c2 & RED_MASK) * (c2a + 1)) >>> 8)) & RED_MASK) |
            (min(GREEN_MASK, (c1 & GREEN_MASK) + (((c2 & GREEN_MASK) * (c2a + 1)) >>> 8)) & GREEN_MASK) |
            min(BLUE_MASK, (c1 & BLUE_MASK) + (((c2 & BLUE_MASK) * (c2a + 1)) >>> 8));
    }

    /** Subtracts the specified colors. */
    @Deprecated
    public static int subtract(int c1, int c2) {
        int c1a = (c1 & ALPHA_MASK) >>> ALPHA_SHIFT;
        int c2a = (c2 & ALPHA_MASK) >>> ALPHA_SHIFT;
        return
            (min(0xff, c1a + c2a) << ALPHA_SHIFT) |
            (max(GREEN_MASK, (c1 & RED_MASK) - (((c2 & RED_MASK) * (c2a + 1)) >>> 8)) & RED_MASK) |
            (max(BLUE_MASK, (c1 & GREEN_MASK) - (((c2 & GREEN_MASK) * (c2a + 1)) >>> 8)) & GREEN_MASK) |
            max(0, (c1 & BLUE_MASK) - (((c2 & BLUE_MASK) * (c2a + 1)) >>> 8));
    }

    /** Multiplies the specified colors. */
    @Deprecated
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
            lerp(c1, (c1r * (c2r+1)) << 8, c2a, RED_MASK) |
            lerp(c1, (c1g * (c2g+1)), c2a, GREEN_MASK) |
            lerp(c1, (c1b * (c2b+1)) >> 8, c2a, BLUE_MASK);
    }

    /** Inverse multiplies the specified colors. */
    @Deprecated
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
            lerp(c1, RED_MASK - ((0xff - c1r) * (0xff - c2r + 1) << 8), c2a, RED_MASK) |
            lerp(c1, GREEN_MASK - ((0xff - c1g) * (0xff - c2g + 1)), c2a, GREEN_MASK) |
            lerp(c1, BLUE_MASK - (((0xff - c1b) * (0xff - c2b + 1)) >> 8), c2a, BLUE_MASK);
    }

    /** Returns the lightest color by RGB channel. */
    @Deprecated
    public static int lightest(int c1, int c2) {
        int c1a = (c1 & ALPHA_MASK) >>> ALPHA_SHIFT;
        int c2a = (c2 & ALPHA_MASK) >>> ALPHA_SHIFT;
        return
            (min(0xff, c1a + c2a) << ALPHA_SHIFT) |
            (max(c1 & RED_MASK, (((c2 & RED_MASK) * (c2a + 1)) >>> 8)) & RED_MASK) |
            (max(c1 & GREEN_MASK, (((c2 & GREEN_MASK) * (c2a + 1)) >>> 8)) & GREEN_MASK) |
            max(c1 & BLUE_MASK, (((c2 & BLUE_MASK) * (c2a + 1)) >>> 8));
    }

    /** Returns the darkest color by RGB channel. */
    @Deprecated
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
