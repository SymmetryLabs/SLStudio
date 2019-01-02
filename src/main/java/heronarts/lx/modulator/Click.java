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

package heronarts.lx.modulator;

import heronarts.lx.parameter.FixedParameter;
import heronarts.lx.parameter.LXParameter;

/**
 * A click is a simple modulator that fires a value of 1 every time its period
 * has passed. Otherwise it always returns 0.
 */
public class Click extends LXPeriodicModulator {

    public Click(double periodMs) {
        this(new FixedParameter(periodMs));
    }

    public Click(LXParameter periodMs) {
        this("CLICK", periodMs);
    }

    public Click(String label, double periodMs) {
        this(label, new FixedParameter(periodMs));
    }

    public Click(String label, LXParameter periodMs) {
        super(label, periodMs);
    }

    /**
     * Sets the value of the click to 1, so that code querying it in this frame of
     * execution sees it as active. On the next iteration of the runloop it will
     * be off again.
     *
     * @return this
     */
    public LXModulator fire() {
        setValue(1);
        start();
        return this;
    }

    /**
     * Helper to conditionalize logic based on the click. Typical use is to query
     * as follows:
     *
     * <pre>
     * if (clickInstance.click()) {
     *   // perform periodic operation
     * }
     * </pre>
     *
     * @return true if the value is 1, otherwise false
     */
    public boolean click() {
        return this.getValue() == 1;
    }

    @Override
    protected double computeValue(double deltaMs, double basis) {
        return loop() || finished() ? 1 : 0;
    }

    @Override
    protected double computeBasis(double basis, double value) {
        // The basis is indeterminate for this modulator, it can only be
        // specifically known when the value is 1.
        return value < 1 ? 0 : 1;
    }
}