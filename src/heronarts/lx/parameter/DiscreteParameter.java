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

import java.lang.Math;

/**
 * Parameter type with a discrete set of possible integer values.
 */
public class DiscreteParameter extends LXListenableNormalizedParameter {

    public final int minValue;
    
    public final int maxValue;
    
    public final int range;
    
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
        this.minValue = min;
        this.maxValue = max - 1;
        this.range = max-min;
    }
            
    @Override
    protected double updateValue(double value) {
        return this.minValue + ((int)(value - this.minValue) % this.range);
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
