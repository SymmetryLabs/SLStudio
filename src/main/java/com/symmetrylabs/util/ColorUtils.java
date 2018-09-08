package com.symmetrylabs.util;

import com.symmetrylabs.color.Spaces;

import java.awt.color.ColorSpace;

import org.apache.commons.math3.util.FastMath;

import heronarts.lx.color.LXColor;

public final class ColorUtils {

    public static int setAlpha(int rgb, int alpha) {
        return (rgb & (~LXColor.ALPHA_MASK)) | ((alpha << LXColor.ALPHA_SHIFT) & LXColor.ALPHA_MASK);
    }

    public static int setAlpha(int rgb, float alpha) {
        return setAlpha(rgb, (int) (alpha * 0xff));
    }

    public static int setAlpha(int rgb, double alpha) {
        return setAlpha(rgb, (int) (alpha * 0xff));
    }

    public static int scaleAlpha(int argb, double s) {
        return setAlpha(argb, MathUtils.byteMultiply(LXColor.alpha(argb), s));
    }

    public static int subtractAlpha(int argb, int amount) {
        return setAlpha(argb, MathUtils.byteSubtract(LXColor.alpha(argb), amount));
    }

    public static void blend(int[] dst, int[] src) {
        for (int i = 0; i < src.length; i++) {
            dst[i] = ColorUtils.blend(dst[i], src[i]);
        }
    }

    public static int blend(int dst, int src) {
        float dstA = (dst >> 24 & 0xFF) / 255.0f;
        float srcA = (src >> 24 & 0xFF) / 255.0f;
        float outA = srcA + dstA * (1 - srcA);
        if (outA == 0)
            return 0;

        int outR = FastMath.round(((src >> 16 & 0xFF) * srcA + (dst >> 16 & 0xFF) * dstA * (1 - srcA)) / outA) & 0xFF;
        int outG = FastMath.round(((src >> 8 & 0xFF) * srcA + (dst >> 8 & 0xFF) * dstA * (1 - srcA)) / outA) & 0xFF;
        int outB = FastMath.round(((src & 0xFF) * srcA + (dst & 0xFF) * dstA * (1 - srcA)) / outA) & 0xFF;
        return (((int)(outA * 0xFF)) & 0xFF) << 24 | outR << 16 | outG << 8 | outB;
    }

    public static int max(int dst, int src) {
        int outA = FastMath.max(src >> 24 & 0xFF, dst >> 24 & 0xFF);
        int outR = FastMath.max(src >> 16 & 0xFF, dst >> 16 & 0xFF);
        int outG = FastMath.max(src >> 8 & 0xFF, dst >> 8 & 0xFF);
        int outB = FastMath.max(src & 0xFF, dst & 0xFF);
        return outA << 24 | outR << 16 | outG << 8 | outB;
    }

    public static int maxAlpha(int dst, int src) {
        return (src >> 24 & 0xFF) > (dst >> 24 & 0xFF) ? src : dst;
    }

    //private static ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);

    // mostly based on
    // http://www.brucelindbloom.com/index.html?Eqn_XYZ_to_RGB.html
    public static void xyz2rgb(float[] xyz, float[] rgb) {

        /*
        float tmp[] = cs.fromCIEXYZ(xyz);
        rgb[0] = tmp[0] / 255f;
        rgb[1] = tmp[1] / 255f;
        rgb[2] = tmp[2] / 255f;
        */

        final float eps = 216f / 24389f;
        final float k = 24389f / 27f;

        float r, g, b;

        // based on sRGB
        //r = xyz[0] *  3.2404542f + xyz[1] * -1.5371385f + xyz[2] * -0.4985314f;
        //g = xyz[0] * -0.9692660f + xyz[1] *  1.8760108f + xyz[2] *  0.0415560f;
        //b = xyz[0] *  0.0556434f + xyz[1] * -0.2040259f + xyz[2] *  1.0572252f;

        // sRGB gamma
        //r = (r > 0.0031308f) ? (1.055f * (float)FastMath.pow(r, 1 / 2.4f) - 0.055f) : 12.92f * r;
        //g = (g > 0.0031308f) ? (1.055f * (float)FastMath.pow(g, 1 / 2.4f) - 0.055f) : 12.92f * g;
        //b = (b > 0.0031308f) ? (1.055f * (float)FastMath.pow(b, 1 / 2.4f) - 0.055f) : 12.92f * b;

        // based on "Best RGB" colorspace
        r = xyz[0] *  1.7552599f + xyz[1] * -0.4836786f + xyz[2] * -0.2530000f;
        g = xyz[0] * -0.5441336f + xyz[1] *  1.5068789f + xyz[2] *  0.0215528f;
        b = xyz[0] *  0.0063467f + xyz[1] * -0.0175761f + xyz[2] *  1.2256959f;

        rgb[0] = (float)FastMath.pow(r, 1 / 2.2);
        rgb[1] = (float)FastMath.pow(g, 1 / 2.2);
        rgb[2] = (float)FastMath.pow(b, 1 / 2.2);

        //rgb[0] = r <= eps ? r * k / 100f : 1.16f * (float)FastMath.cbrt(r) - 0.16f;
        //rgb[1] = g <= eps ? g * k / 100f : 1.16f * (float)FastMath.cbrt(g) - 0.16f;
        //rgb[2] = b <= eps ? b * k / 100f : 1.16f * (float)FastMath.cbrt(b) - 0.16f;

        if (rgb[0] < 0) rgb[0] = 0;
        if (rgb[0] > 1) rgb[0] = 1;
        if (rgb[1] < 0) rgb[1] = 0;
        if (rgb[1] > 1) rgb[1] = 1;
        if (rgb[2] < 0) rgb[2] = 0;
        if (rgb[2] > 1) rgb[2] = 1;
    }

