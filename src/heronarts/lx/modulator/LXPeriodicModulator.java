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

public abstract class LXPeriodicModulator extends LXModulator {

    /**
     * Whether this modulator runs continuously looping.
     */
    private boolean looping = true;
    
    /**
     * Whether the modulator finished on this cycle.
     */
    private boolean finished = false;    
    
    /**
     * The basis is a value that moves from 0 to 1 through the period
     */
    private double basis = 0;
    
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
     * Utility constructor with period
     * 
     * @param periodMs Oscillation period, in milliseconds
     */
    protected LXPeriodicModulator(double periodMs) {
        this.periodMs = periodMs;
    }
    
    protected LXPeriodicModulator setLooping(boolean looping) {
        this.looping = looping;
        return this;
    }
    
    /**
     * Accessor for the current basis
     * 
     * @return The basis of the modulator
     */
    public final double getBasis() {
        return this.basis;
    }
    
    @Override
    protected void onTrigger() {
        this.setBasis(0);
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
        updateValue(this.computeValue(0));
        return this;
    }

    /**
     * Set the modulator to a certain value in its cycle.
     * 
     * @param value The value to apply
     * @return This modulator, for method chaining
     */
    @Override
    public LXModulator setValue(double value) {
        super.setValue(value);
        this.updateBasis();
        return this;
    }
    
    protected void updateBasis() {
        this.basis = computeBasis();
    }

    /**
     * @deprecated Use setPeriod
     */
    @Deprecated public final LXPeriodicModulator setDuration(double durationMs) {
        return this.setPeriod(durationMs);
    }

    /**
     * @deprecated Use modulatePeriodBy
     */
    @Deprecated public final LXPeriodicModulator modulateDurationBy(LXModulator durationModulator) {
        return this.modulatePeriodBy(durationModulator);
    }
    
    /**
     * Modify the period of this modulator
     * 
     * @param periodMs New period, in milliseconds
     * @return Modulator, for method chaining;
     */
    public final LXPeriodicModulator setPeriod(double periodMs) {
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
    final public LXPeriodicModulator modulatePeriodBy(LXModulator periodModulator) {
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
    final public LXPeriodicModulator modulatePeriodBy(LXParameter periodParameter, double minPeriod, double maxPeriod) {
        this.periodModulator = null;
        this.periodParameter = periodParameter;
        this.minPeriodMs = minPeriodMs;
        this.maxPeriodMs = maxPeriodMs;
        return this;
    }
    
    @Override
    protected void onRun(double deltaMs) {
        this.finished = false;
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
                this.stop();
            }
        }
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
     * Implementation method to compute the appropriate basis for a modulator given
     * its current value.
     */
    abstract protected double computeBasis();

}
