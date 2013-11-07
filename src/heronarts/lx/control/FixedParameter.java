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
 * A FixedParameter is an immutable parameter. It will throw a RuntimeException
 * if setValue() is attempted. Useful for anonymous placeholder values in places 
 * that expect to use LXParameters.
 */
public class FixedParameter implements LXParameter {

    class FixedParameterException extends RuntimeException {
        FixedParameterException() {
            super("Cannot call setValue() on a FixedParameter");
        }
    }
    
    private final double value;
    
    public FixedParameter(double value) {
        this.value = value;
    }
        
    @Override
    public LXParameter reset() {
        return this;
    }

    @Override
    public LXParameter setValue(double value) {
        throw new RuntimeException("Cannot setValue on a FixedParameter");
    }

    @Override
    public double getValue() {
        return this.value;
    }

    @Override
    public float getValuef() {
        return (float) this.value;
    }

    @Override
    public String getLabel() {
        return null;
    }

}
