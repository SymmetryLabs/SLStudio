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

import heronarts.lx.control.LXParameter;
import heronarts.lx.control.LXParameterized;

import java.lang.Math;

/**
 * A Modulator is an abstraction for a variable with a value that varies over
 * time, such as an envelope or a low frequency oscillator. Some modulators run
 * continuously, others may halt after they reach a certain value.
 */
public abstract class LXModulator extends LXParameterized implements LXParameter {
    
    /**
     * Whether this modulator is currently running.
     */
    private boolean isRunning = false;

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
     * Sets the Modulator in motion
     */
    public final LXModulator start() {
        this.isRunning = true;
        return this;
    }

    /**
     * Pauses the modulator wherever it is. Internal state should be maintained.
     * A subsequent call to start() should result in the Modulator continuing as
     * it was running before.
     */
    public final LXModulator stop() {
        this.isRunning = false;
        return this;
    }

    /**
     * Indicates whether this modulator is running.
     */
    public final boolean isRunning() {
        return this.isRunning;
    }

    /**
     * Invoking the trigger() method restarts a modulator from its initial
     * value, and should also start the modulator if it is not already running.
     */
    public final LXModulator trigger() {
        this.reset();
        this.start();
        return this;
    }
    
    /**
     * Resets the modulator to its default condition and stops it.
     * 
     * @return this, for method chaining
     */
    public LXParameter reset() {
        this.stop();
        this.onReset();
        return this;
    }
    
    /**
     * Optional subclass method when reset happens.
     */
    protected /* abstract */ void onReset() {}

    /**
     * Retrieves the current value of the modulator in full precision
     * 
     * @return Current value of the modulator
     */
    public final double getValue() {
        return this.value;
    }

    /**
     * Retrieves the current value of the modulator in floating point precision,
     * useful when working directly with processing graphics libraries
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
    public LXModulator setValue(double value) {
        this.value = value;
        return this;
    }
    
    /**
     * Helper for subclasses to update value in situations where it needs
     * to be recomputed. This cannot be overriden, and subclasses may assume
     * that it ONLY updates the internal value without triggering any other
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
     * Applies updates to the modulator for the specified number of
     * milliseconds. This method is invoked by the core engine.
     *
     * @param deltaMs Milliseconds to advance by
     */
    public final void run(double deltaMs) {
        if (!this.isRunning) {
            return;
        }
        this.onRun(deltaMs);
        this.value = this.computeValue(deltaMs);
    }
    
    /**
     * Subclasses are notified when the modulator is running.
     * 
     * @param deltaMs Milliseconds passed since last invocation
     */
    protected /*abstract*/ void onRun(double deltaMs) {}
    
    /**
     * Implementation method to advance the modulator's internal state. Subclasses
     * must provide and update value appropriately.
     * 
     * @param deltaMs Number of milliseconds to advance by
     */
    protected abstract double computeValue(double deltaMs);

}
