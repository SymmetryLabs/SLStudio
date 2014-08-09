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
 * A MutableParameter is a parameter that has a value which can be changed to anything.
 */
public class MutableParameter extends LXListenableParameter {

    public MutableParameter() {
        super();
    }

    public MutableParameter(String label) {
        super(label);
    }

    public MutableParameter(double value) {
        super(value);
    }

    @Override
    protected double updateValue(double value) {
        return value;
    }

}
