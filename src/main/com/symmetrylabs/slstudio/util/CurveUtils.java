package com.symmetrylabs.slstudio.util;

import processing.core.PMatrix3D;

public class CurveUtils {

    //////////////////////////////////////////////////////////////
    // SPLINE UTILITY FUNCTIONS (used by both Bezier and Catmull-Rom)
    protected static void splineForward(int segments, PMatrix3D matrix) {
        float f  = 1.0f / segments;
        float ff = f * f;
        float fff = ff * f;

        matrix.set(0,     0,    0, 1,
                             fff,   ff,   f, 0,
                             6*fff, 2*ff, 0, 0,
                             6*fff, 0,    0, 0);
    }



    //////////////////////////////////////////////////////////////
    // BEZIER

    protected static boolean bezierInited = false;
    public static int bezierDetail = 20;

    // used by both curve and bezier, so just init here
    protected static PMatrix3D bezierBasisMatrix =
        new PMatrix3D(-1,  3, -3,  1,
                                     3, -6,  3,  0,
                                    -3,  3,  0,  0,
                                     1,  0,  0,  0);

    //protected PMatrix3D bezierForwardMatrix;
    protected static PMatrix3D bezierDrawMatrix;

    public static float bezierPoint(float a, float b, float c, float d, float t) {
        float t1 = 1.0f - t;
        return a*t1*t1*t1 + 3*b*t*t1*t1 + 3*c*t*t*t1 + d*t*t*t;
    }

    public static float bezierTangent(float a, float b, float c, float d, float t) {
        return (3*t*t * (-a+3*b-3*c+d) +
                        6*t * (a-2*b+c) +
                        3 * (-a+b));
    }

    protected static void bezierInitCheck() {
        if (!bezierInited) {
            bezierInit();
        }
    }

    protected static void bezierInit() {
        // overkill to be broken out, but better parity with the curve stuff below
        bezierDetail(bezierDetail);
        bezierInited = true;
    }

    public static void bezierDetail(int detail) {
        bezierDetail = detail;

        if (bezierDrawMatrix == null) {
            bezierDrawMatrix = new PMatrix3D();
        }

        // setup matrix for forward differencing to speed up drawing
        splineForward(detail, bezierDrawMatrix);

        // multiply the basis and forward diff matrices together
        // saves much time since this needn't be done for each curve
        //mult_spline_matrix(bezierForwardMatrix, bezier_basis, bezierDrawMatrix, 4);
        //bezierDrawMatrix.set(bezierForwardMatrix);
        bezierDrawMatrix.apply(bezierBasisMatrix);
    }

    //////////////////////////////////////////////////////////////
    // CATMULL-ROM CURVE

    protected static boolean curveInited = false;
    public static int curveDetail = 20;
    public static float curveTightness = 0;
    // catmull-rom basis matrix, perhaps with optional s parameter
    protected static PMatrix3D curveBasisMatrix;
    protected static PMatrix3D curveDrawMatrix;

    protected static PMatrix3D bezierBasisInverse;
    protected static PMatrix3D curveToBezierMatrix;

    public static float curvePoint(float a, float b, float c, float d, float t) {
        curveInitCheck();

        float tt = t * t;
        float ttt = t * tt;
        PMatrix3D cb = curveBasisMatrix;

        // not optimized (and probably need not be)
        return (a * (ttt*cb.m00 + tt*cb.m10 + t*cb.m20 + cb.m30) +
                        b * (ttt*cb.m01 + tt*cb.m11 + t*cb.m21 + cb.m31) +
                        c * (ttt*cb.m02 + tt*cb.m12 + t*cb.m22 + cb.m32) +
                        d * (ttt*cb.m03 + tt*cb.m13 + t*cb.m23 + cb.m33));
    }

    public static float curveTangent(float a, float b, float c, float d, float t) {
        curveInitCheck();

        float tt3 = t * t * 3;
        float t2 = t * 2;
        PMatrix3D cb = curveBasisMatrix;

        // not optimized (and probably need not be)
        return (a * (tt3*cb.m00 + t2*cb.m10 + cb.m20) +
                        b * (tt3*cb.m01 + t2*cb.m11 + cb.m21) +
                        c * (tt3*cb.m02 + t2*cb.m12 + cb.m22) +
                        d * (tt3*cb.m03 + t2*cb.m13 + cb.m23) );
    }

    public static void curveDetail(int detail) {
        curveDetail = detail;
        curveInit();
    }

    public static void curveTightness(float tightness) {
        curveTightness = tightness;
        curveInit();
    }

    protected static void curveInitCheck() {
        if (!curveInited) {
            curveInit();
        }
    }

    protected static void curveInit() {
        // allocate only if/when used to save startup time
        if (curveDrawMatrix == null) {
            curveBasisMatrix = new PMatrix3D();
            curveDrawMatrix = new PMatrix3D();
            curveInited = true;
        }

        float s = curveTightness;
        curveBasisMatrix.set((s-1)/2f, (s+3)/2f,  (-3-s)/2f, (1-s)/2f,
                                                 (1-s),    (-5-s)/2f, (s+2),     (s-1)/2f,
                                                 (s-1)/2f, 0,         (1-s)/2f,  0,
                                                 0,        1,         0,         0);

        //setup_spline_forward(segments, curveForwardMatrix);
        splineForward(curveDetail, curveDrawMatrix);

        if (bezierBasisInverse == null) {
            bezierBasisInverse = bezierBasisMatrix.get();
            bezierBasisInverse.invert();
            curveToBezierMatrix = new PMatrix3D();
        }

        // TODO only needed for PGraphicsJava2D? if so, move it there
        // actually, it's generally useful for other renderers, so keep it
        // or hide the implementation elsewhere.
        curveToBezierMatrix.set(curveBasisMatrix);
        curveToBezierMatrix.preApply(bezierBasisInverse);

        // multiply the basis and forward diff matrices together
        // saves much time since this needn't be done for each curve
        curveDrawMatrix.apply(curveBasisMatrix);
    }

    public static void interpolateArray(float[] in, float[] out) {
        if (out.length == in.length) {
            System.arraycopy(in, 0, out, 0, out.length);
            return;
        }

        float outPerIn = 1.0f * (out.length-1) / (in.length-1);
        for (int outIndex = 0; outIndex < out.length; outIndex++) {
            int inIndex = (int)(outIndex / outPerIn);
            // Test if we're the nearest index to the exact index in the `in` array
            // to keep those crisp and un-aliased
            if ((int)(outIndex % outPerIn) == 0) { //  || inIndex+1 >= in.length
                out[outIndex] = in[inIndex];
            } else {
                // Use spline fitting. (Double up the value if we're at the edge of the `out` array)
                if (inIndex >= 1 && inIndex < in.length-2) {
                    out[outIndex] = curvePoint(in[inIndex-1], in[inIndex], in[inIndex+1],
                        in[inIndex+2], (outIndex/outPerIn) % 1);
                } else if (inIndex == 0) {
                    out[outIndex] = curvePoint(in[inIndex], in[inIndex], in[inIndex+1],
                        in[inIndex+2], (outIndex/outPerIn) % 1);
                } else {
                    out[outIndex] = curvePoint(in[inIndex-1], in[inIndex], in[inIndex+1],
                        in[inIndex+1], (outIndex/outPerIn) % 1);
                }
            }
        }
    }
}