    public static void rgb2xyz(float[] rgb, float[] xyz) {

        /*
        float[] tmp = new float[3];
        tmp[0] = rgb[0] * 255f;
        tmp[1] = rgb[1] * 255f;
        tmp[2] = rgb[2] * 255f;
        float[] tmp2 = cs.toCIEXYZ(tmp);
        rgb[0] = tmp2[0] / 255f;
        rgb[1] = tmp2[1] / 255f;
        rgb[2] = tmp2[2] / 255f;
        */

        final float k = 24389f / 27f;

        float r, g, b;

        r = (float)FastMath.pow(rgb[0], 2.2);
        g = (float)FastMath.pow(rgb[1], 2.2);
        b = (float)FastMath.pow(rgb[2], 2.2);

        //r = rgb[0] <= 0.08f ? 100 * rgb[0] / k : (float)FastMath.pow((rgb[0] + 0.16f) / 1.16f, 3);
        //g = rgb[1] <= 0.08f ? 100 * rgb[1] / k : (float)FastMath.pow((rgb[1] + 0.16f) / 1.16f, 3);
        //b = rgb[2] <= 0.08f ? 100 * rgb[2] / k : (float)FastMath.pow((rgb[2] + 0.16f) / 1.16f, 3);

        // sRGB gamma
        //float r = rgb[0] <= 0.04045 ? rgb[0] / 12.92f : (float)FastMath.pow((rgb[0] + 0.055) / 1.055f, 2.4f)
        //float g = rgb[1] <= 0.04045 ? rgb[1] / 12.92f : (float)FastMath.pow((rgb[1] + 0.055) / 1.055f, 2.4f)
        //float b = rgb[2] <= 0.04045 ? rgb[2] / 12.92f : (float)FastMath.pow((rgb[2] + 0.055) / 1.055f, 2.4f)

        // based on sRGB
        //xyz[0] = r * 0.4124564f + g * 0.3575761f + b * 0.1804375f;
        //xyz[1] = r * 0.2126729f + g * 0.7151522f + b * 0.0721750f;
        //xyz[2] = r * 0.0193339f + g * 0.1191920f + b * 0.9503041f;

        // based on "Best RGB" colorspace
        xyz[0] = r * 0.6326696f + g * 0.2045558f + b * 0.1269946f;
        xyz[1] = r * 0.2284569f + g * 0.7373523f + b * 0.0341908f;
        xyz[2] = r * 0.0000000f + g * 0.0095142f + b * 0.8156958f;
    }

    public static void xyz2luv(float[] xyz, float[] luv) {
        final float X_r = 0.95047f;
        final float Y_r = 1.00000f;
        final float Z_r = 1.08883f;

        final float eps = 216f / 24389f;
        final float k = 24389f / 27f;

        float d = xyz[0] + 15 * xyz[1] + 3 * xyz[2];
        float U = 4 * xyz[0] / d;
        float V = 9 * xyz[1] / d;

        float d_r = X_r + 15 * Y_r + 3 * Z_r;
        float U_r = 4 * X_r / d_r;
        float V_r = 9 * Y_r / d_r;

        float y = xyz[1] / Y_r;
        float L = y > eps ? (116 * (float)FastMath.cbrt(y)) - 16 : k * y;
        float u = 13 * L * (U - U_r);
        float v = 13 * L * (V - V_r);

        luv[0] = L;
        luv[1] = u;
        luv[2] = v;
    }

