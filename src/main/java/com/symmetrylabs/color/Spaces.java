package com.symmetrylabs.color;

/** Conversions between color spaces. */
public class Spaces {
    private Spaces() {
        throw new UnsupportedOperationException("This is a static utility class");
    }

    // Lookup tables for srgbIntensityToValue and srgbValueToIntensity.
    private static int[] srgbValues = null;
    private static int[] srgbIntensities = null;

    // Using a cutoff of 0.0031308 as recommended by https://en.wikipedia.org/wiki/SRGB
    // creates a discontinuity at the cutoff point of up to 3e-08.  Using a cutoff
    // of 0.003130668442501 ensures that the discontinuity is less than 1e-16.
    private static double SRGB_TRANSFER_CUTOFF = 0.003130668442501;

    /**
     * Converts an sRGB colour channel value (v, ranging from 0 to 1)
     * to a linear intensity (also ranging from 0 to 1).
     *
     * Tuned to guarantee that:
     *     {@code f(0.0)} gives 0.0 exactly
     *     {@code f(1.0)} gives 1.0 exactly
     *     {@code 0.0 <= f(v) <= 1.0} for all double-precision values {@code 0.0 <= v <= 1.0}
     *     {@code f(v2) >= f(v1)} for all double-precision values {@code v1} and {@code v2 > v1}
     */
    public static double srgbValueToIntensity(double v) {
        // See https://en.wikipedia.org/wiki/SRGB#The_reverse_transformation
        return v <= (12.92 * SRGB_TRANSFER_CUTOFF) ? v / 12.92 :
                Math.pow((v + 0.055) / 1.055, 2.4);
    }

    /**
     * Converts an sRGB colour channel value (v, ranging from 0 to 1)
     * to a linear intensity (also ranging from 0 to 1).
     *
     * Tuned to guarantee that:
     *     {@code f(0.0)} gives 0.0 exactly
     *     {@code f(1.0)} gives 1.0 exactly
     *     {@code 0.0 <= f(i) <= 1.0} for all double-precision values {@code 0.0 <= i <= 1.0}
     *     {@code f(i2) > f(i1) - 1e16} for all double-precision values {@code i1} and {@code i2 > i1}
     */
    public static double srgbIntensityToValue(double i) {
        // See https://en.wikipedia.org/wiki/SRGB#The_forward_transformation_(CIE_XYZ_to_sRGB)
        // Due to floating-point error, 1.055 - 1.0 does not yield 0.055; we define
        // OFFSET this way so that 1.055 - OFFSET yields exactly 1.0.
        final double OFFSET = 1.055 - 1.0;
        return i <= SRGB_TRANSFER_CUTOFF ? 12.92 * i :
                1.055 * Math.pow(i, 1.0 / 2.4) - OFFSET;
    }

    private static void initSrgbValues() {
        if (srgbValues == null) {
            srgbValues = new int[65536];
            for (int i = 0; i < 65536; i++) {
                srgbValues[i] = (int) (srgbIntensityToValue(i/65535.0) * 255 + 0.5);
            }
        }
    }

    private static void initSrgbIntensities() {
        if (srgbIntensities == null) {
            srgbIntensities = new int[256];
            for (int i = 0; i < 256; i++) {
                srgbIntensities[i] = (int) (srgbValueToIntensity(i/255.0) * 65535 + 0.5);
            }
        }
    }

    /**
     * Converts a CIELAB perceived lightness value (L, ranging from 0 to 1)
     * to a CIEXYZ linear luminance value (Y, also ranging from 0 to 1).
     *
     * Tuned to guarantee that:
     *     {@code f(0.0)} gives 0.0 exactly
     *     {@code f(1.0)} gives 1.0 exactly
     *     {@code 0.0 <= f(l) <= 1.0} for all double-precision values {@code 0.0 <= l <= 1.0}
     *     {@code f(l2) >= f(l1)} for all double-precision values {@code l1} and {@code l2 > l1}
     */
    public static double cie_lightness_to_luminance(double l) {
        // See https://en.wikipedia.org/wiki/CIELAB_color_space#Reverse_transformation
        // The values of t and delta have been scaled up by 29 to avoid
        // floating-point error.  This formulation is designed to yield
        // exactly 0.0 and 1.0 for inputs of 0.0 and 1.0, and to make it
        // easy to see that both parts have the same value and same first
        // derivative at the crossover point where t = 6 (l = 0.08).
        double t = l * 25 + 4;  // t ranges from 4 to 29
        return (t > 6 ? t * t * t : 3*(t - 4) * 6 * 6) / (29 * 29 * 29);
    }

