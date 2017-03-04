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
 * An accelerator is a free-running modulator that changes its value based on
 * velocity and acceleration, measured in units/second and units/second^2,
 * respectively.
 */
public class Accelerator extends LXModulator {

    private double initValue;
    private double initVelocity;

    private double velocity;

    private LXParameter acceleration;

    public Accelerator(double initValue, double initVelocity, double acceleration) {
        this(initValue, initVelocity, new FixedParameter(acceleration));
    }

    public Accelerator(double initValue, double initVelocity,
            LXParameter acceleration) {
        this("ACCEL", initValue, initVelocity, acceleration);
    }

    public Accelerator(String label, double initValue, double initVelocity,
            double acceleration) {
        this(label, initValue, initVelocity, new FixedParameter(acceleration));
    }

    public Accelerator(String label, double initValue, double initVelocity,
            LXParameter acceleration) {
        super(label);
        setValue(this.initValue = initValue);
        setSpeed(initVelocity, acceleration);
    }

    @Override
    protected void onReset() {
        this.velocity = this.initVelocity;
        setValue(this.initValue);
    }

    /**
     * @return the current velocity
     */
    public double getVelocity() {
        return this.velocity;
    }

    /**
     * @return the current velocity as a floating point
     */
    public float getVelocityf() {
        return (float) this.getVelocity();
    }

    /**
     * @return The current acceleration
     */
    public double getAcceleration() {
        return this.acceleration.getValue();
    }

    /**
     * @return The current acceleration, as a float
     */
    public float getAccelerationf() {
        return (float) this.getAcceleration();
    }

    public Accelerator setSpeed(double initVelocity, double acceleration) {
        return setSpeed(initVelocity, new FixedParameter(acceleration));
    }

    /**
     * Sets both the velocity and acceleration of the modulator. Updates the
     * default values so that a future call to trigger() will reset to this
     * velocity.
     *
     * @param initVelocity New velocity
     * @param acceleration Acceleration
     * @return this
     */
    public Accelerator setSpeed(double initVelocity, LXParameter acceleration) {
        this.velocity = this.initVelocity = initVelocity;
        this.acceleration = acceleration;
        return this;
    }

    /**
     * Updates the velocity. Does not reset the default.
     *
     * @param velocity New velocity
     * @return this
     */
    public Accelerator setVelocity(double velocity) {
        this.velocity = velocity;
        return this;
    }

    public Accelerator setAcceleration(double acceleration) {
        return setAcceleration(new FixedParameter(acceleration));
    }

    /**
     * Updates the acceleration.
     *
     * @param acceleration New acceleration
     * @return this
     */
    public Accelerator setAcceleration(LXParameter acceleration) {
        this.acceleration = acceleration;
        return this;
    }

    @Override
    protected double computeValue(double deltaMs) {
        double a = getAcceleration();
        double dt = deltaMs / 1000.;
        // v(t) = v(0) + a*t
        this.velocity += a * dt;
        // s(t) = s(0) + v*t + (1/2)a*t^2
        return this.getValue() + this.velocity * dt + 0.5 * a * dt * dt;
    }
}