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
 * This class provides a common interface for system components to have
 * parameters that can modify their operation. Any LXComponent can have
 * parameters, such as a pattern, effect, or transition.
 */
public interface LXParameter {

    /**
     * A method to reset the value of the parameter, if a default is available.
     * Not necessarily defined for all parameters, may be ignored.
     */
    public abstract Object reset();

    /**
     * Sets the value of the parameter.
     *
     * @param value The value
     */
    public LXParameter setValue(double value);

    /**
     * Retrieves the value of the parameter
     *
     * @return Parameter value
     */
    public double getValue();

    /**
     * Utility helper function to get the value of the parameter as a float.
     *
     * @return Parameter value as float
     */
    public float getValuef();

    /**
     * Gets the label for this parameter
     *
     * @return Label of parameter
     */
    public String getLabel();
}