    public static long rgb8ToRgb16(int rgb8) {
        // If we were to shift left by 8, then 0xff would become 0xff00.
        // Instead, we multiply by 0x0101, so that 0xff becomes 0xffff.
        return Ops16.rgba(
                Ops8.red(rgb8) * 0x0101,
                Ops8.green(rgb8) * 0x0101,
                Ops8.blue(rgb8) * 0x0101,
                Ops8.alpha(rgb8) * 0x0101
        );
    }

    public static int rgb16ToRgb8(long rgb16) {
        return Ops8.rgba(
                Ops16.red(rgb16) >>> 8,
                Ops16.green(rgb16) >>> 8,
                Ops16.blue(rgb16) >>> 8,
                Ops16.alpha(rgb16) >>> 8
        );
    }

    public static long srgb8ToRgb16(int srgb8) {
        initSrgbIntensities();
        // We also convert the alpha channel, so that blending (0, 0, 0, 0) + (1, 1, 1, 0.5)
        // in RGB16 space gives the same result as blending their analogues in SRGB8 space.
        return Ops16.rgba(
                srgbIntensities[Ops8.red(srgb8)],
                srgbIntensities[Ops8.green(srgb8)],
                srgbIntensities[Ops8.blue(srgb8)],
                srgbIntensities[Ops8.alpha(srgb8)]
        );
    }

    public static int rgb16ToSrgb8(long rgb16) {
        initSrgbValues();
        // We also convert the alpha channel, so that blending (0, 0, 0, 0) + (1, 1, 1, 0.5)
        // in SRGB8 space gives the same result as blending their analogues in RGB16 space.
        return Ops8.rgba(
                srgbValues[Ops16.red(rgb16)],
                srgbValues[Ops16.green(rgb16)],
                srgbValues[Ops16.blue(rgb16)],
                srgbValues[Ops16.alpha(rgb16)]
        );
    }

    public static int rgb8ToSrgb8(int rgb8) {
        return rgb16ToSrgb8(rgb8ToRgb16(rgb8));
    }

    public static int srgb8ToRgb8(int srgb8) {
        return rgb16ToRgb8(srgb8ToRgb16(srgb8));
    }

    public static void rgb8ToRgb16(int[] rgb8s, long[] rgb16s) {
        for (int i = 0; i < rgb8s.length; i++) rgb16s[i] = rgb8ToRgb16(rgb8s[i]);
    }

    public static void rgb16ToRgb8(long[] rgb16s, int[] rgb8s) {
        for (int i = 0; i < rgb16s.length; i++) rgb8s[i] = rgb16ToRgb8(rgb16s[i]);
    }

    public static void srgb8ToRgb16(int[] srgb8s, long[] rgb16s) {
        for (int i = 0; i < srgb8s.length; i++) rgb16s[i] = srgb8ToRgb16(srgb8s[i]);
    }

    public static void rgb16ToSrgb8(long[] rgb16s, int[] srgb8s) {
        for (int i = 0; i < rgb16s.length; i++) srgb8s[i] = rgb16ToSrgb8(rgb16s[i]);
    }

    public static void rgb8ToSrgb8(int[] rgb8s, int[] srgb8s) {
        for (int i = 0; i < rgb8s.length; i++) srgb8s[i] = rgb8ToSrgb8(rgb8s[i]);
    }

    public static void srgb8ToRgb8(int[] srgb8s, int[] rgb8s) {
        for (int i = 0; i < srgb8s.length; i++) rgb8s[i] = srgb8ToRgb8(srgb8s[i]);
    }
}
