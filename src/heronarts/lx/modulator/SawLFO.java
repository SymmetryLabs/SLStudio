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
 * A sawtooth LFO oscillates from one extreme value to another. When the later
 * value is hit, the oscillator rests to its initial value.
 */
public class SawLFO extends LXRangeModulator {

    public SawLFO(double startValue, double endValue, double periodMs) {
        this(new FixedParameter(startValue), new FixedParameter(endValue),
                new FixedParameter(periodMs));
    }

    public SawLFO(LXParameter startValue, double endValue, double periodMs) {
        this(startValue, new FixedParameter(endValue), new FixedParameter(periodMs));
    }

    public SawLFO(double startValue, LXParameter endValue, double periodMs) {
        this(new FixedParameter(startValue), endValue, new FixedParameter(periodMs));
    }

    public SawLFO(double startValue, double endValue, LXParameter periodMs) {
        this(new FixedParameter(startValue), new FixedParameter(endValue), periodMs);
    }

    public SawLFO(LXParameter startValue, LXParameter endValue, double periodMs) {
        this(startValue, endValue, new FixedParameter(periodMs));
    }

    public SawLFO(LXParameter startValue, double endValue, LXParameter periodMs) {
        this(startValue, new FixedParameter(endValue), periodMs);
    }

    public SawLFO(double startValue, LXParameter endValue, LXParameter periodMs) {
        this(new FixedParameter(startValue), endValue, periodMs);
    }

    public SawLFO(LXParameter startValue, LXParameter endValue,
            LXParameter periodMs) {
        this("SAW", startValue, endValue, periodMs);
    }

    public SawLFO(String label, double startValue, double endValue,
            double periodMs) {
        this(label, new FixedParameter(startValue), new FixedParameter(endValue),
                new FixedParameter(periodMs));
    }

    public SawLFO(String label, LXParameter startValue, double endValue,
            double periodMs) {
        this(label, startValue, new FixedParameter(endValue), new FixedParameter(
                periodMs));
    }

    public SawLFO(String label, double startValue, LXParameter endValue,
            double periodMs) {
        this(label, new FixedParameter(startValue), endValue, new FixedParameter(
                periodMs));
    }

    public SawLFO(String label, double startValue, double endValue,
            LXParameter periodMs) {
        this(label, new FixedParameter(startValue), new FixedParameter(endValue),
                periodMs);
    }

    public SawLFO(String label, LXParameter startValue, LXParameter endValue,
            double periodMs) {
        this(label, startValue, endValue, new FixedParameter(periodMs));
    }

    public SawLFO(String label, LXParameter startValue, double endValue,
            LXParameter periodMs) {
        this(label, startValue, new FixedParameter(endValue), periodMs);
    }

    public SawLFO(String label, double startValue, LXParameter endValue,
            LXParameter periodMs) {
        this(label, new FixedParameter(startValue), endValue, periodMs);
    }

    public SawLFO(String label, LXParameter startValue, LXParameter endValue,
            LXParameter periodMs) {
        super(label, startValue, endValue, periodMs);
    }

    @Override
    protected final double computeNormalizedValue(double deltaMs, double basis) {
        return basis;
    }

    @Override
    protected final double computeNormalizedBasis(double basis,
            double normalizedValue) {
        return normalizedValue;
    }
}