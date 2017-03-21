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
import heronarts.lx.ModelBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BoundedParameter;

public class BlurEffect extends LXEffect {

    public final BoundedParameter amount = new BoundedParameter("Amount", 0);

    private final ModelBuffer blurBuffer;

    public BlurEffect(LX lx) {
        super(lx);
        this.blurBuffer = new ModelBuffer(lx);
        int[] blurArray = blurBuffer.getArray();
        for (int i = 0; i < blurArray.length; ++i) {
            blurArray[i] = LXColor.BLACK;
        }
        addParameter(this.amount);
    }

    @Override
    protected void onEnable() {
        int[] blurArray = this.blurBuffer.getArray();
        for (int i = 0; i < blurArray.length; ++i) {
            blurArray[i] = 0;
        }
    }

    @Override
    public void run(double deltaMs, double amount) {
        float blurf = (float) (amount * this.amount.getValuef());
        if (blurf > 0) {
            blurf = 1 - (1 - blurf) * (1 - blurf) * (1 - blurf);
            int[] blurArray = this.blurBuffer.getArray();
            for (int i = 0; i < this.colors.length; ++i) {
                int blend = LXColor.screen(this.colors[i], blurArray[i]);
                this.colors[i] = LXColor.lerp(this.colors[i], blend, blurf);
            }
            System.arraycopy(this.colors, 0, blurArray, 0, this.colors.length);
        }

    }
}
