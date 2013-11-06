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

/**
 * Simple parameter class with a double value.
 */
public class BasicParameter extends LXListenableParameter {
    
    public class Range {
        public final double min;
        public final double max;

        private Range(double min, double max) {
            this.min = min;
            this.max = max;
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
     * 
     * @param label
     * @param value
     */
    public BasicParameter(String label, double value) {
        this(label, value, 1);
    }
    
    public BasicParameter(String label, double value, double max) {
        this(label, value, 0, max);
    }
    
    public BasicParameter(String label, double value, double min, double max) {
        super(label, value);
        this.range = new Range(min, max);
    }
    
    @Override
    protected double updateValue(double value) {
        if (value < this.range.min) value = this.range.min;
        else if (value > this.range.max) value = this.range.max;
        return value;
    }

}
