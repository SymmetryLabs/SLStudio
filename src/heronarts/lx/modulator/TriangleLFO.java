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
 * A triangular LFO is a simple linear modulator that oscillates between a low
 * and hi value over a specified time period.
 */
public class TriangleLFO extends LXModulator {
    private double startVal;
    private double endVal;
    
    private double step = 0;
    private boolean inverted = false;

    public TriangleLFO(double startVal, double endVal, double durationMs) {
        this.value = startVal;
        this.setRange(startVal, endVal, durationMs);
    }

    public TriangleLFO reverse() {
        this.step = -this.step;
        return this;
    }

    public TriangleLFO setRange(double startVal, double endVal, 
            double durationMs) {
        this.startVal = startVal;
        this.endVal = endVal;

        double newStep = (endVal - startVal) * 2.f / durationMs;
        this.step = ((this.step < 0) ^ this.inverted) ? -newStep : newStep;

        if (this.startVal > this.endVal) {
            double temp = this.startVal;
            this.startVal = this.endVal;
            this.endVal = temp;
            this.inverted = true;
        }
        this.value = LXUtils.constrain(this.value, this.startVal, this.endVal);

        return this;
    }

    public TriangleLFO setDuration(double durationMs) {
        double stepMag = (endVal - startVal) * 2. / durationMs;
        this.step = (this.step > 0) ? stepMag : -stepMag;
        return this;
    }

    public LXModulator trigger() {
        if (this.inverted) {
            this.step = -Math.abs(this.step);
            this.value = this.endVal;
        } else {
            this.step = Math.abs(this.step);
            this.value = this.startVal;
        }
        return this.start();
    }

    protected void computeRun(int deltaMs) {
        this.value += this.step * deltaMs;
        while (true) {
            boolean overshoot = (this.step > 0) && (this.value > this.endVal);
            boolean undershoot = (this.step < 0)
                    && (this.value < this.startVal);
            if (overshoot) {
                this.value = this.endVal - (this.value - this.endVal);
                this.step = -this.step;
            } else if (undershoot) {
                this.value = this.startVal + (this.startVal - this.value);
                this.step = -this.step;
            } else {
                break;
            }
        }
    }
}