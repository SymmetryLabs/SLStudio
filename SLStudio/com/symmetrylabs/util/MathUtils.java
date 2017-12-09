package com.symmetrylabs.util;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public final class MathUtils {

    public static byte byteSubtract(int a, int b) {
        byte res = (byte) (a - b);
        return (byte) (res & (byte) ((b & 0xFF) <= (a & 0xFF) ? -1 : 0));
    }

    public static byte byteMultiply(byte b, double s) {
        int res = (int) ((b & 0xFF) * s);
        byte hi = (byte) (res >> 8);
        byte lo = (byte) (res);
        return (byte) (lo | (byte) (hi == 0 ? 0 : -1));
    }

    public static void interpolateArray(float[] in, float[] out) {
        if (out.length == in.length) {
            System.arraycopy(in, 0, out, 0, out.length);
            return;
        }

        float outPerIn = 1.0f * (out.length - 1) / (in.length - 1);
        for (int outIndex = 0; outIndex < out.length; outIndex++) {
            int inIndex = (int) (outIndex / outPerIn);
            // com.symmetrylabs.pattern.Test if we're the nearest index to the exact index in the `in` array
            // to keep those crisp and un-aliased
            if ((int) (outIndex % outPerIn) == 0) { //  || inIndex+1 >= in.length
                out[outIndex] = in[inIndex];
            } else {
                // Use spline fitting. (Double up the value if we're at the edge of the `out` array)
                if (inIndex >= 1 && inIndex < in.length - 2) {
                    out[outIndex] = Utils.curvePoint(in[inIndex - 1], in[inIndex], in[inIndex + 1],
                        in[inIndex + 2], (outIndex / outPerIn) % 1
                    );
                } else if (inIndex == 0) {
                    out[outIndex] = Utils.curvePoint(in[inIndex], in[inIndex], in[inIndex + 1],
                        in[inIndex + 2], (outIndex / outPerIn) % 1
                    );
                } else {
                    out[outIndex] = Utils.curvePoint(in[inIndex - 1], in[inIndex], in[inIndex + 1],
                        in[inIndex + 1], (outIndex / outPerIn) % 1
                    );
                }
            }
        }
    }

}
