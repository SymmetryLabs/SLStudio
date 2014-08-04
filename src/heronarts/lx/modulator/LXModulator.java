/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx.modulator;

import heronarts.lx.LXLoopTask;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterized;
import heronarts.lx.parameter.LXParameterListener;

/**
 * A Modulator is an abstraction for a variable with a value that varies over
 * time, such as an envelope or a low frequency oscillator. Some modulators run
 * continuously, others may halt after they reach a certain value.
 */
public abstract class LXModulator extends LXParameterized implements
        LXParameter, LXLoopTask {

    /**
     * Whether this modulator is currently running.
     */
    public final BooleanParameter isRunning = new BooleanParameter("RUN", false);

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
        this.isRunning.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                if (isRunning.isOn()) {
                    onStart();
                } else {
                    onStop();
                }
            }
        });
    }

    public final String getLabel() {
        return this.label;
    }

    /**
     * Sets the Modulator in motion
     */
    public final LXModulator start() {
        this.isRunning.setValue(true);
        return this;
    }

    /**
     * Pauses the modulator wherever it is. Internal state should be maintained. A
     * subsequent call to start() should result in the Modulator continuing as it
     * was running before.
     */
    public final LXModulator stop() {
        this.isRunning.setValue(false);
        return this;
    }

    /**
     * Indicates whether this modulator is running.
     */
    public final boolean isRunning() {
        return this.isRunning.isOn();
    }

    /**
     * Invoking the trigger() method restarts a modulator from its initial value,
     * and should also start the modulator if it is not already running.
     */
    public final LXModulator trigger() {
        return this.reset().start();
    }

    /**
     * Resets the modulator to its default condition and stops it.
     *
     * @return this, for method chaining
     */
    public final LXModulator reset() {
        this.stop();
        this.onReset();
        return this;
    }

    /**
     * Optional subclass method when start happens.
     */
    protected/* abstract */void onStart() {

    }

    /**
     * Optional subclass method when stop happens.
     */
    protected/* abstract */void onStop() {

    }

    /**
     * Optional subclass method when reset happens.
     */
    protected/* abstract */void onReset() {
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
    public final void loop(double deltaMs) {
        if (this.isRunning.isOn()) {
            this.value = this.computeValue(deltaMs);
        }
    }

    /**
     * Implementation method to advance the modulator's internal state. Subclasses
     * must provide and update value appropriately.
     *
     * @param deltaMs Number of milliseconds to advance by
     */
    protected abstract double computeValue(double deltaMs);

}
