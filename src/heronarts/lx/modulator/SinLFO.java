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

/**
 * A classic sinusoidal oscillator.
 */
public class SinLFO extends LXModulator {
    private double magnitude;
    private double median;
    private double angle;
    private double step;

    private double startVal;
    private double endVal;

    public SinLFO(double startVal, double endVal, double durationMs) {
        this.startVal = startVal;
        this.endVal = endVal;

        this.median = (startVal + endVal) / 2.0f;
        this.magnitude = (endVal - startVal) / 2.0f;
        this.angle = (startVal < endVal) ? -HALF_PI : HALF_PI;
        this.setDuration(durationMs);
        this.value = startVal;
    }

    public SinLFO setDuration(double durationMs) {
        this.step = TWO_PI / durationMs;
        return this;
    }

    public LXModulator setValue(double value) {
        this.value = LXUtils.constrain(value, Math.min(this.startVal, this.endVal), Math.max(this.startVal, this.endVal));    
        this.angle = Math.asin((this.value - this.median) / this.magnitude);
        return this;
    }

    public LXModulator trigger() {
        this.value = this.startVal;
        this.angle = (this.startVal < this.endVal) ? -HALF_PI : HALF_PI;
        return this.start();
    }

    protected void computeRun(int deltaMs) {
        this.angle += this.step * deltaMs;
        this.value = this.median + this.magnitude * Math.sin(this.angle);
    }
}