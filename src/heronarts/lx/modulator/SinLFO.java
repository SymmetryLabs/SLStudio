/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.modulator;

import heronarts.lx.parameter.FixedParameter;
import heronarts.lx.parameter.LXParameter;

/**
 * A classic sinusoidal oscillator.
 */
public class SinLFO extends LXRangeModulator {

    public SinLFO(double startValue, double endValue, double periodMs) {
        this(new FixedParameter(startValue), new FixedParameter(endValue),
                new FixedParameter(periodMs));
    }

    public SinLFO(LXParameter startValue, double endValue, double periodMs) {
        this(startValue, new FixedParameter(endValue), new FixedParameter(periodMs));
    }

    public SinLFO(double startValue, LXParameter endValue, double periodMs) {
        this(new FixedParameter(startValue), endValue, new FixedParameter(periodMs));
    }

    public SinLFO(double startValue, double endValue, LXParameter periodMs) {
        this(new FixedParameter(startValue), new FixedParameter(endValue), periodMs);
    }

    public SinLFO(LXParameter startValue, LXParameter endValue, double periodMs) {
        this(startValue, endValue, new FixedParameter(periodMs));
    }

    public SinLFO(LXParameter startValue, double endValue, LXParameter periodMs) {
        this(startValue, new FixedParameter(endValue), periodMs);
    }

    public SinLFO(double startValue, LXParameter endValue, LXParameter periodMs) {
        this(new FixedParameter(startValue), endValue, periodMs);
    }

    public SinLFO(LXParameter startValue, LXParameter endValue,
            LXParameter periodMs) {
        this("SIN", startValue, endValue, periodMs);
    }

    public SinLFO(String label, double startValue, double endValue,
            double periodMs) {
        this(label, new FixedParameter(startValue), new FixedParameter(endValue),
                new FixedParameter(periodMs));
    }

    public SinLFO(String label, LXParameter startValue, double endValue,
            double periodMs) {
        this(label, startValue, new FixedParameter(endValue), new FixedParameter(
                periodMs));
    }

    public SinLFO(String label, double startValue, LXParameter endValue,
            double periodMs) {
        this(label, new FixedParameter(startValue), endValue, new FixedParameter(
                periodMs));
    }

    public SinLFO(String label, double startValue, double endValue,
            LXParameter periodMs) {
        this(label, new FixedParameter(startValue), new FixedParameter(endValue),
                periodMs);
    }

    public SinLFO(String label, LXParameter startValue, LXParameter endValue,
            double periodMs) {
        this(label, startValue, endValue, new FixedParameter(periodMs));
    }

    public SinLFO(String label, LXParameter startValue, double endValue,
            LXParameter periodMs) {
        this(label, startValue, new FixedParameter(endValue), periodMs);
    }

    public SinLFO(String label, double startValue, LXParameter endValue,
            LXParameter periodMs) {
        this(label, new FixedParameter(startValue), endValue, periodMs);
    }

    public SinLFO(String label, LXParameter startValue, LXParameter endValue,
            LXParameter periodMs) {
        super(label, startValue, endValue, periodMs);
    }

    @Override
    protected double computeNormalizedValue(double deltaMs, double basis) {
        return (1 + Math.sin(basis * TWO_PI - HALF_PI)) / 2.;
    }

    @Override
    protected double computeNormalizedBasis(double basis, double normalizedValue) {
        double sinValue = -1 + 2. * normalizedValue;
        double angle = Math.asin(sinValue);
        if (basis > 0.5) {
            angle = Math.PI - angle;
        }
        return (angle + HALF_PI) / TWO_PI;
    }
}
