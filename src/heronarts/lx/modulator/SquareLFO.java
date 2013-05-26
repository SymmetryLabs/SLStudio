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
 * Simple square wave LFO. Not damped. Oscillates between a low and high value.
 */
public class SquareLFO extends LXModulator {
    private double lowVal;
    private double hiVal;

    private boolean high;
    private double thresholdMs;
    private double elapsedMs;

    public SquareLFO(double lowVal, double hiVal, double durationMs) {
        this.value = this.lowVal = lowVal;
        this.hiVal = hiVal;
        this.setDuration(durationMs);
    }

    public LXModulator trigger() {
        this.high = false;
        return this.start();
    }

    protected void computeRun(int deltaMs) {
        this.elapsedMs += deltaMs;
        if (this.elapsedMs >= this.thresholdMs) {
            this.high = !this.high;
            while (this.elapsedMs >= this.thresholdMs) {
                this.elapsedMs -= this.thresholdMs;
            }
        }
    }

    public SquareLFO setDuration(double durationMs) {
        this.thresholdMs = durationMs / 2;
        return this;
    }

    public double getValue() {
        return this.high ? this.hiVal : this.lowVal;
    }

    public LXModulator setValue(double value) {
        this.high = (value == this.hiVal);
        return this;
    }
}