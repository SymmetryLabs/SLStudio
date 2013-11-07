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

import heronarts.lx.control.FixedParameter;
import heronarts.lx.control.LXParameter;

/**
 * An LXPeriodicModulator is a modulator that moves through a cycle in a given
 * amount of time. It may then repeat the cycle, or perform it once. The values
 * are computed based upon a position in the cycle, internally referred to as
 * a basis, which moves from 0 to 1. This can be thought of as equivalent to an
 * angle moving from 0 to two times pi. The period itself is a parameter which
 * may be a modulator or otherwise.
 */
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
    private LXParameter period;
    
    /**
     * Utility constructor with period
     * 
     * @param label Label
     * @param period Parameter for period
     */
    protected LXPeriodicModulator(String label, LXParameter period) {
        super(label);
        this.period = period;
    }
    
    /**
     * Sets whether the modulator should loop after it completes a cycle or halt
     * at the end position.
     * 
     * @param looping Whether to loop
     * @return this, for method chaining
     */
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
    protected void onReset() {
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
     * @deprecated Use setPeriod
     */
    @Deprecated public final LXPeriodicModulator modulateDurationBy(LXModulator durationModulator) {
        return this.setPeriod(durationModulator);
    }
    
    /**
     * Modify the period of this modulator
     * 
     * @param periodMs New period, in milliseconds
     * @return Modulator, for method chaining;
     */
    public final LXPeriodicModulator setPeriod(double periodMs) {
        this.period = new FixedParameter(periodMs);
        return this;
    }
    
    /**
     * @return The period of this modulator
     */
    public final double getPeriod() {
        return this.period.getValue();
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
        return getPeriod();
    }

    /**
     * Sets a parameter to the period of this modulator
     * 
     * @param period Parameter for period value
     * @return This modulator, for method chaining
     */
    final public LXPeriodicModulator setPeriod(LXParameter period) {
        this.period = period;
        return this;
    }
        
    @Override
    protected void onRun(double deltaMs) {
        this.finished = false;
        this.basis += deltaMs / this.period.getValue();
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
