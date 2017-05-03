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

public class ScreenBlend extends LXBlend {

    public ScreenBlend(LX lx) {
        super(lx);
    }

    @Override
    public void blend(int[] dst, int[] src, double alpha, int[] output) {
        int alphaAdjust = (int) (alpha * 0x100);
        for (int i = 0; i < src.length; ++i) {
            int a = (((src[i] >>> ALPHA_SHIFT) * alphaAdjust) >> 8) & 0xff;

            int srcAlpha = a + (a >= 0x7F ? 1 : 0);
            int dstAlpha = 0x100 - srcAlpha;

            int dstRb = dst[i] & RB_MASK;
            int dstGn = dst[i] & G_MASK;
            int srcGn = src[i] & G_MASK;
            int dstR = (dst[i] & R_MASK) >> R_SHIFT;
            int dstB = dst[i] & B_MASK;

            int rbSub = (
                    (src[i] & R_MASK) * (dstR + 1) |
                    (src[i] & B_MASK) * (dstB + 1)
                ) >>> 8 & RB_MASK;
            int gnSub = srcGn * (dstGn + 0x100) >> G_SHIFT & G_MASK;

            output[i] = min((dst[i] >>> ALPHA_SHIFT) + a, 0xff) << ALPHA_SHIFT |
                (dstRb * dstAlpha + (dstRb + (src[i] & RB_MASK) - rbSub) * srcAlpha) >>> 8 & RB_MASK |
                (dstGn * dstAlpha + (dstGn + srcGn - gnSub) * srcAlpha) >>> 8 & G_MASK;
        }
    }
}
