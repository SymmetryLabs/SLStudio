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
