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
