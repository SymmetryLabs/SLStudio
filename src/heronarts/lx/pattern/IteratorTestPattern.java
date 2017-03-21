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
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.FunctionalParameter;

/**
 * Braindead simple test pattern that iterates through all the nodes turning
 * them on one by one in fixed order.
 */
public class IteratorTestPattern extends LXPattern {

    private final SawLFO index;
    public final BoundedParameter speed = new BoundedParameter("Speed", 10, 1, 100);

    private final FunctionalParameter period = new FunctionalParameter() {
        @Override
        public double getValue() {
            return (1000 / speed.getValue()) * lx.total;
        }
    };

    public IteratorTestPattern(LX lx) {
        super(lx);
        addParameter(speed);
        setAutoCycleEligible(false);
        startModulator(this.index = new SawLFO(0, lx.total, period));
    }

    @Override
    public void run(double deltaMs) {
        int active = (int) Math.floor(this.index.getValue());
        for (int i = 0; i < colors.length; ++i) {
            this.colors[i] = (i == active) ? 0xFFFFFFFF : 0xFF000000;
        }
    }
}
