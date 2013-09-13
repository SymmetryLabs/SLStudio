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
    
    private double initValue;
    private double initVelocity;
    
    private double velocity;
    private double acceleration;

    public Accelerator(double initValue, double initVelocity, double acceleration) {
        setValue(this.initValue = initValue);
        setSpeed(initVelocity, acceleration);
    }

    protected void onTrigger() {
        this.velocity = this.initVelocity;
        setValue(this.initValue);
    }
    
    public double getVelocity() {
        return this.velocity;
    }
    
    public float getVelocityf() {
        return (float)this.getVelocity();
    }
    
    public Accelerator setSpeed(double initVelocity, double acceleration) {
        this.velocity = this.initVelocity = initVelocity;
        this.acceleration = acceleration;
        return this;
    }
    
    public Accelerator setVelocity(double velocity) {
        this.velocity = velocity;
        return this;
    }
    
    public Accelerator setAcceleration(double acceleration) {
        this.acceleration = acceleration;
        return this;
    }
    
    @Override
    protected double computeValue(int deltaMs) {
        this.velocity += this.acceleration * deltaMs / 1000.0;
        return this.getValue() + this.velocity * deltaMs / 1000.0;
    }
    
    @Override
    protected double computeBasis() {
        // This is undefined/irrelevant for an Accelerator
        return 0;
    }
}