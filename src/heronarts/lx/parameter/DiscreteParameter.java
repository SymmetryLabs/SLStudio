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

package heronarts.lx.parameter;

import java.lang.IllegalArgumentException;
import java.lang.Math;

/**
 * Parameter type with a discrete set of possible integer values.
 */
public class DiscreteParameter extends LXListenableNormalizedParameter {

    private int minValue;
    
    private int maxValue;
    
    private int range;
    
    /**
     * Parameter with values from [0, range-1], 0 by default
     * 
     * @param label Name of parameter
     * @param range range of values
     */
    public DiscreteParameter(String label, int range) {
        this(label, 0, range);
    }
    
    /**
     * Parameter with values from [min, max-1], min by default 
     * 
     * @param label
     * @param range
     * @param value
     */
    public DiscreteParameter(String label, int min, int max) {
        super(label, min);
        setRange(min, max);
    }
            
    @Override
    protected double updateValue(double value) {
        if (value < this.minValue) {
            return this.minValue + (this.range - ((int)(this.minValue - value) % this.range)) % this.range;
        }
        return this.minValue + ((int)(value - this.minValue) % this.range);
    }
    
    public int getMinValue() {
        return this.minValue;
    }
    
    public int getMaxValue() {
        return this.maxValue;
    }
    
    public int getRange() {
        return this.range;
    }
    
    /**
     * Sets the range from [minValue, maxValue-1] inclusive
     * 
     * @param minValue Minimum value
     * @param maxValue Maximum value, exclusive
     * @return this
     */
    public DiscreteParameter setRange(int minValue, int maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue - 1;
        if (this.maxValue < this.minValue) {
            throw new IllegalArgumentException("DiscreteParameter must have range of at least 1");
        }
        this.range = maxValue-minValue;
        setValue(updateValue(this.getValue()));
        return this;
    }
    
    /**
     * Sets range from [0, range-1] inclusive
     * 
     * @param range Number of discrete values
     * @return this
     */
    public DiscreteParameter setRange(int range) {
        return setRange(0, range);
    }
    
    public DiscreteParameter increment() {
        this.setValue(getValuei() + 1);
        return this;
    }
    
    public DiscreteParameter decrement() {
        this.setValue(getValuei() - 1);
        return this;
    }
    
    public int getValuei() {
        return (int) getValue();
    }
    
    public double getNormalized() {
        return (getValue() - this.minValue) / (this.range-1);
    }
    
    public float getNormalizedf() {
        return (float) getNormalized();
    }
    
    public DiscreteParameter setNormalized(double normalized) {
        int value = (int) Math.floor(normalized * this.range);
        if (value == this.range) {
            --value;
        }
        setValue(this.minValue + value);
        return this;
    }

}
