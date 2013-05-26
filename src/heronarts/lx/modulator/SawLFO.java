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

import heronarts.lx.LXUtils;

import java.lang.Math;


/**
 * A sawtooth LFO oscillates from one extreme value to another. When the later
 * value is hit, the oscillator rests to its initial value.
 */
public class SawLFO extends LXModulator {
    private double startVal;
    private double endVal;
    private double step;

    public SawLFO(double startVal, double endVal, double durationMs) {
        this.value = this.startVal = startVal;
        this.endVal = endVal;
        this.setDuration(durationMs);
    }

    public SawLFO setRange(double startVal, double endVal, double durationMs) {
        this.startVal = startVal;
        this.endVal = endVal;
        this.value = LXUtils.constrain(this.value, Math.min(startVal, endVal), Math.max(startVal, endVal));
        return this.setDuration(durationMs);
    }

    public SawLFO setDuration(double durationMs) {
        this.step = (this.endVal - this.startVal) / durationMs;
        return this;
    }

    public LXModulator trigger() {
        this.value = this.startVal;
        return this.start();
    }

    protected void computeRun(int deltaMs) {
        this.value += this.step * deltaMs;
        if ((this.step > 0) && (this.value > this.endVal)) {
            this.value = this.startVal + (this.value - this.endVal);
        } else if ((this.step < 0) && (this.value < this.endVal)) {
            this.value = this.startVal - (this.endVal - this.value);
        }
    }
}