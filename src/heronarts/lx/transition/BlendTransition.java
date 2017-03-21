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

public class BlendTransition extends LXTransition {

    private final LXColor.Blend blendMode;

    public BlendTransition(LX lx, LXColor.Blend blendMode) {
        this(lx, blendMode, Mode.FULL);
    }

    public BlendTransition(LX lx, LXColor.Blend blendMode, Mode mode) {
        super(lx);
        this.blendMode = blendMode;
        setMode(mode);
    }

    @Override
    protected void computeBlend(int[] c1, int[] c2, double progress) {
        if (progress == 0.5) {
            for (int i = 0; i < c1.length; ++i) {
                this.colors[i] = LXColor.blend(c1[i], c2[i], this.blendMode);
            }
        } else if (progress <= 0) {
            for (int i = 0; i < c1.length; ++i) {
                this.colors[i] = c1[i];
            }
        } else if (progress >= 1) {
            for (int i = 0; i < c2.length; ++i) {
                this.colors[i] = c2[i];
            }
        } else if (progress < 0.5) {
            for (int i = 0; i < c1.length; ++i) {
                this.colors[i] = LXColor.lerp(c1[i],
                    LXColor.blend(c1[i], c2[i], this.blendMode),
                    2. * progress
                );
            }
        } else {
            for (int i = 0; i < c1.length; ++i) {
                this.colors[i] = LXColor.lerp(
                    c2[i],
                    LXColor.blend(c1[i], c2[i], this.blendMode),
                    2. * (1. - progress)
                );
            }
        }
    }

}
