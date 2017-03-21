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

public class FadeTransition extends LXTransition {

    public FadeTransition(LX lx) {
        super(lx);
    }

    @Override
    protected void computeBlend(int[] c1, int[] c2, double progress) {
        int[] c = (progress < 0.5) ? c1 : c2;
        double b = Math.abs(progress - 0.5) * 2.;

        for (int i = 0; i < this.colors.length; ++i) {
            this.colors[i] = LXColor.hsb(
                    LXColor.h(c[i]),
                    LXColor.s(c[i]),
                    (float) (b * LXColor.b(c[i])));
        }
    }
}
