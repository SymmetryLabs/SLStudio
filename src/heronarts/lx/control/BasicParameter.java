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

package heronarts.lx.control;

import java.lang.Math;

/**
 * Simple parameter class with a double value.
 */
public class BasicParameter extends LXListenableParameter {
    
    public enum Scaling {
        LINEAR,
        QUAD_IN,
        QUAD_OUT
    };
    
    public class Range {
        
        public final double min;
        public final double max;
        public final Scaling scaling;

        private Range(double min, double max, Scaling scaling) {            
            this.min = min;
            this.max = max;
            this.scaling = scaling;
        }
    }
    
    /**
     * Range of the parameter
     */
    public final Range range;
    
    /**
     * Labeled parameter with value of 0 and range of 0-1
     * 
     * @param label Label for parameter
     */
    public BasicParameter(String label) {
        this(label, 0);
    }
        
    /**
     * Basic parameter with label and value
     * 
     * @param label Label
     * @param value value
     */
    public BasicParameter(String label, double value) {
        this(label, value, 1);
    }
    
    public BasicParameter(String label, double value, double max) {
        this(label, value, 0, max);
    }
    
    public BasicParameter(String label, double value, double min, double max) {
        this(label, value, min, max, Scaling.LINEAR);
    }
    
    public BasicParameter(String label, double value, double min, double max, Scaling scaling) {
        super(label, (value < min) ? min : ((value > max) ? max : value));
        this.range = new Range(min, max, scaling);
    }
    
    /**
     * Sets the value of parameter using normal 0-1
     * 
     * @param normalized Value from 0-1 through the parameter range
     * @return this, for method chaining
     */
    public BasicParameter setNormalized(double normalized) {
        if (normalized < 0) normalized = 0;
        else if (normalized > 1) normalized = 1;
        switch (this.range.scaling) {
        case QUAD_IN:
            normalized = normalized * normalized;
            break;
        case QUAD_OUT:
            normalized = 1 - (1-normalized)*(1-normalized);
            break;
        }
        setValue(this.range.min + (this.range.max - this.range.min)*normalized);
        return this;
    }
    
    /**
     * Gets a normalized value of the parameter from 0 to 1
     * 
     * @return Normalized value, from 0 to 1
     */
    public double getNormalized() {
        if (this.range.min == this.range.max) {
            return 0;
        }
        double normalized = (getValue() - this.range.min) / (this.range.max - this.range.min);
        switch (this.range.scaling) {
        case QUAD_IN:
            normalized = Math.sqrt(normalized);
            break;
        case QUAD_OUT:
            normalized = 1 - Math.sqrt(1-normalized);
            break;
        }
        return normalized;
    }
    
    /**
     * Normalized value as a float
     * 
     * @return Normalized value from 0-1 as a float
     */
    public float getNormalizedf() {
        return (float)getNormalized();
    }
    
    @Override
    protected double updateValue(double value) {
        if (value < this.range.min) value = this.range.min;
        else if (value > this.range.max) value = this.range.max;
        return value;
    }

}
