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

public class DissolveBlend extends LXBlend {

    public DissolveBlend(LX lx) {
        super(lx);
    }

    public void blend(PolyBuffer base, PolyBuffer overlay,
                                        double alpha, PolyBuffer dest, PolyBuffer.Space space) {
        switch (space) {
            case RGB8:
                blend((int[]) base.getArray(space), (int[]) overlay.getArray(space),
                                alpha, (int[]) dest.getArray(space));
                dest.markModified(space);
                break;
            case RGB16:
                blend16((long[]) base.getArray(space), (long[]) overlay.getArray(space),
                                alpha, (long[]) dest.getArray(space));
                dest.markModified(space);
                break;
        }
    }

    @Override
    public void blend(int[] dst, int[] src, double alpha, int[] output) {
        int srcAlpha = (int) (alpha * 0x80);
        for (int i = 0; i < src.length; ++i) {
            int dstAlpha = 0x100 - srcAlpha;

            output[i] = 0xff << ALPHA_SHIFT |
                    ((dst[i] & RB_MASK) * dstAlpha + (src[i] & RB_MASK) * srcAlpha) >>> 8 & RB_MASK |
                    ((dst[i] & G_MASK) * dstAlpha + (src[i] & G_MASK) * srcAlpha) >>> 8 & G_MASK;
        }
    }

    @Override
    public void blend16(long[] dst, long[] src, double alpha, long[] output) {
        int srcAlpha = (int) (alpha * 0x8000);
        for (int i = 0; i < src.length; ++i) {
            long dstAlpha = 0x10000 - srcAlpha;

            output[i] = 0xffff << ALPHA_SHIFT16 |
                            ((dst[i] & RB_MASK16) * dstAlpha + (src[i] & RB_MASK16) * srcAlpha) >>> 16 & RB_MASK16 |
                            ((dst[i] & G_MASK16) * dstAlpha + (src[i] & G_MASK16) * srcAlpha) >>> 16 & G_MASK16;
        }
    }
}
