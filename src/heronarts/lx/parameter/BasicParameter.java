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
 * Simple parameter class with a double value.
 */
public class BasicParameter extends LXListenableNormalizedParameter {
    
    public enum Scaling {
        LINEAR,
        QUAD_IN,
        QUAD_OUT
    };
    
    public class Range {
        
        public final double v0;
        public final double v1;
        public final double min;
        public final double max;
        public final Scaling scaling;

        private Range(double v0, double v1, Scaling scaling) {            
            this.v0 = v0;
            this.v1 = v1;
            if (v0 < v1) {
                this.min = v0;
                this.max = v1;
            } else {
                this.min = v1;
                this.max = v0;
            }
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
    
    public BasicParameter(String label, double value, double v1) {
        this(label, value, 0, v1);
    }
    
    public BasicParameter(String label, double value, double v0, double v1) {
        this(label, value, v0, v1, Scaling.LINEAR);
    }
    
    public BasicParameter(String label, double value, double v0, double v1, Scaling scaling) {
        super(label, (value < Math.min(v0,  v1)) ? Math.min(v0,  v1) : ((value > Math.max(v0,  v1)) ? Math.max(v0,  v1) : value));
        this.range = new Range(v0, v1, scaling);
    }
    
    /**
     * Sets the value of parameter using normal 0-1
     * 
     * @param normalized Value from 0-1 through the parameter range
     * @return this, for method chaining
     */
    public BasicParameter setNormalized(double normalized) {
        if (normalized < 0) {
            normalized = 0;
        } else if (normalized > 1) {
            normalized = 1;
        }
        switch (this.range.scaling) {
        case QUAD_IN:
            normalized = normalized * normalized;
            break;
        case QUAD_OUT:
            normalized = 1 - (1-normalized)*(1-normalized);
            break;
        }
        setValue(this.range.v0 + (this.range.v1 - this.range.v0)*normalized);
        return this;
    }
    
    /**
     * Gets a normalized value of the parameter from 0 to 1
     * 
     * @return Normalized value, from 0 to 1
     */
    public double getNormalized() {
        if (this.range.v0 == this.range.v1) {
            return 0;
        }
        double normalized = (getValue() - this.range.v0) / (this.range.v1 - this.range.v0);
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
        if (value < this.range.min) {
            value = this.range.min;
        } else if (value > this.range.max) {
            value = this.range.max;
        }
        return value;
    }

}
