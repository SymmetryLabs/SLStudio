/**
 * Copyright 2016- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.blend;

import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;

import static heronarts.lx.PolyBuffer.Space.RGB16;
import static heronarts.lx.PolyBuffer.Space.RGB8;

public class MultiplyBlend extends LXBlend {

    public MultiplyBlend(LX lx) {
        super(lx);
    }

//  @Override
//  public void blend(int[] dst, int[] src, double alpha, int[] output) {
//    multiply(dst, src, alpha, output);
//  }

    //override?
    @Override
    public void blend(PolyBuffer base, PolyBuffer overlay,
                                        double alpha, PolyBuffer dest, PolyBuffer.Space space) {
        if (space == RGB8) {
            multiply((int[]) base.getArray(RGB8), (int[]) overlay.getArray(RGB8),
                    alpha, (int[]) dest.getArray(RGB8));
            dest.markModified(RGB8);
        } else {
            multiply16((long[]) base.getArray(RGB16), (long[]) overlay.getArray(RGB16),
                    alpha, (long[]) dest.getArray(RGB16));
            dest.markModified(RGB16);
        }
    }

    public static void multiply(int[] dst, int src, double alpha, int[] output) {
        int alphaAdjust = (int) (alpha * 0x100);
        int a = (((src >>> ALPHA_SHIFT) * alphaAdjust) >> 8) & 0xff;
        int srcAlpha = a + (a >= 0x7f ? 1 : 0);
        int dstAlpha = 0x100 - srcAlpha;
        for (int i = 0; i < dst.length; ++i) {
            int dstG = (dst[i] & G_MASK);
            int dstR = (dst[i] & R_MASK) >> R_SHIFT;
            int dstB = (dst[i] & B_MASK);

            int rb = ((src & R_MASK) * (dstR + 1) | (src & B_MASK) * (dstB + 1)) >>> 8 & RB_MASK;
            int g = (src & G_MASK) * (dstG + 0x100) >>> 16 & G_MASK;

            output[i] = min((dst[i] >>> ALPHA_SHIFT) + a, 0xff) << ALPHA_SHIFT |
                            ((dst[i] & RB_MASK) * dstAlpha + rb * srcAlpha) >>> 8 & RB_MASK |
                            (dstG * dstAlpha + g * srcAlpha) >>> 8 & G_MASK;
        }
    }

    public static void multiply(int[] dst, int[] src, double alpha, int[] output) {
        int alphaAdjust = (int) (alpha * 0x100);
        for (int i = 0; i < src.length; ++i) {
            int a = (((src[i] >>> ALPHA_SHIFT) * alphaAdjust) >> 8) & 0xff;

            int srcAlpha = a + (a >= 0x7f ? 1 : 0);
            int dstAlpha = 0x100 - srcAlpha;

            int dstG = (dst[i] & G_MASK);
            int dstR = (dst[i] & R_MASK) >> R_SHIFT;
            int dstB = (dst[i] & B_MASK);

            int rb = ((src[i] & R_MASK) * (dstR + 1) | (src[i] & B_MASK) * (dstB + 1)) >>> 8 & RB_MASK;
            int g = (src[i] & G_MASK) * (dstG + 0x100) >>> 16 & G_MASK;

            output[i] = min((dst[i] >>> ALPHA_SHIFT) + a, 0xff) << ALPHA_SHIFT |
                            ((dst[i] & RB_MASK) * dstAlpha + rb * srcAlpha) >>> 8 & RB_MASK |
                            (dstG * dstAlpha + g * srcAlpha) >>> 8 & G_MASK;
        }
    }

    public static void multiply16(long[] dst, long src, double alpha, long[] output) {
        int alphaAdjust = (int) (alpha * 0x10000);
        long a = (((src >>> ALPHA_SHIFT16) * alphaAdjust) >> 16) & 0xffff;
        long srcAlpha = a + (a >= 0x7fff ? 1 : 0);
        long dstAlpha = 0x10000 - srcAlpha;
        for (int i = 0; i < dst.length; ++i) {
            long dstG = (dst[i] & G_MASK16);
            long dstR = (dst[i] & R_MASK16) >> 32;
            long dstB = (dst[i] & B_MASK16);

            long rb = ((src & R_MASK16) * (dstR + 1) | (src & B_MASK16) * (dstB + 1)) >>> 16 & RB_MASK16;
            long g = (src & G_MASK16) * (dstG + 0x10000) >>> 32 & G_MASK16;

            output[i] = min((dst[i] >>> ALPHA_SHIFT16) + a, 0xffff) << ALPHA_SHIFT16 |
                            ((dst[i] & RB_MASK16) * dstAlpha + rb * srcAlpha) >>> 16 & RB_MASK16 |
                            (dstG * dstAlpha + g * srcAlpha) >>> 16 & G_MASK16;
        }
    }

    public static void multiply16(long[] dst, long[] src, double alpha, long[] output) {
        int alphaAdjust = (int) (alpha * 0x10000);
        for (int i = 0; i < src.length; ++i) {
            long a = (((src[i] >>> ALPHA_SHIFT16) * alphaAdjust) >> 16) & 0xffff;

            long srcAlpha = a + (a >= 0x7fff ? 1 : 0);
            long dstAlpha = 0x10000 - srcAlpha;

            long dstG = (dst[i] & G_MASK16);
            long dstR = (dst[i] & R_MASK16) >> 32;
            long dstB = (dst[i] & B_MASK16);

            long rb = ((src[i] & R_MASK16) * (dstR + 1) | (src[i] & B_MASK16) * (dstB + 1)) >>> 16 & RB_MASK16;
            long g = (src[i] & G_MASK16) * (dstG + 0x10000) >>> 32 & G_MASK16;

            output[i] = min((dst[i] >>> ALPHA_SHIFT16) + a, 0xffff) << ALPHA_SHIFT16 |
                            ((dst[i] & RB_MASK16) * dstAlpha + rb * srcAlpha) >>> 16 & RB_MASK16 |
                            (dstG * dstAlpha + g * srcAlpha) >>> 16 & G_MASK16;
        }
    }
}
