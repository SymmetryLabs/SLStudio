package heronarts.lx.color;

import heronarts.lx.LXUtils;

/** Various utilities that operate on 64-bit integers representing RGBA colors */
public class LXColor16 {
        public static final long ALPHA_MASK = 0xffff000000000000L;
        public static final long RED_MASK = 0x0000ffff00000000L;
        public static final long GREEN_MASK = 0x00000000ffff0000L;
        public static final long BLUE_MASK = 0x000000000000ffffL;

        public static final long BLACK = ALPHA_MASK;
        public static final long WHITE = ALPHA_MASK | RED_MASK | GREEN_MASK | BLUE_MASK;
        public static final long RED = ALPHA_MASK | RED_MASK;
        public static final long GREEN = ALPHA_MASK | GREEN_MASK;
        public static final long BLUE = ALPHA_MASK | BLUE_MASK;

        public static final int ALPHA_SHIFT = 48;
        public static final int RED_SHIFT = 32;
        public static final int GREEN_SHIFT = 16;

        protected static byte[] srgbValues = null;

        public static int alpha(long argb) {
                return (int) ((argb & ALPHA_MASK) >>> ALPHA_SHIFT);
        }

        public static int red(long argb) {
                return (int) ((argb & RED_MASK) >>> RED_SHIFT);
        }

        public static int green(long argb) {
                return (int) ((argb & GREEN_MASK) >>> GREEN_SHIFT);
        }

        public static int blue(long argb) {
                return (int) (argb & BLUE_MASK);
        }

        public static int toRgb8(long rgb16) {
                return LXColor.rgba(red(rgb16) >> 8, green(rgb16) >> 8, blue(rgb16) >> 8, alpha(rgb16) >> 8);
        }

        public static void toRgb8(long[] rgb16s, int[] rgb8s) {
                for (int i = 0; i < rgb16s.length; i++) {
                        rgb8s[i] = toRgb8(rgb16s[i]);
                }
        }

        public static int toSrgb8(long rgb16) {
                if (srgbValues == null) {
                        srgbValues = new byte[65536];
                        for (int i = 0; i < 65536; i++) {
                                // For these values of i, 0.0 <= i/65535.0 <= 1.0.
                                // For these inputs, srgb_intensity_to_value is guaranteed to return 0.0 <= v <= 1.0.
                                // 255.99999999999997 is the largest double-precision number less than 256,
                                // so (byte) (v * 255.99999999999997) will always return a value from 0 to 255.
                                srgbValues[i] = (byte) (LXUtils.srgb_intensity_to_value(i/65535.0) * 255.99999999999997);
                        }
                }
                // We remap the alpha channel too, so that blending (0, 0, 0, 0) + (1, 1, 1, 0.5) in
                // SRGB8 space gives the same result as blending their analogues in RGB16 space.
                return LXColor.rgba(
                        srgbValues[red(rgb16)], srgbValues[green(rgb16)],
                        srgbValues[blue(rgb16)], srgbValues[alpha(rgb16)]
                );
        }

        public static void toSrgb8(long[] rgb16s, int[] srgb8s) {
                for (int i = 0; i < rgb16s.length; i++) {
                        srgb8s[i] = toSrgb8(rgb16s[i]);
                }
        }


