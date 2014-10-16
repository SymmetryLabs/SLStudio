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