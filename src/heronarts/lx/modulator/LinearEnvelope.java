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
 * This modulator is a simple linear ramp from one value to another over a
 * specified number of milliseconds.
 */
public class LinearEnvelope extends LXModulator {

    private double startVal;
    private double endVal;
    private double durationMs;

    private double step;

    public LinearEnvelope(double startVal, double endVal, double durationMs) {
        this.value = this.startVal;
        this.setRange(startVal, endVal, durationMs);
    }

    public LinearEnvelope trigger() {
        this.value = this.startVal;
        this.start();
        return this;
    }

    public LinearEnvelope setDuration(double durationMs) {
        this.durationMs = durationMs;
        this.step = (this.endVal - this.startVal) / durationMs;
        return this;
    }

    public LinearEnvelope setEndVal(double endVal) {
        return this.setRange(this.getValue(), endVal);
    }

    public LinearEnvelope setEndVal(double endVal, double durationMs) {
        return this.setRange(this.getValue(), endVal, durationMs);
    }

    public LinearEnvelope setRange(double startVal, double endVal,
            double durationMs) {
        this.durationMs = durationMs;
        return this.setRange(startVal, endVal);
    }

    public LinearEnvelope setRange(double startVal, double endVal) {
        this.startVal = startVal;
        this.endVal = endVal;
        this.step = (this.endVal - this.startVal) / this.durationMs;
        if (this.value < Math.min(this.startVal, this.endVal)
                || this.value > Math.max(this.startVal, this.endVal)) {
            this.value = this.startVal;
        }
        return this;
    }

    protected void computeRun(int deltaMs) {
        this.value += this.step * (double) deltaMs;

        // Check for hitting the end of the envelope
        if (((this.step > 0) && (this.value > this.endVal))
                || ((this.step < 0) && (this.value < this.endVal))) {
            this.value = this.endVal;
            this.running = false;
        }
    }
}