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

public class DissolveBlend extends LXBlend {

    public DissolveBlend(LX lx) {
        super(lx);
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
}
