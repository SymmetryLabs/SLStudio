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
 * Simple square wave LFO. Not damped. Oscillates between a low and high value.
 */
public class SquareLFO extends LXRangeModulator {

    public SquareLFO(double startValue, double endValue, double periodMs) {
        this(new FixedParameter(startValue), new FixedParameter(endValue),
                new FixedParameter(periodMs));
    }

    public SquareLFO(LXParameter startValue, double endValue, double periodMs) {
        this(startValue, new FixedParameter(endValue), new FixedParameter(periodMs));
    }

    public SquareLFO(double startValue, LXParameter endValue, double periodMs) {
        this(new FixedParameter(startValue), endValue, new FixedParameter(periodMs));
    }

    public SquareLFO(double startValue, double endValue, LXParameter periodMs) {
        this(new FixedParameter(startValue), new FixedParameter(endValue), periodMs);
    }

    public SquareLFO(LXParameter startValue, LXParameter endValue, double periodMs) {
        this(startValue, endValue, new FixedParameter(periodMs));
    }

    public SquareLFO(LXParameter startValue, double endValue, LXParameter periodMs) {
        this(startValue, new FixedParameter(endValue), periodMs);
    }

    public SquareLFO(double startValue, LXParameter endValue, LXParameter periodMs) {
        this(new FixedParameter(startValue), endValue, periodMs);
    }

    public SquareLFO(LXParameter startValue, LXParameter endValue,
            LXParameter periodMs) {
        this("SQUARE", startValue, endValue, periodMs);
    }

    public SquareLFO(String label, double startValue, double endValue,
            double periodMs) {
        this(label, new FixedParameter(startValue), new FixedParameter(endValue),
                new FixedParameter(periodMs));
    }

    public SquareLFO(String label, LXParameter startValue, double endValue,
            double periodMs) {
        this(label, startValue, new FixedParameter(endValue), new FixedParameter(
                periodMs));
    }

    public SquareLFO(String label, double startValue, LXParameter endValue,
            double periodMs) {
        this(label, new FixedParameter(startValue), endValue, new FixedParameter(
                periodMs));
    }

    public SquareLFO(String label, double startValue, double endValue,
            LXParameter periodMs) {
        this(label, new FixedParameter(startValue), new FixedParameter(endValue),
                periodMs);
    }

    public SquareLFO(String label, LXParameter startValue, LXParameter endValue,
            double periodMs) {
        this(label, startValue, endValue, new FixedParameter(periodMs));
    }

    public SquareLFO(String label, LXParameter startValue, double endValue,
            LXParameter periodMs) {
        this(label, startValue, new FixedParameter(endValue), periodMs);
    }

    public SquareLFO(String label, double startValue, LXParameter endValue,
            LXParameter periodMs) {
        this(label, new FixedParameter(startValue), endValue, periodMs);
    }

    public SquareLFO(String label, LXParameter startValue, LXParameter endValue,
            LXParameter periodMs) {
        super(label, startValue, endValue, periodMs);
    }

    @Override
    protected double computeNormalizedValue(double deltaMs, double basis) {
        return (basis < 0.5) ? 0 : 1;
    }

    @Override
    protected double computeNormalizedBasis(double basis, double normalizedValue) {
        return (normalizedValue == 0) ? 0 : 0.5;
    }

}
