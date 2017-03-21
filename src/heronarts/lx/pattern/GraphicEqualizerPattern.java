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

package heronarts.lx.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.LXUtils;
import heronarts.lx.audio.GraphicEQ;
import heronarts.lx.transition.WipeTransition;

public class GraphicEqualizerPattern extends LXPattern {

    private final GraphicEQ eq;

    public GraphicEqualizerPattern(LX lx) {
        super(lx);
        addModulator(this.eq = new GraphicEQ(lx.audio.getInput())).start();
        this.transition = new WipeTransition(lx, WipeTransition.Direction.UP);
    }

    @Override
    public void run(double deltaMs) {
        for (int i = 0; i < this.lx.width; ++i) {
            int avgIndex = (int) (i / (double) this.lx.width * (eq.numBands - 1));
            double value = eq.getBand(avgIndex);
            for (int j = 0; j < this.lx.height; ++j) {
                double jscaled = (this.lx.height - 1 - j)
                        / (double) (this.lx.height - 1);
                double b = LXUtils.constrain(400. * (value - jscaled), 0, 100);
                this.setColor(i, j, palette.getColor(b));
            }
        }
    }

}