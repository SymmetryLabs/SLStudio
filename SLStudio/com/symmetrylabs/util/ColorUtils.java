package com.symmetrylabs.util;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public static final class ColorUtils {

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
        float dstA = (dst >> 24 & 0xFF) / 255.0;
        float srcA = (src >> 24 & 0xFF) / 255.0;
        float outA = srcA + dstA * (1 - srcA);
        if (outA == 0) {
            return 0;
        }
        int outR = FastMath.round(((src >> 16 & 0xFF) * srcA + (dst >> 16 & 0xFF) * dstA * (1 - srcA)) / outA) & 0xFF;
        int outG = FastMath.round(((src >> 8 & 0xFF) * srcA + (dst >> 8 & 0xFF) * dstA * (1 - srcA)) / outA) & 0xFF;
        int outB = FastMath.round(((src & 0xFF) * srcA + (dst & 0xFF) * dstA * (1 - srcA)) / outA) & 0xFF;
        return (((int) (outA * 0xFF)) & 0xFF) << 24 | outR << 16 | outG << 8 | outB;
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

}
