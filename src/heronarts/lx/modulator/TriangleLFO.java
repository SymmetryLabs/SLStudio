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
 * A triangular LFO is a simple linear modulator that oscillates between a low
 * and hi value over a specified time period.
 */
public class TriangleLFO extends LXRangeModulator {

    public TriangleLFO(double startValue, double endValue, double periodMs) {
        this(new FixedParameter(startValue), new FixedParameter(endValue),
                new FixedParameter(periodMs));
    }

    public TriangleLFO(LXParameter startValue, double endValue, double periodMs) {
        this(startValue, new FixedParameter(endValue), new FixedParameter(periodMs));
    }

    public TriangleLFO(double startValue, LXParameter endValue, double periodMs) {
        this(new FixedParameter(startValue), endValue, new FixedParameter(periodMs));
    }

    public TriangleLFO(double startValue, double endValue, LXParameter periodMs) {
        this(new FixedParameter(startValue), new FixedParameter(endValue), periodMs);
    }

    public TriangleLFO(LXParameter startValue, LXParameter endValue,
            double periodMs) {
        this(startValue, endValue, new FixedParameter(periodMs));
    }

    public TriangleLFO(LXParameter startValue, double endValue,
            LXParameter periodMs) {
        this(startValue, new FixedParameter(endValue), periodMs);
    }

    public TriangleLFO(double startValue, LXParameter endValue,
            LXParameter periodMs) {
        this(new FixedParameter(startValue), endValue, periodMs);
    }

    public TriangleLFO(LXParameter startValue, LXParameter endValue,
            LXParameter periodMs) {
        this("TRI", startValue, endValue, periodMs);
    }

    public TriangleLFO(String label, double startValue, double endValue,
            double periodMs) {
        this(label, new FixedParameter(startValue), new FixedParameter(endValue),
                new FixedParameter(periodMs));
    }

    public TriangleLFO(String label, LXParameter startValue, double endValue,
            double periodMs) {
        this(label, startValue, new FixedParameter(endValue), new FixedParameter(
                periodMs));
    }

    public TriangleLFO(String label, double startValue, LXParameter endValue,
            double periodMs) {
        this(label, new FixedParameter(startValue), endValue, new FixedParameter(
                periodMs));
    }

    public TriangleLFO(String label, double startValue, double endValue,
            LXParameter periodMs) {
        this(label, new FixedParameter(startValue), new FixedParameter(endValue),
                periodMs);
    }

    public TriangleLFO(String label, LXParameter startValue,
            LXParameter endValue, double periodMs) {
        this(label, startValue, endValue, new FixedParameter(periodMs));
    }

    public TriangleLFO(String label, LXParameter startValue, double endValue,
            LXParameter periodMs) {
        this(label, startValue, new FixedParameter(endValue), periodMs);
    }

    public TriangleLFO(String label, double startValue, LXParameter endValue,
            LXParameter periodMs) {
        this(label, new FixedParameter(startValue), endValue, periodMs);
    }

    public TriangleLFO(String label, LXParameter startValue,
            LXParameter endValue, LXParameter periodMs) {
        super(label, startValue, endValue, periodMs);
    }

    @Override
    protected double computeNormalizedValue(double deltaMs, double basis) {
        if (basis < 0.5) {
            return 2. * basis;
        } else {
            return 1. - 2. * (basis - 0.5);
        }
    }

    @Override
    protected double computeNormalizedBasis(double basis, double normalizedValue) {
        if (basis < 0.5) {
            return normalizedValue / 2.;
        } else {
            return 1 - (normalizedValue / 2.);
        }
    }
}