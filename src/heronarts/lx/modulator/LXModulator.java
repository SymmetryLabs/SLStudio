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

import heronarts.lx.LXRunnable;
import heronarts.lx.parameter.LXParameter;

/**
 * A Modulator is an abstraction for a variable with a value that varies over
 * time, such as an envelope or a low frequency oscillator. Some modulators run
 * continuously, others may halt after they reach a certain value.
 */
public abstract class LXModulator extends LXRunnable implements LXParameter {

    /**
     * The current computed value of this modulator.
     */
    private double value = 0;

    private final String label;

    /**
     * Quick helper to get half of PI.
     */
    public static final double HALF_PI = Math.PI / 2.;

    /**
     * Quick helper to get two times PI.
     */
    public static final double TWO_PI = Math.PI * 2.;

    /**
     * Utility default constructor
     *
     * @param label Label
     */
    protected LXModulator(String label) {
        this.label = label;
    }

    public final String getLabel() {
        return this.label;
    }


        /**
     * Retrieves the current value of the modulator in full precision
     *
     * @return Current value of the modulator
     */
    public final double getValue() {
        return this.value;
    }

    /**
     * Retrieves the current value of the modulator in floating point precision.
     *
     * @return Current value of the modulator, cast to float
     */
    public final float getValuef() {
        return (float) this.getValue();
    }

    /**
     * Set the modulator to a certain value in its cycle.
     *
     * @param value The value to apply
     * @return This modulator, for method chaining
     */
    public final LXModulator setValue(double value) {
        this.value = value;
        this.onSetValue(value);
        return this;
    }

    /**
     * Subclasses may override when actions are necessary on value change.
     *
     * @param value New value
     */
    protected/* abstract */void onSetValue(double value) {
    }

    /**
     * Helper for subclasses to update value in situations where it needs to be
     * recomputed. This cannot be overriden, and subclasses may assume that it
     * ONLY updates the internal value without triggering any other
     * recomputations.
     *
     * @param value
     * @return this, for method chaining
     */
    protected final LXModulator updateValue(double value) {
        this.value = value;
        return this;
    }

    /**
     * Applies updates to the modulator for the specified number of milliseconds.
     * This method is invoked by the core engine.
     *
     * @param deltaMs Milliseconds to advance by
     */
    @Override
    protected final void run(double deltaMs) {
        this.value = this.computeValue(deltaMs);
    }

    /**
     * Implementation method to advance the modulator's internal state. Subclasses
     * must provide and update value appropriately.
     *
     * @param deltaMs Number of milliseconds to advance by
     */
    protected abstract double computeValue(double deltaMs);

}
