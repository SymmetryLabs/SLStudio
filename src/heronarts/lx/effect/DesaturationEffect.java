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
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.BoundedParameter;

public class DesaturationEffect extends LXEffect {

    private final LinearEnvelope desaturation;
    private final BoundedParameter attack;
    private final BoundedParameter decay;
    private final BoundedParameter amount;

    public DesaturationEffect(LX lx) {
        super(lx);
        this.addModulator(this.desaturation = new LinearEnvelope(0, 0, 100));
        this.addParameter(this.amount = new BoundedParameter("Amount", 1.));
        this.addParameter(this.attack = new BoundedParameter("Attack", 100, 0, 1000));
        this.addParameter(this.decay = new BoundedParameter("Decay", 100, 0, 1000));
    }

    @Override
    protected void onEnable() {
        this.desaturation.setRangeFromHereTo(1).setPeriod(this.attack);
        this.desaturation.start();
    }

    @Override
    protected void onDisable() {
        this.desaturation.setRangeFromHereTo(0).setPeriod(this.decay);
        this.desaturation.start();
    }

    @Override
    protected void run(double deltaMs) {
        double d = this.desaturation.getValue() * this.amount.getValue();
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
