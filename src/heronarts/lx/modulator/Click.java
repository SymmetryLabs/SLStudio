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

import heronarts.lx.parameter.FixedParameter;
import heronarts.lx.parameter.LXParameter;

/**
 * A click is a simple modulator that fires a value of 1 every time its period
 * has passed. Otherwise it always returns 0.
 */
public class Click extends LXPeriodicModulator {
    
    private double elapsedMs = 0;
    
    public Click(double periodMs) {
        this(new FixedParameter(periodMs));
    }
    
    public Click(LXParameter periodMs) {
        this("CLICK", periodMs);
    }
    
    public Click(String label, double periodMs) {
        this(label, new FixedParameter(periodMs));
    }
    
    public Click(String label, LXParameter periodMs) {
        super(label, periodMs);
    }

    /**
     * Stops the modulator and sets it back to its initial state. 
     * 
     * @return this
     */
    public Click stopAndReset() {
        this.stop();
        this.elapsedMs = 0;
        this.setBasis(0);
        return this;
    }

    /**
     * Sets the value of the click to 1, so that code querying it in this frame
     * of execution sees it as active. On the next iteration of the runloop it
     * will be off again.
     *  
     * @return this
     */
    public LXModulator fire() {
        this.elapsedMs = 0;
        setValue(1);
        return this.start();
    }

    /**
     * Helper to conditionalize logic based on the click. Typical use is to
     * query as follows:
     * <pre>
     * if (clickInstance.click()) {
     *   // perform periodic operation
     * }
     * </pre>
     * 
     * @return true if the value is 1, otherwise false
     */
    public boolean click() {
        return this.getValue() == 1;
    }    
    
    @Override
    protected double computeValue(double deltaMs) {
        double periodMs = getPeriod();
        this.elapsedMs += deltaMs;
        if (this.elapsedMs >= periodMs) {
            this.elapsedMs = this.elapsedMs % periodMs;
            return 1;
        }
        return 0;
    }
    
    @Override
    protected double computeBasis() {
        // The basis is indeterminate for this modulator, it can only be
        // specifically known when the value is 1.
        return getValue() < 1 ? 0 : 1;
    }
}