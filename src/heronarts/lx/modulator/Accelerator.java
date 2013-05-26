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

public class Accelerator extends LXModulator {
    private double startVal;
    private double initSpeed;
    private double speed;
    private double accel;

    public Accelerator(double startVal, double initSpeed, double accel) {
        this.startVal = this.value = startVal;
        this.initSpeed = this.speed = initSpeed;
        this.accel = accel;
    }

    public LXModulator trigger() {
        this.speed = this.initSpeed;
        this.value = this.startVal;
        return this.start();
    }

    public LXModulator setDuration(double durationMs) {
        // Accelerators do not have a duration
        return this;
    }
    
    public double getSpeed() {
        return this.speed;
    }
    
    public float getSpeedf() {
        return (float)this.getSpeed();
    }
    
    public Accelerator setSpeed(double initSpeed, double accel) {
        this.initSpeed = initSpeed;
        this.accel = accel;
        return this;
    }

    protected void computeRun(int deltaMs) {
        this.value += this.speed * deltaMs / 1000.0;
        this.speed += this.accel * deltaMs / 1000.0;
    }
}