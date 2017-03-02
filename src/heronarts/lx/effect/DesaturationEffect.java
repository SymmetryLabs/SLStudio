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
import heronarts.lx.parameter.LXParameter;

public class DesaturationEffect extends LXEffect {

    private final BoundedParameter attack;
    private final BoundedParameter decay;
    private final BoundedParameter amount;

    public DesaturationEffect(LX lx) {
        super(lx);
        this.addParameter(this.amount = new BoundedParameter("Amount", 1.));
        this.addParameter(this.attack = new BoundedParameter("Attack", 100, 0, 1000));
        this.addParameter(this.decay = new BoundedParameter("Decay", 100, 0, 1000));
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == this.attack) {
            this.enabledDampingAttack.setValue(p.getValue());
        } else if (p == this.decay) {
            this.enabledDampingRelease.setValue(p.getValue());
        }
    }

    @Override
    protected void run(double deltaMs, double amount) {
        double d = amount * this.amount.getValue();
        if (d > 0) {
            d = 1-d;
            for (int i = 0; i < colors.length; ++i) {
                this.colors[i] = LXColor.hsb(
                    LXColor.h(this.colors[i]),
                    Math.max(0, LXColor.s(colors[i]) * d),
                    LXColor.b(colors[i])
                );
            }
        }
    }

}
