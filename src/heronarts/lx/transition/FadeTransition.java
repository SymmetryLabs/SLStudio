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

public class FadeTransition extends LXTransition {

    public FadeTransition(LX lx) {
        super(lx);
    }

    @Override
    protected void computeBlend(int[] c1, int[] c2, double progress) {
        int[] c = (progress < 0.5) ? c1 : c2;
        double b = Math.abs(progress - 0.5) * 2.;

        for (int i = 0; i < this.colors.length; ++i) {
            this.colors[i] = LXColor.hsb(
                    LXColor.h(c[i]),
                    LXColor.s(c[i]),
                    (float) (b * LXColor.b(c[i])));
        }
    }
}
