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

package heronarts.lx.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BoundedParameter;

public class BlurEffect extends LXEffect {

    public final BoundedParameter amount = new BoundedParameter("BLUR", 0);

    private final int[] blurBuffer = new int[this.lx.total];

    public BlurEffect(LX lx) {
        super(lx);
        for (int i = 0; i < blurBuffer.length; ++i) {
            this.blurBuffer[i] = 0xff000000;
        }
        addParameter(this.amount);
    }

    @Override
    public void run(double deltaMs) {
        float blurf = this.amount.getValuef();
        if (blurf > 0) {
            blurf = 1 - (1 - blurf) * (1 - blurf) * (1 - blurf);
            for (int i = 0; i < this.colors.length; ++i) {
                int blend = LXColor.screen(this.colors[i], this.blurBuffer[i]);
                this.colors[i] = LXColor.lerp(this.colors[i], blend, blurf);
            }
        }
        for (int i = 0; i < this.colors.length; ++i) {
            this.blurBuffer[i] = this.colors[i];
        }
    }
}
