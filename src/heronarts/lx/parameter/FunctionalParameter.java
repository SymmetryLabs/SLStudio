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

/**
 * An LXParameter that has a value computed by a function, which may combine the
 * values of other parameters, or call some function, etc.
 */
public abstract class FunctionalParameter implements LXParameter {
    
    private final String label;
    
    protected FunctionalParameter() {
        this("FUNC-PARAM");
    }
    
    protected FunctionalParameter(String label) {
        this.label = label;
    }
    
    /**
     * Does nothing, subclass may override.
     */
    public FunctionalParameter reset() {
        return this;
    }
        
    /**
     * Not supported for this parameter type unless subclass overrides.
     * 
     * @param value The value
     */
    public LXParameter setValue(double value) {
        throw new UnsupportedOperationException("FunctionalParameter does not support setValue()");
    }
    
    /**
     * Retrieves the value of the parameter, subclass must implement.
     * 
     * @return Parameter value
     */
    public abstract double getValue();
    
    /**
     * Utility helper function to get the value of the parameter as a float.
     * 
     * @return Parameter value as float
     */
    public float getValuef() {
        return (float) getValue();
    }
    
    /**
     * Gets the label for this parameter
     * 
     * @return Label of parameter
     */
    public final String getLabel() {
        return this.label;
    }

}
