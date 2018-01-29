package com.symmetrylabs.slstudio.effect;

import org.apache.commons.math3.util.FastMath;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.slstudio.util.ColorUtils;

public class LABShift extends LXEffect {
    public final CompoundParameter Lboost = new CompoundParameter("Lboost", 1, 0, 4);
    public final CompoundParameter Lshift = new CompoundParameter("Lshift", 0, -100, 100);
    public final CompoundParameter Ashift = new CompoundParameter("Ashift", 0, -100, 100);
    public final CompoundParameter Bshift = new CompoundParameter("Bshift", 0, -100, 100);

    public LABShift(LX lx) {
        super(lx);

        addParameter(Lboost);
        addParameter(Lshift);
        addParameter(Ashift);
        addParameter(Bshift);
    }

    private final float[] xyz = new float[3];
    private final float[] lab = new float[3];
    private final float[] rgb = new float[3];

    @Override
    public void run(double deltaMs, double amount) {
        for (LXPoint p : model.points) {
            int c = colors[p.index];
            rgb[0] = ((c & LXColor.RED_MASK) >>> LXColor.RED_SHIFT) / 255f;
            rgb[1] = ((c & LXColor.GREEN_MASK) >>> LXColor.GREEN_SHIFT) / 255f;
            rgb[2] = (c & LXColor.BLUE_MASK) / 255f;

            //rgb2lab(r, g, b, lab);

            //lab[0] += L.getValuef();
            //lab[1] += A.getValuef();
            //lab[2] += B.getValuef();

            //lab[0] = Math.max(0, Math.min(100, lab[0]));
            //lab[1] = Math.max(-110, Math.min(110, lab[1]));
            //lab[2] = Math.max(-110, Math.min(110, lab[2]));

            ColorUtils.rgb2xyz(rgb, xyz);
            ColorUtils.xyz2lab(xyz, lab);

            lab[0] = Math.max(0, Math.min(100, lab[0] * Lboost.getValuef() + Lshift.getValuef()));
            lab[1] = Math.max(-110, Math.min(110, lab[1] + Ashift.getValuef()));
            lab[2] = Math.max(-110, Math.min(110, lab[2] + Bshift.getValuef()));

            ColorUtils.lab2xyz(lab, xyz);
            ColorUtils.xyz2rgb(xyz, rgb);

            colors[p.index] = lab2rgb(lab);
        }
    }

    // adapted from https://github.com/antimatter15/rgb-lab
    private static int lab2rgb(float[] lab) {
        float y = (lab[0] + 16) / 116f;
        float x = lab[1] / 500f + y;
        float z = y - lab[2] / 200f;
        float r, g, b;

        x = 0.95047f * ((x * x * x > 0.008856f) ? x * x * x : (x - 16f / 116f) / 7.787f);
        y = 1.00000f * ((y * y * y > 0.008856f) ? y * y * y : (y - 16f / 116f) / 7.787f);
        z = 1.08883f * ((z * z * z > 0.008856f) ? z * z * z : (z - 16f / 116f) / 7.787f);

        r = x *    3.2406f + y * -1.5372f + z * -0.4986f;
        g = x * -0.9689f + y *    1.8758f + z *    0.0415f;
        b = x *    0.0557f + y * -0.2040f + z *    1.0570f;

        r = (r > 0.0031308f) ? (1.055f * (float)FastMath.pow(r, 1 / 2.4f) - 0.055f) : 12.92f * r;
        g = (g > 0.0031308f) ? (1.055f * (float)FastMath.pow(g, 1 / 2.4f) - 0.055f) : 12.92f * g;
        b = (b > 0.0031308f) ? (1.055f * (float)FastMath.pow(b, 1 / 2.4f) - 0.055f) : 12.92f * b;

        return LXColor.rgb(
                (int)(Math.max(0, Math.min(1, r)) * 255),
                (int)(Math.max(0, Math.min(1, g)) * 255),
                (int)(Math.max(0, Math.min(1, b)) * 255)
        );
    }

    private static void rgb2lab(int R, int G, int B, float[] lab) {
        float r = R / 255f;
        float g = G / 255f;
        float b = B / 255f;
        float x, y, z;

        r = (r > 0.04045f) ? (float)FastMath.pow((r + 0.055f) / 1.055f, 2.4f) : r / 12.92f;
        g = (g > 0.04045f) ? (float)FastMath.pow((g + 0.055f) / 1.055f, 2.4f) : g / 12.92f;
        b = (b > 0.04045f) ? (float)FastMath.pow((b + 0.055f) / 1.055f, 2.4f) : b / 12.92f;

        x = (r * 0.4124f + g * 0.3576f + b * 0.1805f) / 0.95047f;
        y = (r * 0.2126f + g * 0.7152f + b * 0.0722f) / 1.00000f;
        z = (r * 0.0193f + g * 0.1192f + b * 0.9505f) / 1.08883f;

        x = (x > 0.008856f) ? (float)FastMath.pow(x, 1f / 3f) : (7.787f * x) + 16f / 116f;
        y = (y > 0.008856f) ? (float)FastMath.pow(y, 1f / 3f) : (7.787f * y) + 16f / 116f;
        z = (z > 0.008856f) ? (float)FastMath.pow(z, 1f / 3f) : (7.787f * z) + 16f / 116f;

        lab[0] = (116 * y) - 16;
        lab[1] = 500 * (x - y);
        lab[2] = 200 * (y - z);
    }
}
