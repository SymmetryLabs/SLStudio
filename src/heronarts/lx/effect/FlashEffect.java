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

public class FlashEffect extends LXEffect {

    private final LinearEnvelope flash;
    private final BoundedParameter sat;

    public final BoundedParameter attack;
    public final BoundedParameter decay;
    public final BoundedParameter intensity;

    public FlashEffect(LX lx) {
        super(lx, true);
        this.addModulator(this.flash = new LinearEnvelope(0, 0, 0));
        this.addParameter(this.attack = new BoundedParameter("Attack", 100, 1000));
        this.addParameter(this.decay = new BoundedParameter("Decay", 1500, 3000));
        this.addParameter(this.intensity = new BoundedParameter("Intensity", 1));
        this.addParameter(this.sat = new BoundedParameter("Saturation", 0));
    }

    private double getAttackTime() {
        return this.attack.getValue();
    }

    private double getDecayTime() {
        return this.decay.getValue();
    }

    @Override
    protected void onEnable() {
        this.flash.setRangeFromHereTo(this.intensity.getValue(), getAttackTime()).trigger();
    }

    @Override
    protected void onDisable() {
        this.flash.setRangeFromHereTo(0, getDecayTime()).trigger();
    }

    @Override
    protected void run(double deltaMs) {
        float flashValue = this.flash.getValuef();
        double satValue = this.sat.getValue() * 100.;
        double hueValue = this.lx.palette.getHue();
        if (flashValue > 0) {
            for (int i = 0; i < this.colors.length; ++i) {
                this.colors[i] = LXColor.lerp(this.colors[i], LXColor.hsb(hueValue, satValue, 100.), flashValue);
            }
        }
    }
}