                /**
         * Hue of a color from 0-360
         *
         * @param rgb Color value
         * @return Hue value from 0-360
         */
        public static float h(long rgb) {
                int r = red(rgb);
                int g = green(rgb);
                int b = blue(rgb);
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
        public static float s(long rgb) {
                int r = red(rgb);
                int g = green(rgb);
                int b = blue(rgb);
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
        public static float b(long rgb) {
                int r = red(rgb);
                int g = green(rgb);
                int b = blue(rgb);
                int max = (r > g) ? r : g;
                if (b > max) {
                        max = b;
                }
                return 100f * max / 65535.0f;
        }

        public static long gray(double brightness) {
                int b = 0xffff & (int) (65535.0 * (brightness / 100.0));
                return rgb(b, b, b);
        }

        public static long gray(float brightness) {
                int b = 0xffff & (int) (65535.0f * (brightness / 100.0f));
                return rgb(b, b, b);
        }

        /**
         * Computes an RGB color value
         *
         * @param r Red 0-65535
         * @param g Green 0-65535
         * @param b Blue 0-65535
         * @return RGBA color value
         */
        public static long rgb(int r, int g, int b) {
                return rgba(r, g, b, 0xffff);
        }

        /**
         * Computes an RGB color value
         *
         * @param r Red 0-65535
         * @param g Green 0-65535
         * @param b Blue 0-65535
         * @param a Alpha 0-65535
         * @return RGBA color value
         */
        public static long rgba(int r, int g, int b, int a) {
                return ((long) (a & 0xffff) << ALPHA_SHIFT) |
                                ((long) (r & 0xffff) << RED_SHIFT) |
                                ((long) (g & 0xffff) << GREEN_SHIFT) |
                                ((long) b & 0xffff);
        }

        /**
         * Utility to create a color from double values
         *
         * @param h Hue from 0-360
         * @param s Saturation from 0-100
         * @param b Brightness from 0-100
         * @return RGBA color value
         */
        public static long hsb(double h, double s, double b) {
                return hsb((float) h, (float) s, (float) b);
        }

        /**
         * Utility to create a color from double values
         *
         * @param h Hue from 0-360
         * @param s Saturation from 0-100
         * @param b Brightness from 0-100
         * @param a Alpha from 0-1
         * @return RGBA color value
         */
        public static long hsba(double h, double s, double b, double a) {
                return hsba((float) h, (float) s, (float) b, (float) a);
        }

        private static final float H_COEFF = 1 / 360.f;
        private static final float S_COEFF = 1 / 100.f;
        private static final float B_COEFF = 1 / 100.f;

        /**
         * Create a color from HSB
         *
         * @param h Hue from 0-360
         * @param s Saturation from 0-100
         * @param b Brightness from 0-100
         * @return RGB color value
         */
        public static long hsb(float h, float s, float b) {
                return scaledHsbToRgb(h * H_COEFF, s * S_COEFF, b * B_COEFF);
        }

        /**
         * Create a color from HSB
         *
         * @param hue Hue from 0.0 - 1.0
         * @param saturation Saturation from 0.0 - 1.0
         * @param brightness Brightness from 0.0 - 1.0
         * @return RGB color value
         */
        public static long scaledHsbToRgb(float hue, float saturation, float brightness) {
                int r = 0, g = 0, b = 0;
                if (saturation == 0) {
                        r = g = b = (int) (brightness*65535.0f + 0.5f);
                } else {
                        float h = (hue - (float) Math.floor(hue))*6.0f;
                        float f = h - (float) java.lang.Math.floor(h);
                        float p = brightness*(1.0f - saturation);
                        float q = brightness*(1.0f - saturation*f);
                        float t = brightness*(1.0f - (saturation*(1.0f - f)));
                        switch ((int) h) {
                                case 0:
                                        r = (int) (brightness*65535.0f + 0.5f);
                                        g = (int) (t*65535.0f + 0.5f);
                                        b = (int) (p*65535.0f + 0.5f);
                                        break;
                                case 1:
                                        r = (int) (q*65535.0f + 0.5f);
                                        g = (int) (brightness*65535.0f + 0.5f);
                                        b = (int) (p*65535.0f + 0.5f);
                                        break;
                                case 2:
                                        r = (int) (p*65535.0f + 0.5f);
                                        g = (int) (brightness*65535.0f + 0.5f);
                                        b = (int) (t*65535.0f + 0.5f);
                                        break;
                                case 3:
                                        r = (int) (p*65535.0f + 0.5f);
                                        g = (int) (q*65535.0f + 0.5f);
                                        b = (int) (brightness*65535.0f + 0.5f);
                                        break;
                                case 4:
                                        r = (int) (t*65535.0f + 0.5f);
                                        g = (int) (p*65535.0f + 0.5f);
                                        b = (int) (brightness*65535.0f + 0.5f);
                                        break;
                                case 5:
                                        r = (int) (brightness*65535.0f + 0.5f);
                                        g = (int) (p*65535.0f + 0.5f);
                                        b = (int) (q*65535.0f + 0.5f);
                                        break;
                        }
                }
                return rgb(r, g, b);
        }

        /**
         * Convert an RGB color to hue, saturation, and brightness.
         * @return An array of HSB floats, each 0.0 to 1.0.
         */
        private static float[] rgbToScaledHsb(int r, int g, int b, float[] hsbvals) {
                float hue, saturation, brightness;
                if (hsbvals == null) {
                        hsbvals = new float[3];
                }
                int cmax = (r > g) ? r : g;
                if (b > cmax) cmax = b;
                int cmin = (r < g) ? r : g;
                if (b < cmin) cmin = b;

                brightness = ((float) cmax) / 65535.0f;
                if (cmax != 0)
                        saturation = ((float) (cmax - cmin)) / ((float) cmax);
                else
                        saturation = 0;
                if (saturation == 0)
                        hue = 0;
                else {
                        float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
                        float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
                        float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
                        if (r == cmax)
                                hue = bluec - greenc;
                        else if (g == cmax)
                                hue = 2.0f + redc - bluec;
                        else
                                hue = 4.0f + greenc - redc;
                        hue = hue / 6.0f;
                        if (hue < 0)
                                hue = hue + 1.0f;
                }
                hsbvals[0] = hue;
                hsbvals[1] = saturation;
                hsbvals[2] = brightness;
                return hsbvals;
        }

        /**
         * Create a color from HSA, where brightness is always full
         *
         * @param h Hue from 0-360
         * @param s Saturation from 0-100
         * @param a Alpha mask from 0-1
         * @return argb color value
         */
        public static long hsa(float h, float s, float a) {
                return hsba(h, s, 100, a);
        }

        /**
         * Create a color from HSBA
         *
         * @param h Hue from 0-360
         * @param s Saturation from 0-100
         * @param b Brightness from 0-100
         * @param a Alpha from 0-1
         * @return argb color value
         */
        public static long hsba(float h, float s, float b, float a) {
                return ((long) min(65535, (int) (a*65535.0f)) << ALPHA_SHIFT) | hsb(h, s, b);
        }

        /**
         * Scales the brightness of an array of colors by some factor
         *
         * @param rgbs Array of color values
         * @param s Factor by which to scale brightness
         * @return Array of new color values
         */
        @Deprecated
        public static long[] scaleBrightness(long[] rgbs, float s) {
                long[] result = new long[rgbs.length];
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
        public static void scaleBrightness(long[] rgbs, float s, long[] result) {
                int r, g, b;
                long rgb;
                float[] hsb = new float[3];
                if (result == null) {
                        result = rgbs;
                }
                for (int i = 0; i < rgbs.length; ++i) {
                        rgb = rgbs[i];
                        r = red(rgb);
                        g = green(rgb);
                        b = blue(rgb);
                        rgbToScaledHsb(r, g, b, hsb);
                        result[i] = scaledHsbToRgb(hsb[0], hsb[1], Math.min(1, hsb[2] * s));
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
        public static long scaleBrightness(long rgb, float s) {
                int r = red(rgb);
                int g = green(rgb);
                int b = blue(rgb);
                float[] hsb = rgbToScaledHsb(r, g, b, null);
                return scaledHsbToRgb(hsb[0], hsb[1], Math.min(1, hsb[2] * s));
        }

        /**
         * Utility function to convert an RGB color value to HSB values.
         *
         * @param rgb ARGB integer color
         * @param hsb Array into which results should be placed
         * @return Array of hsb values, all in the range 0.0 to 1.0.
         */
        public static float[] RGBtoHSB(long rgb, float[] hsb) {
                int r = red(rgb);
                int g = green(rgb);
                int b = blue(rgb);
                return rgbToScaledHsb(r, g, b, hsb);
        }

        /**
         * Blends the two colors using specified blend based on the alpha channel of c2
         *
         * @param c1 First color
         * @param c2 Second color to be blended
         * @param blendMode Type of blending
         * @return Blended color
         */
        public static long blend(long c1, long c2, LXColor.Blend blendMode) {
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

        /**
         * Interpolates each of the RGB channels between c1 and c2
         *
         * @param c1 First color
         * @param c2 Second color
         * @return Interpolated color based on alpha of c2
         */
        public static long lerp(long c1, long c2) {
                return lerp(c1, c2, (c2 & ALPHA_MASK) >>> ALPHA_SHIFT);
        }

        /**
         * Interpolates each of the RGB channels between c1 and c2 and specified amount
         *
         * @param c1 First color
         * @param c2 Second color
         * @param amount Float from 0-1 for amount of interpolation
         * @return Interpolated color
         */
        public static long lerp(long c1, long c2, float amount) {
                return lerp(c1, c2, (int) (amount * 65535.0f));
        }

        /**
         * Interpolates each of the RGB channels between c1 and c2 and specified amount
         *
         * @param c1 First color
         * @param c2 Second color
         * @param amount Double from 0-1 for amount of interpolation
         * @return Interpolated color
         */
        public static long lerp(long c1, long c2, double amount) {
                return lerp(c1, c2, (int) (amount * 65535.0));
        }

        /**
         * Interpolates each of the RGB channels between c1 and c2 and specified alpha
         *
         * @param c1 First color
         * @param c2 Second color
         * @param alpha Single byte (0-255) alpha channel
         * @return Interpolated color
         */
        public static long lerp(long c1, long c2, int alpha) {
                int c1a = alpha(c1);
                int c2a = alpha(c2);
                return
                        (((long) min(0xffff, c1a + c2a)) << ALPHA_SHIFT) |
                                lerp(c1, c2, alpha, RED_MASK) |
                                lerp(c1, c2, alpha, GREEN_MASK) |
                                lerp(c1, c2, alpha, BLUE_MASK);
        }

        /**
         * Adds the specified colors
         *
         * @param c1 First color
         * @param c2 Second color
         * @return Summed RGB channels, clipped at 65535
         */
        public static long add(long c1, long c2) {
                long c1a = alpha(c1);
                long c2a = alpha(c2);
                return
                        (min(0xffffL, (long) (c1a + c2a)) << ALPHA_SHIFT) |
                                (min(RED_MASK, (c1 & RED_MASK) + (((c2 & RED_MASK) * (c2a + 1)) >>> 16)) & RED_MASK) |
                                (min(GREEN_MASK, (c1 & GREEN_MASK) + (((c2 & GREEN_MASK) * (c2a + 1)) >>> 16)) & GREEN_MASK) |
                                (min(BLUE_MASK, (c1 & BLUE_MASK) + (((c2 & BLUE_MASK) * (c2a + 1)) >>> 16)) & BLUE_MASK);
        }

        /**
         * Subtracts the specified colors
         *
         * @param c1 First color
         * @param c2 Second color
         * @return Color that is [c1 - c2] per RGB with 0-clip
         */
        public static long subtract(long c1, long c2) {
                long c1a = alpha(c1);
                long c2a = alpha(c2);
                return
                        (min(0xffffL, (long) c1a + c2a) << ALPHA_SHIFT) |
                                (max(GREEN_MASK, (c1 & RED_MASK) - (((c2 & RED_MASK) * (c2a + 1)) >>> 16)) & RED_MASK) |
                                (max(BLUE_MASK, (c1 & GREEN_MASK) - (((c2 & GREEN_MASK) * (c2a + 1)) >>> 16)) & GREEN_MASK) |
                                max(0, (c1 & BLUE_MASK) - (((c2 & BLUE_MASK) * (c2a + 1)) >>> 16));
        }

        /**
         * Multiplies the specified colors
         *
         * @param c1 First color
         * @param c2 Second color
         * @return RGB channels multiplied with 255 clip
         */
        public static long multiply(long c1, long c2) {
                int c1a = alpha(c1);
                int c2a = alpha(c2);
                long c1r = red(c1);
                long c2r = red(c2);
                long c1g = green(c1);
                long c2g = green(c2);
                long c1b = blue(c1);
                long c2b = blue(c2);
                return
                        (min(0xffffL, (long) c1a + c2a) << ALPHA_SHIFT) |
                                lerp(c1, (c1r * (c2r+1)) << 16, c2a, RED_MASK) |
                                lerp(c1, (c1g * (c2g+1)), c2a, GREEN_MASK) |
                                lerp(c1, (c1b * (c2b+1)) >> 16, c2a, BLUE_MASK);
        }

        /**
         * Inverse multiplies the specified colors
         *
         * @param c1 First color
         * @param c2 Second color
         * @return RGB channels multiplied as 255 - [255-c1]*[255-c2] with clip
         */
        public static long screen(long c1, long c2) {
                int c1a = alpha(c1);
                int c2a = alpha(c2);
                int c1r = red(c1);
                int c2r = red(c2);
                int c1g = green(c1);
                int c2g = green(c2);
                int c1b = blue(c1);
                int c2b = blue(c2);
                return
                        (min(0xffffL, (long) c1a + c2a) << ALPHA_SHIFT) |
                                lerp(c1, RED_MASK - ((0xffff - c1r) * (0xffff - c2r + 1) << 16), c2a, RED_MASK) |
                                lerp(c1, GREEN_MASK - ((0xffff - c1g) * (0xffff - c2g + 1)), c2a, GREEN_MASK) |
                                lerp(c1, BLUE_MASK - (((0xffff - c1b) * (0xffff - c2b + 1)) >> 16), c2a, BLUE_MASK);
        }

        /**
         * Returns the lightest color by RGB channel
         *
         * @param c1 First color
         * @param c2 Second color
         * @return Lightest of each RGB channel
         */
        public static long lightest(long c1, long c2) {
                int c1a = alpha(c1);
                int c2a = alpha(c2);
                return
                        (min(0xffffL, (long) c1a + c2a) << ALPHA_SHIFT) |
                                (max(c1 & RED_MASK, (((c2 & RED_MASK) * (c2a + 1)) >>> 16)) & RED_MASK) |
                                (max(c1 & GREEN_MASK, (((c2 & GREEN_MASK) * (c2a + 1)) >>> 16)) & GREEN_MASK) |
                                max(c1 & BLUE_MASK, (((c2 & BLUE_MASK) * (c2a + 1)) >>> 16));
        }

        /**
         * Returns the darkest color by RGB channel
         *
         * @param c1 First color
         * @param c2 Second color
         * @return Darkest of each RGB channel
         */
        public static long darkest(long c1, long c2) {
                int c1a = alpha(c1);
                int c2a = alpha(c2);
                return
                        (min(0xffffL, (long) c1a + c2a) << ALPHA_SHIFT) |
                                lerp(c1, min(c1 & RED_MASK, c2 & RED_MASK), c2a, RED_MASK) |
                                lerp(c1, min(c1 & GREEN_MASK, c2 & GREEN_MASK), c2a, GREEN_MASK) |
                                lerp(c1, min(c1 & BLUE_MASK, c2 & BLUE_MASK), c2a, BLUE_MASK);
        }

        private static long min(long a, long b) {
                return (a < b) ? a : b;
        }

        private static long max(long a, long b) {
                return (a > b) ? a : b;
        }

        private static int min(int a, int b) {
                return (a < b) ? a : b;
        }

        private static int max(int a, int b) {
                return (a > b) ? a : b;
        }

        /**
         * Extracts a channel from the input colors a and b using the given mask,
         * then linearly interpolates from a to b by the amount given by alpha
         * (which ranges from 0 to 65535), returning the result in the same channel.
         */
        private static long lerp(long a, long b, int alpha, long mask) {
                long am = a & mask, bm = b & mask;
                return (am + (((alpha + 1)*(bm - am)) >>> 16)) & mask;
        }

        // Java silently allows ints to be passed for arguments declared as long.
        // To prevent accidental use of 8-bit-per-channel color values where
        // 16-bit-per-channel color values are expected, we define an overloaded
        // method for every method that takes a long, and make it show up as
        // deprecated and throw exceptions at runtime.
        @Deprecated public static void alpha(int argb) { rejectInt(); }
        @Deprecated public static void red(int argb) { rejectInt(); }
        @Deprecated public static void green(int argb) { rejectInt(); }
        @Deprecated public static void blue(int argb) { rejectInt(); }
        @Deprecated public static void toInt(int argb) { rejectInt(); }
        @Deprecated public static void h(int rgb) { rejectInt(); }
        @Deprecated public static void s(int rgb) { rejectInt(); }
        @Deprecated public static void b(int rgb) { rejectInt(); }
        @Deprecated public static void RGBtoHSB(int rgb, float[] hsb) { rejectInt(); }
        @Deprecated public static void blend(int c1, long c2, LXColor.Blend blendMode) { rejectInt(); }
        @Deprecated public static void blend(long c1, int c2, LXColor.Blend blendMode) { rejectInt(); }
        @Deprecated public static void lerp(int c1, long c2) { rejectInt(); }
        @Deprecated public static void lerp(long c1, int c2) { rejectInt(); }
        @Deprecated public static void lerp(int c1, long c2, float amount) { rejectInt(); }
        @Deprecated public static void lerp(long c1, int c2, float amount) { rejectInt(); }
        @Deprecated public static void lerp(int c1, long c2, double amount) { rejectInt(); }
        @Deprecated public static void lerp(long c1, int c2, double amount) { rejectInt(); }
        @Deprecated public static void lerp(int c1, long c2, int alpha) { rejectInt(); }
        @Deprecated public static void lerp(long c1, int c2, int alpha) { rejectInt(); }
        @Deprecated public static void add(int c1, long c2) { rejectInt(); }
        @Deprecated public static void add(long c1, int c2) { rejectInt(); }
        @Deprecated public static void subtract(int c1, long c2) { rejectInt(); }
        @Deprecated public static void subtract(long c1, int c2) { rejectInt(); }
        @Deprecated public static void multiply(int c1, long c2) { rejectInt(); }
        @Deprecated public static void multiply(long c1, int c2) { rejectInt(); }
        @Deprecated public static void screen(int c1, long c2) { rejectInt(); }
        @Deprecated public static void screen(long c1, int c2) { rejectInt(); }
        @Deprecated public static void lightest(int c1, long c2) { rejectInt(); }
        @Deprecated public static void lightest(long c1, int c2) { rejectInt(); }
        @Deprecated public static void darkest(int c1, long c2) { rejectInt(); }
        @Deprecated public static void darkest(long c1, int c2) { rejectInt(); }
        @Deprecated public static void lerp(int c1, long c2, int alpha, long mask) { rejectInt(); }
        @Deprecated public static void lerp(long c1, int c2, int alpha, long mask) { rejectInt(); }

        private static void rejectInt() {
                throw new UnsupportedOperationException("LXColor16 does not accept ints");
        }
}
