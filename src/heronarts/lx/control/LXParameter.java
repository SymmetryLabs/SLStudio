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

public abstract class LXParameter {

    /**
     * A listener interface to be notified of changes to the parameter value.
     */
    public interface Listener {
        public void onParameterChanged(LXParameter parameter);
    }

    /**
     * A method to reset the value of the parameter, if a default is available.
     * Not necessarily defined for all parameters, may be ignored. 
     */
    public abstract LXParameter reset();
    
    /**
     * Sets the value of the parameter.
     * 
     * @param value The value
     */
    public abstract LXParameter setValue(double value);
    
    /**
     * Retrieves the value of the parameter
     * 
     * @return Parameter value
     */
    public abstract double getValue();
    
    /**
     * Utility helper function to get the value of the parameter as a float.
     * 
     * @return Parameter value as float
     */
    public final float getValuef() {
        return (float) getValue();
    }
    
    /**
     * Gets the label for this parameter
     * 
     * @return Label of parameter
     */
    public abstract String getLabel();
}
