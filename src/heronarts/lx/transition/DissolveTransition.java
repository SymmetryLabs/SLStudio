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

public class DissolveTransition extends LXTransition {

    private final int[] sb1, sb2;

    public DissolveTransition(LX lx) {
        super(lx);
        this.sb1 = new int[lx.total];
        this.sb2 = new int[lx.total];
    }

    @Override
    protected void computeBlend(int[] c1, int[] c2, double progress) {
        LXColor.scaleBrightness(c1, (float) (1 - progress), this.sb1);
        LXColor.scaleBrightness(c2, (float) progress, this.sb2);
        for (int i = 0; i < this.colors.length; ++i) {
            this.colors[i] = LXColor.add(this.sb1[i], this.sb2[i]);
        }
    }

}
