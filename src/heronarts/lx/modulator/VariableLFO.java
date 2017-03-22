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

import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.FixedParameter;
import heronarts.lx.parameter.FunctionalParameter;

/**
 * A sawtooth LFO oscillates from one extreme value to another. When the later
 * value is hit, the oscillator rests to its initial value.
 */
public class VariableLFO extends LXRangeModulator implements LXWaveshape {

    public enum Waveshape {
        SIN,
        TRI,
        SQUARE,
        UP,
        DOWN
    };

    public final EnumParameter<Waveshape> waveshape = new EnumParameter<Waveshape>("Wave", Waveshape.SIN);

    public final CompoundParameter rate = new CompoundParameter("Rate", 1, 0.01, 10);
    public final CompoundParameter skew = new CompoundParameter("Skew", 0, -1, 1);
    public final CompoundParameter shape = new CompoundParameter("Shape", 0, -1, 1);
    public final CompoundParameter exp = new CompoundParameter("Exp", 0, -1, 1);
    public final CompoundParameter phase = new CompoundParameter("Phase", 0);

    public VariableLFO() {
        super("LFO", new FixedParameter(0), new FixedParameter(1), new FixedParameter(1000));
        setPeriod(new FunctionalParameter() {
            @Override
            public double getValue() {
                return 1000 / rate.getValue();
            }
        });
        addParameter(waveshape);
        addParameter(rate);
        addParameter(skew);
        addParameter(shape);
        addParameter(phase);
        addParameter(exp);
    }

    public LXWaveshape getWaveshape() {
        switch (this.waveshape.getEnum()) {
        case SIN: return LXWaveshape.SIN;
        case TRI: return LXWaveshape.TRI;
        case UP: return LXWaveshape.UP;
        case DOWN: return LXWaveshape.DOWN;
        case SQUARE: return LXWaveshape.SQUARE;
        }
        return LXWaveshape.SIN;
    }

    @Override
    protected final double computeNormalizedValue(double deltaMs, double basis) {
        return compute(basis);
    }

    @Override
    protected final double computeNormalizedBasis(double basis, double normalizedValue) {
        return invert(normalizedValue, basis);
    }

    @Override
    public double compute(double basis) {
        basis = basis + this.phase.getValue();
        if (basis > 1.) {
            basis = basis % 1.;
        }

        double skewValue = this.skew.getValue();
        double skewPower = (skewValue >= 0) ? (1 + 4*skewValue) : (1 / (1-4*skewValue));
        if (skewPower != 1) {
            basis = Math.pow(basis, skewPower);
        }
        double value = getWaveshape().compute(basis);
        double shapeValue = this.shape.getValue();
        double shapePower = (shapeValue <= 0) ? (1 - 4*shapeValue) : (1 / (1+4*shapeValue));
        if (shapePower != 1) {
            if (value >= 0.5) {
                value = 0.5 + 0.5 * Math.pow(2*(value-0.5), shapePower);
            } else {
                value = 0.5 - 0.5 * Math.pow(2*(0.5 - value), shapePower);
            }
        }
        double expValue = this.exp.getValue();
        double expPower = (expValue <= 0) ? (1 - 4*expValue) : (1 / (1+4*expValue));
        if (expPower != 1) {
            value = Math.pow(value, expPower);
        }
        return value;
    }

    @Override
    public double invert(double value, double basisHint) {
        // TODO(mcslee): implement shape anad bias inversion properly
        return getWaveshape().invert(value, basisHint);
    }
}