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

public class SubtractBlend extends LXBlend {

    public SubtractBlend(LX lx) {
        super(lx);
    }

    public void blend(PolyBuffer base, PolyBuffer overlay,
                                        double alpha, PolyBuffer dest, PolyBuffer.Space space) {
        if (space == RGB8) {
            blend((int[]) base.getArray(RGB8), (int[]) overlay.getArray(RGB8),
                    alpha, (int[]) dest.getArray(RGB8));
            dest.markModified(RGB8);
        } else {
            blend16((long[]) base.getArray(RGB16), (long[]) overlay.getArray(RGB16),
                    alpha, (long[]) dest.getArray(RGB16));
            dest.markModified(RGB16);
        }
    }

    @Override
        public void blend(int[] dst, int[] src, double alpha, int[] output) {
        int alphaAdjust = (int) (alpha * 0x100);
        for (int i = 0; i < src.length; ++i) {
            int a = (((src[i] >>> ALPHA_SHIFT) * alphaAdjust) >> 8) & 0xff;

            int srcAlpha = a + (a >= 0x7f ? 1 : 0);

            int rb = (src[i] & RB_MASK) * srcAlpha >>> 8;
            int gn = (src[i] & G_MASK) * srcAlpha >>> 8;

            output[i] = min((dst[i] >>> ALPHA_SHIFT) + a, 0xff) << ALPHA_SHIFT |
                max((dst[i] & R_MASK) - (rb & R_MASK), 0) |
                max((dst[i] & G_MASK) - (gn & G_MASK), 0) |
                max((dst[i] & B_MASK) - (rb & B_MASK), 0);

        }
    }

    public void blend16(long[] dst, long[] src, double alpha, long[] output) {
        int alphaAdjust = (int) (alpha * 0x10000); // should be long?
        for (int i = 0; i < src.length; ++i) {
            long a = (((src[i] >>> ALPHA_SHIFT16) * alphaAdjust) >> 16) & 0xffff;

            long srcAlpha = a + (a >= 0x7fff ? 1 : 0);

            long rb = (src[i] & RB_MASK16) * srcAlpha >>> 16;
            long gn = (src[i] & G_MASK16) * srcAlpha >>> 16;

            output[i] = min((dst[i] >>> ALPHA_SHIFT16) + a, 0xffff) << ALPHA_SHIFT16 |
                            max((dst[i] & R_MASK16) - (rb & R_MASK16), 0) |
                            max((dst[i] & G_MASK16) - (gn & G_MASK16), 0) |
                            max((dst[i] & B_MASK16) - (rb & B_MASK16), 0);

        }
    }
}
