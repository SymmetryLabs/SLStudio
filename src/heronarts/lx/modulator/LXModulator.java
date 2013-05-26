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

import java.lang.Math;

/**
 * A Modulator is an abstraction for a variable with a value that varies over
 * time, such as an envelope or a low frequency oscillator. Some modulators run
 * continuously, others may halt after they reach a certain value.
 */
public abstract class LXModulator {
    protected boolean running = false;
    protected double value;

    private LXModulator durationModulator = null;
    
    public static double HALF_PI = Math.PI / 2.;
    public static double TWO_PI = Math.PI * 2.;
    
    /**
     * Sets the Modulator in motion
     */
    public LXModulator start() {
        this.running = true;
        return this;
    }

    /**
     * Pauses the modulator wherever it is. Internal state should be maintained.
     * A subsequent call to start() should result in the Modulator continuing as
     * it was running before.
     */
    public LXModulator stop() {
        this.running = false;
        return this;
    }

    /**
     * Indicates whether this modulator is running.
     */
    public boolean isRunning() {
        return this.running;
    }

    /**
     * Invoking the trigger() method restarts a modulator from its initial
     * value, and should also start the modulator if it is not already running.
     */
    abstract public LXModulator trigger();

    /**
     * Retrieves the current value of the modulator in full precision
     * 
     * @return Current value of the modulator
     */
    public double getValue() {
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
     * @param value The value to apply
     * @return This modulator, for method chaining
     */
    public LXModulator setValue(double value) {
        this.value = value;
        return this;
    }

    /**
     * Modify the duration of this modulator
     * 
     * @param durationMs New duration, in milliseconds
     * @return Modulator, for method chaining;
     */
    abstract public LXModulator setDuration(double durationMs);
    
    /**
     * Sets another modulator to modulate the speed of this modulator
     * 
     * @param durationModulator Another modulator, which will update the duration
     * @return This modulator, for method chaining
     */
    final public LXModulator modulateDurationBy(LXModulator durationModulator) {
        this.durationModulator = durationModulator;
        return this;
    }
    
    /**
     * Applies updates to the modulator for the specified number of
     * milliseconds. This method is invoked by the core engine.
     *
     * @param deltaMs Milliseconds to advance by
     */
    public final void run(int deltaMs) {
        if (!this.running) {
            return;
        }
        if (this.durationModulator != null) {
            this.setDuration(this.durationModulator.getValue());
        }
        this.computeRun(deltaMs);
    }
    
    /**
     * Implementation method to advance the modulator's internal state. Subclasses
     * must provide and update value appropriately.
     * 
     * @param deltaMs Number of milliseconds to advance by
     */
    abstract protected void computeRun(int deltaMs);
}
