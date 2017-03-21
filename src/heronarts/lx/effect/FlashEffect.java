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
import heronarts.lx.parameter.LXParameter;

public class FlashEffect extends LXEffect {

    private final BoundedParameter sat;
    public final BoundedParameter attack;
    public final BoundedParameter decay;
    public final BoundedParameter intensity;

    public FlashEffect(LX lx) {
        super(lx);
        this.addParameter(this.attack = new BoundedParameter("Attack", 100, 1000));
        this.addParameter(this.decay = new BoundedParameter("Decay", 1500, 3000));
        this.addParameter(this.intensity = new BoundedParameter("Intensity", 1));
        this.addParameter(this.sat = new BoundedParameter("Saturation", 0));
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