    public static void luv2xyz(float[] luv, float[] xyz) {
        final float X_r = 0.95047f;
        final float Y_r = 1.00000f;
        final float Z_r = 1.08883f;

        final float eps = 216f / 24389f;
        final float k = 24389f / 27f;

        float L = luv[0];
        float u = luv[1];
        float v = luv[2];

        float d_r = X_r + 15 * Y_r + 3 * Z_r;
        float u_0 = 4 * X_r / d_r;
        float v_0 = 9 * Y_r / d_r;

        float Y;
        if (L > k * eps) {
            Y = (L + 16) / 116f;
            Y = Y * Y * Y;
        }
        else {
            Y = L / k;
        }

        float a = (52 * L / (u + 13 * L * u_0) - 1) / 3f;
        float b = -5 * Y;
        float c = -1 / 3f;
        float d = Y * (39 * L / (v + 13 * L * v_0) - 5);

        float X = (d - b) / (a - c);
        float Z = X * a + b;

        xyz[0] = X;
        xyz[1] = Y;
        xyz[2] = Z;
    }

    public static void xyz2lab(float[] xyz, float[] lab) {
        final float X_r = 0.95047f;
        final float Y_r = 1.00000f;
        final float Z_r = 1.08883f;

        final float eps = 216f / 24389f;
        final float k = 24389f / 27f;

        float x = xyz[0] / X_r;
        float y = xyz[1] / Y_r;
        float z = xyz[2] / Z_r;

        float f_x = x > eps ? (float)FastMath.cbrt(x) : (k * x + 16) / 116f;
        float f_y = y > eps ? (float)FastMath.cbrt(y) : (k * y + 16) / 116f;
        float f_z = z > eps ? (float)FastMath.cbrt(z) : (k * z + 16) / 116f;

        lab[0] = x > eps && y > eps && z > eps ? 116 * f_x - 16 : k * y;
        lab[1] = 500 * (f_x - f_y);
        lab[2] = 200 * (f_y - f_z);
    }

    public static void lab2xyz(float[] lab, float[] xyz) {
        final float X_r = 0.95047f;
        final float Y_r = 1.00000f;
        final float Z_r = 1.08883f;

        final float eps = 216f / 24389f;
        final float k = 24389f / 27f;

        float L = lab[0];
        float a = lab[1];
        float b = lab[2];

        float f_y = (L + 16) / 116f;
        float f_x = a / 500f + f_y;
        float f_z = f_y - b / 200f;

        float x = f_x * f_x * f_x;
        if (x <= eps) {
            x = (116 * f_x - 16) / k;
        }

        float z = f_z * f_z * f_z;
        if (z <= eps) {
            z = (116 * f_z - 16) / k;
        }

        float y = L > k * eps ? (float)FastMath.pow((L + 16) / 116, 3) : L / k;

        xyz[0] = x * X_r;
        xyz[1] = y * Y_r;
        xyz[2] = z * Z_r;
    }

    public static void luv2lch(float[] luv, float[] lch) {
        lch[0] = luv[0];
        lch[1] = (float)FastMath.sqrt(luv[1] * luv[1] + luv[2] * luv[2]);
        lch[2] = (float)FastMath.toDegrees(FastMath.atan2(luv[2], luv[1]));
        if (lch[2] < 0) {
            lch[2] += 360;
        }
    }

    public static void lch2luv(float[] lch, float[] luv) {
        luv[0] = lch[0];
        float H_rad = (float)FastMath.toRadians(lch[2] < 0 ? lch[2] + 360 : lch[2]);
        luv[1] = lch[1] * (float)FastMath.cos(H_rad);
        luv[2] = lch[1] * (float)FastMath.sin(H_rad);
    }

    public static int adjustHueSat(
        int color, float hueShift, float hueVar, float hueCenter, float satFactor) {
        if (hueShift == 0 && hueVar == 1 && satFactor == 1) return color;

        float h = LXColor.h(color) / 360f;
        float s = LXColor.s(color) / 100f;
        float b = LXColor.b(color) / 100f;
        int alpha = color & 0xff000000;

        h = h - hueCenter + 0.5f;
        float hf = (float) Math.floor(h);
        h = h - hf;
        h = h - 0.5f;
        h *= hueVar;
        h = h + hueCenter;
        h += hueShift;

        s *= satFactor;
        if (s > 1) s = 1;

        return alpha | (LXColor.hsb(h * 360f, s * 100f, b * 100f) & 0x00ffffff);
    }

    public static long adjustHueSat16(
        long c, double hueShift, double hueVar, double hueCenter, double satFactor) {
        if (hueShift == 0 && hueVar == 1 && satFactor == 1) return c;
        int color = Spaces.rgb16ToRgb8(c);

        double h = LXColor.h(color) / 360.0;
        double s = LXColor.s(color) / 100.0;
        double b = LXColor.b(color) / 100.0;
        int alpha = color & 0xff000000;

        h = h - hueCenter + 0.5;
        float hf = (float) Math.floor(h);
        h = h - hf;
        h = h - 0.5;
        h *= hueVar;
        h = h + hueCenter;
        h += hueShift;

        s *= satFactor;
        if (s > 1) s = 1;

        return Spaces.rgb8ToRgb16(alpha | (LXColor.hsb(h * 360, s * 100, b * 100) & 0x00ffffff));
    }
}
