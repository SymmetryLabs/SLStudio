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

import java.lang.Math;

/**
 * A Modulator is an abstraction for a variable with a value that varies over
 * time, such as an envelope or a low frequency oscillator. Some modulators run
 * continuously, others may halt after they reach a certain value.
 */
public abstract class LXModulator {
    
    /**
     * Whether this modulator runs continuously looping.
     */
    protected boolean looping = true;
    
    /**
     * Whether this modulator is currently running.
     */
    private boolean isRunning = false;
    
    /**
     * Whether the modulator finished on this cycle.
     */
    private boolean finished = false;

    /**
     * The basis is a value that moves from 0 to 1.
     */
    private double basis = 0;
    
    /**
     * The current computed value of this modulator.
     */
    private double value = 0;
    
    /**
     * The number of milliseconds in the period of this modulator.
     */
    protected double periodMs = 0;

    /**
     * Another modulator which automatically modifies the period of this one.
     */
    private LXModulator periodModulator = null;
    
    /**
     * A parameter to modulate the period
     */
    private LXParameter periodParameter = null;
    
    private double minPeriodMs = 0;
    
    private double maxPeriodMs = 0;
    
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
     */
    protected LXModulator() {}
    
    /**
     * Utility constructor with period
     * 
     * @param periodMs Oscillation period, in ms
     */
    protected LXModulator(double periodMs) {
        this.periodMs = periodMs;
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
        this.setBasis(0);
        this.start();
        this.onTrigger();
        return this;
    }
    
    /**
     * Optional subclass method when trigger happens.
     */
    protected /* abstract */ void onTrigger() {}

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
     * Accessor for the current basis
     * 
     * @return The basis of the modulator
     */
    public final double getBasis() {
        return this.basis;
    }
    
    /**
     * Set the modulator to a certain value in its cycle.
     * 
     * @param value The value to apply
     * @return This modulator, for method chaining
     */
    public LXModulator setValue(double value) {
        this.value = value;
        this.basis = this.computeBasis();
        return this;
    }
    
    /**
     * Set the modulator to a certain basis position in its cycle.
     *  
     * @param basis
     * @return
     */
    public final LXModulator setBasis(double basis) {
        if (basis < 0) {
            basis = 0;
        } else if (basis > 1) {
            basis = 1;
        }
        this.basis = basis;
        this.value = this.computeValue(0);
        return this;
    }
    
    protected final void updateBasis() {
        this.basis = computeBasis(); 
    }

    /**
     * @deprecated Use setPeriod
     */
    @Deprecated public final LXModulator setDuration(double durationMs) {
        return this.setPeriod(durationMs);
    }

    /**
     * @deprecated Use modulatePeriodBy
     */
    @Deprecated public final LXModulator modulateDurationBy(LXModulator durationModulator) {
        return this.modulatePeriodBy(durationModulator);
    }
    
    /**
     * Modify the period of this modulator
     * 
     * @param periodMs New period, in milliseconds
     * @return Modulator, for method chaining;
     */
    public final LXModulator setPeriod(double periodMs) {
        this.periodMs = periodMs;
        return this;
    }
    
    /**
     * @return The period of this modulator
     */
    public final double getPeriod() {
        return this.periodMs;
    }
    
    /**
     * @return The period of this modulator as a floating point
     */
    public final float getPeriodf() {
        return (float)this.getPeriod();
    }
    
    /**
     * @deprecated Use getPeriod()
     */
    @Deprecated public final double getDuration() {
        return this.getPeriod();
    }
    
    /**
     * Sets another modulator to modulate the speed of this modulator
     * 
     * @param periodModulator Another modulator, which will update the period
     * @return This modulator, for method chaining
     */
    final public LXModulator modulatePeriodBy(LXModulator periodModulator) {
        this.periodParameter = null;
        this.periodModulator = periodModulator;
        return this;
    }
    
    /**
     * Sets a parameter to tell this modulator what period to run at.
     * 
     * @param periodParameter The parameter to listen to
     * @param minPeriodMs The shortest period in ms
     * @param maxPerioMs The longest period in ms
     * @return This modulator, for chaining
     */
    final public LXModulator modulatePeriodBy(LXParameter periodParameter, double minPeriod, double maxPeriod) {
        this.periodModulator = null;
        this.periodParameter = periodParameter;
        this.minPeriodMs = minPeriodMs;
        this.maxPeriodMs = maxPeriodMs;
        return this;
    }
    
    /**
     * Applies updates to the modulator for the specified number of
     * milliseconds. This method is invoked by the core engine.
     *
     * @param deltaMs Milliseconds to advance by
     */
    public final void run(double deltaMs) {
        this.finished = false;
        if (!this.isRunning) {
            return;
        }
        if (this.periodModulator != null) {
            this.setPeriod(this.periodModulator.getValue());
        }
        if (this.periodParameter != null) {
            this.setPeriod(this.minPeriodMs + (this.maxPeriodMs-this.minPeriodMs)*this.periodParameter.getValue());
        }
        this.basis += deltaMs / this.periodMs;
        if (this.basis >= 1.) {
            if (this.looping) {
                if (this.basis > 1) {
                    this.basis = this.basis - Math.floor(this.basis);
                }
            } else {
                this.basis = 1.;
                this.finished = true;
                this.isRunning = false;
            }
        }
        this.value = this.computeValue(deltaMs);
    }
    
    /**
     * For envelope modulators, which are not looping, this returns true if
     * they finished on this frame.
     * 
     * @return true if the modulator just finished its operation on this frame
     */
    public final boolean finished() {
        return this.finished;
    }
    
    /**
     * Implementation method to advance the modulator's internal state. Subclasses
     * must provide and update value appropriately.
     * 
     * @param deltaMs Number of milliseconds to advance by
     */
    abstract protected double computeValue(double deltaMs);
    
    /**
     * Implementation method to compute the appropriate basis for a modulator given
     * its current value.
     */
    abstract protected double computeBasis();

}
