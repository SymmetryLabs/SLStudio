package heronarts.lx.midi.surface;

import heronarts.lx.color.LXColor;
import org.apache.commons.math3.util.FastMath;


public class APC40Colors {
    /** The color at index i in this array is the color displayed when sending velocity i to the APC40 */
    public static final int[] COLOR_MAP = new int[] {
        0x000000, 0x1E1E1E, 0x7F7F7F, 0xFFFFFF, 0xFF4C4C, 0xFF0000, 0x590000, 0x190000,
        0xFFBD6C, 0xFF5400, 0x591D00, 0x271B00, 0xFFFF4C, 0xFFFF00, 0x595900, 0x191900,
        0x88FF4C, 0x54FF00, 0x1D5900, 0x142B00, 0x4CFF4C, 0x00FF00, 0x005900, 0x001900,
        0x4CFF5E, 0x00FF19, 0x00590D, 0x001902, 0x4CFF88, 0x00FF55, 0x00591D, 0x001F12,
        0x4CFFB7, 0x00FF99, 0x005935, 0x001912, 0x4CC3FF, 0x00A9FF, 0x004152, 0x001019,
        0x4C88FF, 0x0055FF, 0x001D59, 0x000819, 0x4C4CFF, 0x0000FF, 0x000059, 0x000019,
        0x874CFF, 0x5400FF, 0x190064, 0x0F0030, 0xFF4CFF, 0xFF00FF, 0x590059, 0x190019,
        0xFF4C87, 0xFF0054, 0x59001D, 0x220013, 0xFF1500, 0x993500, 0x795100, 0x436400,
        0x033900, 0x005735, 0x00547F, 0x0000FF, 0x00454F, 0x2500CC, 0x7F7F7F, 0x202020,
        0xFF0000, 0xBDFF2D, 0xAFED06, 0x64FF09, 0x103B00, 0x00FF87, 0x00A9FF, 0x002AFF,
        0x3F00FF, 0x7A00FF, 0xB21A7D, 0x402100, 0xFF4A00, 0x88E106, 0x72FF15, 0x00FF00,
        0x3BFF26, 0x59FF71, 0x38FFCC, 0x5B8AFF, 0x3151C6, 0x877FE9, 0xD31DFF, 0xFF005D,
        0xFF7F00, 0xB9B000, 0x90FF00, 0x835D07, 0x392B00, 0x144C10, 0x0D5038, 0x15152A,
        0x16205A, 0x693C1C, 0xA8000A, 0xDE513D, 0xD86A1C, 0xFFE126, 0x9EE12F, 0x67B50F,
        0x1E1E30, 0xDCFF6B, 0x80FFBD, 0x9A99FF, 0x8E66FF, 0x404040, 0x757575, 0xE0FFFF,
        0xA00000, 0x350000, 0x1AD000, 0x074200, 0xB9B000, 0x3F3100, 0xB35F00, 0x4B1502,
    };

    public static final double[] H = new double[COLOR_MAP.length];
    public static final double[] S = new double[COLOR_MAP.length];
    public static final double[] B = new double[COLOR_MAP.length];

    /* weight hue differences higher than saturation and value differences */
    private static final double HUE_DIST_WEIGHT = 4.0;

    static {
        for (int i = 0; i < COLOR_MAP.length; i++) {
            int c = COLOR_MAP[i];
            H[i] = LXColor.h(c) / 360.f;
            S[i] = LXColor.s(c) / 100.f;
            B[i] = LXColor.b(c) / 100.f;
        }
    }

    /** Finds the velocity corresponding to the closest color match */
    public static int matchColor(int c) {
        double h = LXColor.h(c) / 360.f;
        double s = LXColor.s(c) / 100.f;
        double b = LXColor.b(c) / 100.f;

        double minSqDist = 1e5;
        int minIdx = -1;

        for (int i = 0; i < COLOR_MAP.length; i++) {
            /* hue is periodic */
            double hdist = Double.min(
                FastMath.abs(H[i] - h),
                (1 - Double.max(H[i], h) + Double.min(H[i], h)));
            double sqdist = HUE_DIST_WEIGHT * FastMath.pow(hdist, 2) + FastMath.pow(S[i] - s, 2) + FastMath.pow(B[i] - b, 2);
            if (sqdist < minSqDist) {
                minSqDist = sqdist;
                minIdx = i;
            }
        }

        return minIdx;
    }
}
