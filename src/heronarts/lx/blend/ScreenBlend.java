/**
 * Copyright 2016- Mark C. Slee, Heron Arts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
