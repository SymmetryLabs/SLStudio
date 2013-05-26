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
 * A click is a simple modulator that fires a value of 1 every time its duration
 * has passed. Otherwise it always returns 0.
 */
public class Click extends LXModulator {
    private double elapsedMs;
    private double durationMs;

    public Click(double durationMs) {
        this.elapsedMs = 0;
        this.durationMs = durationMs;
    }

    public Click stopAndReset() {
        this.stop();
        this.value = 0;
        return this;
    }

    public LXModulator trigger() {
        this.elapsedMs = 0;
        return this.start();
    }

    public LXModulator fire() {
        this.elapsedMs = durationMs;
        return this.start();
    }

    public Click setDuration(double durationMs) {
        this.durationMs = durationMs;
        return this;
    }

    public double getDuration() {
        return this.durationMs;
    }

    protected void computeRun(int deltaMs) {
        this.elapsedMs += deltaMs;
        this.value = 0.f;
        while (this.elapsedMs >= this.durationMs) {
            this.value = 1.f;
            this.elapsedMs -= this.durationMs;
        }
    }

    public boolean click() {
        return this.getValue() == 1.0;
    }
}