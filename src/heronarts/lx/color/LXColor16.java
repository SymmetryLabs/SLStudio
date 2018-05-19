package heronarts.lx.color;

/** Various utilities that operate on 64-bit integers representing RGBA colors */
public class LXColor16 {
    private LXColor16() {
        throw new UnsupportedOperationException("This is a static utility class");
    }

    public static final long BLACK = 0xffff_0000_0000_0000L;
    public static final long WHITE = 0xffff_ffff_ffff_ffffL;

    public static int alpha(long argb) { return (int) ((argb & 0xffff_0000_0000_0000L) >>> 48); }
    public static int red(long argb) { return (int) ((argb & 0x0000_ffff_0000_0000L) >>> 32); }
    public static int green(long argb) { return (int) ((argb & 0x0000_0000_ffff_0000L) >>> 16); }
    public static int blue(long argb) { return (int) (argb & 0x0000_0000_0000_ffffL); }
    private static long clamp(int x) { return x < 0 ? 0 : x > 65535 ? 65535 : x; }

    public static long rgba(int r, int g, int b, int a) {
        return (clamp(a) << 48) | (clamp(r) << 32) | (clamp(g) << 16) | clamp(b);
    }

    /** Converts a brightness value (0 to 100) to a gray RGB16 color value. */
    public static long gray(double brightness) {
        int v = (int) clamp((int) ((brightness / 100) * 65535 + 0.5));
        return rgba(v, v, v, 0xffff);
    }

    /**
     * Computes an RGB16 color value from a hue (0 to 360), saturation (0 to 100),
     * and brightness (0 to 100).
     */
    public static long hsb(float hue360, float sat100, float brt100) {
        return fromHsb(hue360 / 360, sat100 / 100, brt100 / 100);
    }

    /** Computes an RGB16 color value given HSB components (all from 0.0 to 1.0). */
    public static long fromHsb(float hue, float saturation, float brightness) {
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
        return rgba(r, g, b, 0xffff);
    }

    /** Converts an RGB16 color to HSB components (all floats from 0.0 to 1.0). */
    public static float[] toHsb(long rgb16, float[] hsb) {
        float hue = 0, saturation = 0, brightness = 0;
        int r = red(rgb16), g = green(rgb16), b = blue(rgb16);
        int cmin = (r < g && r < b) ? r : (g < b ? g : b);
        int cmax = (r > g && r > b) ? r : (g > b ? g : b);

        brightness = (float) cmax / 65535f;
        saturation = cmax == 0 ? 0 : (float) (cmax - cmin) / cmax;
        if (saturation > 0) {
            float redc = (float) (cmax - r) / (cmax - cmin);
            float greenc = (float) (cmax - g) / (cmax - cmin);
            float bluec = (float) (cmax - b) / (cmax - cmin);
            hue = (r == cmax ? bluec - greenc :
                         g == cmax ? 2 + redc - bluec :
                                                 4 + greenc - redc) / 6;
            if (hue < 0) hue += 1;
        }

        if (hsb == null) {
            hsb = new float[3];
        }
        hsb[0] = hue;
        hsb[1] = saturation;
        hsb[2] = brightness;
        return hsb;
    }

    /** Gets the HSB brightness component (from 0 to 100) from an RGB16 color. */
    public static float b(long rgb16) {
        int r = red(rgb16), g = green(rgb16), b = blue(rgb16);
        int max = (r > g && r > b) ? r : (g > b ? g : b);
        return max / 65535f * 100f;
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
    @Deprecated public static void toHsb(int rgb16, float[] hsb) { rejectInt(); }
    @Deprecated public static void b(int rgb16) { rejectInt(); }

    private static void rejectInt() {
        throw new UnsupportedOperationException("LXColor16 does not accept ints");
    }
}
