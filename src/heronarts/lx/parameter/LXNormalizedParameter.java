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
 * A parameter that supports values in a normalized form, from 0 to 1. This
 * only makes sense for parameters with fixed, finite ranges. The calls to
 * setNormalized() and getNormalized() operate in this space, while getValue()
 * respects the actual given value range. 
 */
public interface LXNormalizedParameter extends LXParameter {
    
    /**
     * Sets the value or the parameter in normalized space from 0 to 1 
     * @param value The normalized value, from 0 to 1
     * @return this, for method chaining
     */
    public LXNormalizedParameter setNormalized(double value);
    
    /**
     * Gets the value of the parameter in a normalized space from 0 to 1
     * 
     * @return Value of parameter, normalized to range from 0 to 1
     */
    public double getNormalized();
    
    /**
     * Gets the value of the parameter in a normalized space as a float
     * 
     * @return Normalized value of parameter, in range from 0 to 1
     */
    public float getNormalizedf();
    
}
