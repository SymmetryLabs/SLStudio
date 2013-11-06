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
 * An accelerator is a free-running modulator that changes its value based
 * on velocity and acceleration.
 */
public class Accelerator extends LXModulator {
    
    private double initValue;
    private double initVelocity;
    
    private double velocity;
    private double acceleration;

    /**
     * Create an accelerator
     * 
     * @param initValue Initial value
     * @param initVelocity Initial velocity
     * @param acceleration Acceleration
     */
    public Accelerator(double initValue, double initVelocity, double acceleration) {
        this("ACCELERATOR", initValue, initVelocity, acceleration);
    }

    /**
     * Create an accelerator
     * 
     * @param label Label
     * @param initValue Initial value
     * @param initVelocity Initial velocity
     * @param acceleration Acceleration
     */
    public Accelerator(String label, double initValue, double initVelocity, double acceleration) {
        super(label);
        setValue(this.initValue = initValue);
        setSpeed(initVelocity, acceleration);
    }

    
    @Override
    protected void onReset() {
        this.velocity = this.initVelocity;
        setValue(this.initValue);
    }
    
    /**
     * @return the current velocity
     */
    public double getVelocity() {
        return this.velocity;
    }
    
    /**
     * @return the current velocity as a floating point
     */
    public float getVelocityf() {
        return (float)this.getVelocity();
    }
    
    /**
     * Sets both the velocity and acceleration of the modulator. Updates the
     * default values so that a future call to trigger() will reset to this
     * velocity.
     * 
     * @param initVelocity New velocity
     * @param acceleration Acceleration
     * @return this
     */
    public Accelerator setSpeed(double initVelocity, double acceleration) {
        this.velocity = this.initVelocity = initVelocity;
        this.acceleration = acceleration;
        return this;
    }
    
    /**
     * Updates the velocity. Does not reset the default.
     * 
     * @param velocity New velocity
     * @return this
     */
    public Accelerator setVelocity(double velocity) {
        this.velocity = velocity;
        return this;
    }
    
    /**
     * Updates the acceleration.
     * 
     * @param acceleration New acceleration
     * @return this
     */
    public Accelerator setAcceleration(double acceleration) {
        this.acceleration = acceleration;
        return this;
    }
    
    @Override
    protected double computeValue(double deltaMs) {
        this.velocity += this.acceleration * deltaMs / 1000.0;
        return this.getValue() + this.velocity * deltaMs / 1000.0;
    }
    
}