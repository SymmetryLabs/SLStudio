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

/**
 * A click is a simple modulator that fires a value of 1 every time its period
 * has passed. Otherwise it always returns 0.
 */
public class Click extends LXModulator {
    private double elapsedMs = 0;
    
    public Click(double periodMs) {
        super(periodMs);
    }

    public Click stopAndReset() {
        this.stop();
        this.elapsedMs = 0;
        this.setBasis(0);
        return this;
    }

    public LXModulator fire() {
        this.elapsedMs = 0;
        setValue(1);
        return this.start();
    }

    public boolean click() {
        return this.getValue() == 1;
    }    
    
    @Override
    protected double computeValue(double deltaMs) {
        this.elapsedMs += deltaMs;
        if (this.elapsedMs >= this.periodMs) {
            this.elapsedMs = this.elapsedMs % this.periodMs;
            return 1;
        }
        return 0;
    }
    
    @Override
    protected double computeBasis() {
        // The basis is sort of irrelevant for this modulator
        return getValue() < 1 ? 0 : 1;
    }
}