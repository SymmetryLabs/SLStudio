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

import heronarts.lx.parameter.BasicParameter;
import heronarts.lx.parameter.FixedParameter;
import heronarts.lx.parameter.LXParameter;

/**
 * A modulator that tracks the value of a parameter but damps motion over time according
 * to rules.
 */
public class DampedParameter extends LXModulator {

    private final LXParameter parameter;

    private final LXParameter velocity;

    private final LXParameter acceleration;

    private double currentVelocity = 0;

    public DampedParameter(String label, double velocity) {
        this(new BasicParameter(label, 0, Double.MIN_VALUE, Double.MAX_VALUE), velocity, 0);
    }

    public DampedParameter(LXParameter parameter, double velocity) {
        this(parameter, velocity, 0);
    }

    public DampedParameter(LXParameter parameter, double velocity, double acceleration) {
        this("DAMPED-" + parameter.getLabel(), parameter, velocity, acceleration);
    }

    public DampedParameter(LXParameter parameter, LXParameter velocity) {
        this("DAMPED-" + parameter.getLabel(), parameter, velocity, 0);
    }

    public DampedParameter(LXParameter parameter, LXParameter velocity, LXParameter acceleration) {
        this("DAMPED-" + parameter.getLabel(), parameter, velocity, acceleration);
    }

    public DampedParameter(String label, LXParameter parameter, LXParameter velocity) {
        this(label, parameter, velocity, 0);
    }

    public DampedParameter(String label, LXParameter parameter, double velocity, double acceleration) {
        this(label, parameter, new FixedParameter(velocity), new FixedParameter(acceleration));
    }

    public DampedParameter(String label, LXParameter parameter, LXParameter velocity, double acceleration) {
        this(label, parameter, velocity, new FixedParameter(acceleration));
    }

    public DampedParameter(String label, LXParameter parameter, LXParameter velocity, LXParameter acceleration) {
        super(label);
        this.parameter = parameter;
        this.velocity = velocity;
        this.acceleration = acceleration;
        updateValue(parameter.getValue());
    }

    @Override
    protected double computeValue(double deltaMs) {
        double value = getValue();

        double target = this.parameter.getValue();
        if (value == target) {
            this.currentVelocity = 0;
            return value;
        }

        double av = Math.abs(this.acceleration.getValue());
        double vv = Math.abs(this.velocity.getValue());

        double deltaS = deltaMs / 1000.;
        if (av > 0) {
            double position = value;
            if (target < value) {
                av = -av;
            }
            double decelTime = Math.abs(this.currentVelocity / av);
            double decelPosition = position + this.currentVelocity * decelTime - .5 * av * decelTime * decelTime;
            if (target > value) {
                // Moving positively
                if ((this.currentVelocity > 0) && (decelPosition > target)) {
                    // Decelerating
                    position = Math.min(target, value + this.currentVelocity * deltaS + .5 * -av * deltaS * deltaS);
                    this.currentVelocity = Math.max(0, this.currentVelocity - av * deltaS);
                } else {
                    // Accelerating
                    position = Math.min(target, value + this.currentVelocity * deltaS + .5 * av * deltaS * deltaS);
                    this.currentVelocity = Math.min(vv, this.currentVelocity + av * deltaS);
                }
            } else {
                // Moving negatively
                if ((this.currentVelocity < 0) && (decelPosition < target)) {
                    // Decelerating
                    position = Math.max(target, value + this.currentVelocity * deltaS + .5 * -av * deltaS * deltaS);
                    this.currentVelocity = Math.min(0, this.currentVelocity - av * deltaS);
                } else {
                    // Accelerating
                    position = Math.max(target, value + this.currentVelocity * deltaS + .5 * av * deltaS * deltaS);
                    this.currentVelocity = Math.max(-vv, this.currentVelocity + av * deltaS);
                }
            }
            return position;
        }

        // No acceleration mode
        double range = vv * deltaS;
        double after;
        if (target > value) {
            after = Math.min(value + range, target);
        } else {
            after = Math.max(value - range, target);
        }
        return after;

    }

    public LXParameter getParameter() {
        return this.parameter;
    }

}
