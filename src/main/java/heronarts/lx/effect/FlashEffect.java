/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
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

package heronarts.lx.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;

public class FlashEffect extends LXEffect {

    private final CompoundParameter sat =
        new CompoundParameter("Saturation", 0)
        .setDescription("Sets the color saturation level of the flash");

    public final CompoundParameter attack =
        new CompoundParameter("Attack", 100, 1000)
        .setDescription("Sets the attack time of the flash");

    public final CompoundParameter decay =
        new CompoundParameter("Decay", 1500, 3000)
        .setDescription("Sets the decay time of the flash");

    public final BoundedParameter intensity =
        new BoundedParameter("Intensity", 1)
        .setDescription("Sets the intensity level of the flash");

    public FlashEffect(LX lx) {
        super(lx);
        addParameter("attack", this.attack);
        addParameter("decay", this.decay);
        addParameter("intensity", this.intensity);
        addParameter("saturation", this.sat);
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
        float flashValue = (float) (amount * this.intensity.getValuef());
        double satValue = this.sat.getValue() * 100.;
        double hueValue = this.lx.palette.getHue();
        if (flashValue > 0) {
            for (int i = 0; i < this.colors.length; ++i) {
                this.colors[i] = LXColor.lerp(this.colors[i], LXColor.hsb(hueValue, satValue, 100.), flashValue);
            }
        }
    }
}
