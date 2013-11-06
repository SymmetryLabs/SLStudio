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
 * This class provides a common interface for system components to have parameters
 * that can modify their operation. Any LXComponent can have parameters, such as
 * a pattern, effect, or transition.
 */
public abstract class LXParameter {

    /**
     * A listener interface to be notified of changes to the parameter value.
     */
    public interface Listener {
        /**
         * Invoked when the value of a parameter is changed.
         * 
         * @param parameter The parameter that has changed its value
         */
        public void onParameterChanged(LXParameter parameter);
    }

    /**
     * A method to reset the value of the parameter, if a default is available.
     * Not necessarily defined for all parameters, may be ignored. 
     */
    public abstract LXParameter reset();
    
    /**
     * Resets the value of the parameter, giving it a new default. Future calls
     * to reset() with no parameter will use this value.
     * 
     * @param value New default value
     */
    public abstract LXParameter reset(double value);
    
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
