/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
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

package heronarts.lx.transition;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;

public class BlendTransition extends LXTransition {

    private final LXColor.Blend blendMode;

    public BlendTransition(LX lx, LXColor.Blend blendMode) {
        this(lx, blendMode, Mode.FULL);
    }

    public BlendTransition(LX lx, LXColor.Blend blendMode, Mode mode) {
        super(lx);
        this.blendMode = blendMode;
        setMode(mode);
    }

    @Override
    protected void computeBlend(int[] c1, int[] c2, double progress) {
        if (progress == 0.5) {
            for (int i = 0; i < c1.length; ++i) {
                this.colors[i] = LXColor.blend(c1[i], c2[i], this.blendMode);
            }
        } else if (progress <= 0) {
            for (int i = 0; i < c1.length; ++i) {
                this.colors[i] = c1[i];
            }
        } else if (progress >= 1) {
            for (int i = 0; i < c2.length; ++i) {
                this.colors[i] = c2[i];
            }
        } else if (progress < 0.5) {
            for (int i = 0; i < c1.length; ++i) {
                this.colors[i] = LXColor.lerp(c1[i],
                    LXColor.blend(c1[i], c2[i], this.blendMode),
                    2. * progress
                );
            }
        } else {
            for (int i = 0; i < c1.length; ++i) {
                this.colors[i] = LXColor.lerp(
                    c2[i],
                    LXColor.blend(c1[i], c2[i], this.blendMode),
                    2. * (1. - progress)
                );
            }
        }
    }

}
