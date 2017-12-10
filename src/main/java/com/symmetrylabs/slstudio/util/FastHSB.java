package com.symmetrylabs.slstudio.util;

import heronarts.lx.LX;

/**
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class FastHSB {
    public static final int SIZE = 64;
    public static final int[][][] cache = new int[SIZE+1][SIZE+1][SIZE+1];

    static {
        for (int h = 0; h <= SIZE; h++) {
            float fh = (float) h / SIZE;
            for (int s = 0; s <= SIZE; s++) {
                float fs = (float) s / SIZE;
                for (int b = 0; b <= SIZE; b++) {
                    float fb = (float) b / SIZE;

                    cache[h][s][b] = LX.hsb(h*360, s*100, b*100);
                }
            }
        }
    }

    public static int hsb(float h, float s, float b) {
        h /= 360;
        s /= 100;
        b /= 100;
        h = h%1;
        if (h < 0) h ++;
        if (s < 0) s = 0;
        if (s > 1) s = 1;
        if (b < 0) b = 0;
        if (b > 1) b = 1;
        return cache[(int) (h*SIZE)][(int) (s*SIZE)][(int) (b*SIZE)];
    }
}
