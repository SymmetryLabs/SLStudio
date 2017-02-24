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

package heronarts.lx.parameter;

/**
 * Simple parameter class with a double value.
 */
public class BoundedParameter extends LXListenableNormalizedParameter {

    /**
     * Scaling functions determine how the BoundedParameter is interpolated from
     * its normalized values of 0-1 onto the actual range.
     */
    public enum Scaling {
        /**
         * Linear scaling between v0 and v1
         */
        LINEAR,

        /**
         * Quadratic easing in from v0 to v1
         */
        QUAD_IN,

        /**
         * Quadratic easing out from v0 to v1
         */
        QUAD_OUT
    };

    public class Range {

        public final double v0;
        public final double v1;
        public final double min;
        public final double max;
        public final Scaling scaling;

        private Range(double v0, double v1, Scaling scaling) {
            this.v0 = v0;
            this.v1 = v1;
            if (v0 < v1) {
                this.min = v0;
                this.max = v1;
            } else {
                this.min = v1;
                this.max = v0;
            }
            this.scaling = scaling;
        }
    }

    /**
     * Range of the parameter
     */
    public final Range range;

    /**
     * Underlying LXListenableParameter that this wraps
     */
    private final LXListenableParameter underlying;

    /**
     * Labeled parameter with value of 0 and range of 0-1
     *
     * @param label Label for parameter
     */
    public BoundedParameter(String label) {
        this(label, 0);
    }

    /**
     * A bounded parameter from 0-1 with scaling function, initial value is 0.
     *
     * @param label Label
     * @param scaling Scaling function
     */
    public BoundedParameter(String label, Scaling scaling) {
        this(label, 0, scaling);
    }

    /**
     * A bounded parameter with label and value, initial value of 0 and a range of 0-1
     *
     * @param label Label
     * @param value value
     */
    public BoundedParameter(String label, double value) {
        this(label, value, 1);
    }

    /**
     * A bounded parameter from 0-1 with scaling function
     *
     * @param label Label
     * @param value Initial value
     * @param scaling Scaling function
     */
    public BoundedParameter(String label, double value, Scaling scaling) {
        this(label, value, 0, 1, scaling);
    }

    /**
     * A bounded parameter with an initial value, and range from 0 to max
     *
     * @param label Label
     * @param value value
     * @param max Maximum value
     */
    public BoundedParameter(String label, double value, double max) {
        this(label, value, 0, max);
    }

    /**
     * A bounded parameter from 0-max with a scaling function
     *
     * @param label Label
     * @param value Initial value
     * @param max Max value
     * @param scaling Scaling function
     */
    public BoundedParameter(String label, double value, double max, Scaling scaling) {
        this(label, value, 0, max, scaling);
    }

    /**
     * A bounded parameter with initial value and range from v0 to v1. Note that it is not necessary for
     * v0 to be less than v1, if it is desired for the knob's value to progress negatively.
     *
     * @param label Label
     * @param value Initial value
     * @param v0 Start of range
     * @param v1 End of range
     */
    public BoundedParameter(String label, double value, double v0, double v1) {
        this(label, value, v0, v1, Scaling.LINEAR);
    }

    /**
     * A bounded parameter with a scaling function applied.
     *
     * @param label Label
     * @param value Initial value
     * @param v0 Start value
     * @param v1 End value
     * @param scaling Scaling function
     */
    public BoundedParameter(String label, double value, double v0, double v1,
            Scaling scaling) {
        this(label, value, v0, v1, scaling, null);
    }

    /**
     * Creates a BoundedParameter which limits the value of an underlying MutableParameter to a given
     * range. Changes to the BoundedParameter are forwarded to the MutableParameter, and vice versa.
     * If the MutableParameter is set to a value outside the specified bounds, this BoundedParmaeter
     * will ignore the update and the values will be inconsistent. The typical use of this mode is
     * to create a parameter suitable for limited-range UI control of a parameter, typically a
     * MutableParameter.
     *
     * @param underlying The underlying parameter
     * @param v0 Beginning of range
     * @param v1 End of range
     */
    public BoundedParameter(LXListenableParameter underlying, double v0, double v1) {
        this(underlying, v0, v1, Scaling.LINEAR);
    }

    /**
     * Creates a BoundedParameter which limits the value of an underlying MutableParameter to a given
     * range. Changes to the BoundedParameter are forwarded to the MutableParameter, and vice versa.
     * If the MutableParameter is set to a value outside the specified bounds, this BoundedParmaeter
     * will ignore the update and the values will be inconsistent. The typical use of this mode is
     * to create a parameter suitable for limited-range UI control of a parameter, typically a
     * MutableParameter.
     *
     * @param underlying The underlying parameter
     * @param v0 Beginning of range
     * @param v1 End of range
     * @param scaling Scaling function
     */
    public BoundedParameter(LXListenableParameter underlying, double v0, double v1, Scaling scaling) {
        this(underlying.getLabel(), underlying.getValue(), v0, v1, scaling, underlying);
    }

    private BoundedParameter(String label, double value, double v0, double v1,
        Scaling scaling, LXListenableParameter underlying) {
        super(label, (value < Math.min(v0, v1)) ? Math.min(v0, v1) : ((value > Math
                .max(v0, v1)) ? Math.max(v0, v1) : value));
        this.range = new Range(v0, v1, scaling);
        this.underlying = underlying;
        if (this.underlying != null) {
            this.underlying.addListener(new LXParameterListener() {
                public void onParameterChanged(LXParameter p) {
                    // NOTE: if the MutableParameter is set to a value outside our range, we ignore it
                    // and the values diverge.
                    double v = p.getValue();
                    if (v >= range.min && v <= range.max) {
                        setValue(v);
                    }
                }
            });
        }
    }

    /**
     * Sets the value of parameter using normal 0-1
     *
     * @param normalized Value from 0-1 through the parameter range
     * @return this, for method chaining
     */
    public BoundedParameter setNormalized(double normalized) {
        if (normalized < 0) {
            normalized = 0;
        } else if (normalized > 1) {
            normalized = 1;
        }
        switch (this.range.scaling) {
        case QUAD_IN:
            normalized = normalized * normalized;
            break;
        case QUAD_OUT:
            normalized = 1 - (1 - normalized) * (1 - normalized);
            break;
        default:
        case LINEAR:
            break;
        }
        setValue(this.range.v0 + (this.range.v1 - this.range.v0) * normalized);
        return this;
    }

    /**
     * Get the range of values for this parameter
     *
     * @return range from min and max
     */
    public double getRange() {
        return Math.abs(this.range.max - this.range.min);
    }

    /**
     * Gets a normalized value of the parameter from 0 to 1
     *
     * @return Normalized value, from 0 to 1
     */
    public double getNormalized() {
        if (this.range.v0 == this.range.v1) {
            return 0;
        }
        double normalized = (getValue() - this.range.v0)
                / (this.range.v1 - this.range.v0);
        switch (this.range.scaling) {
        case QUAD_IN:
            normalized = Math.sqrt(normalized);
            break;
        case QUAD_OUT:
            normalized = 1 - Math.sqrt(1 - normalized);
            break;
        default:
        case LINEAR:
            break;
        }
        return normalized;
    }

    /**
     * Normalized value as a float
     *
     * @return Normalized value from 0-1 as a float
     */
    public float getNormalizedf() {
        return (float) getNormalized();
    }

    @Override
    protected double updateValue(double value) {
        if (value < this.range.min) {
            value = this.range.min;
        } else if (value > this.range.max) {
            value = this.range.max;
        }
        if (this.underlying != null) {
            this.underlying.setValue(value);
        }
        return value;
    }

}
