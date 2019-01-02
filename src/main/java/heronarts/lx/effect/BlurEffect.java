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
import heronarts.lx.blend.NormalBlend;
import heronarts.lx.blend.ScreenBlend;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

public class BlurEffect extends LXEffect {

    public final CompoundParameter amount =
        new CompoundParameter("Amount", 0)
        .setDescription("Sets the amount of blur to apply");

    private final ModelBuffer blurBuffer;

    public BlurEffect(LX lx) {
        super(lx);
        this.blurBuffer = new ModelBuffer(lx);
        int[] blurArray = blurBuffer.getArray();
        for (int i = 0; i < blurArray.length; ++i) {
            blurArray[i] = LXColor.BLACK;
        }
        addParameter("amount", this.amount);
    }

    @Override
    protected void onEnable() {
        int[] blurArray = this.blurBuffer.getArray();
        for (int i = 0; i < blurArray.length; ++i) {
            blurArray[i] = LXColor.BLACK;
        }
    }

    @Override
    public void run(double deltaMs, double amount) {
        float blurf = (float) (amount * this.amount.getValuef());
        if (blurf > 0) {
            blurf = 1 - (1 - blurf) * (1 - blurf) * (1 - blurf);
            int[] blurArray = this.blurBuffer.getArray();

            // Screen blend the colors onto the blur array
            ScreenBlend.screen(blurArray, this.colors, 1, blurArray);

            // Lerp onto the colors based upon amount
            NormalBlend.lerp(this.colors, blurArray, blurf, this.colors);

            // Copy colors into blur array for next frame
            System.arraycopy(this.colors, 0, blurArray, 0, this.colors.length);
        }

    }
}
