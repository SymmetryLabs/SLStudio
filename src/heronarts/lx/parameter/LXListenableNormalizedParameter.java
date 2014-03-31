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
 * A parameter that can be listened to and has normalized values. This is needed
 * for things like UI components such as a slider or a knob, which need to be
 * able to listen for changes to the parameter value and to update it in a
 * normalized range of values.
 */
public abstract class LXListenableNormalizedParameter extends
        LXListenableParameter implements LXNormalizedParameter {

    protected LXListenableNormalizedParameter(String label, double value) {
        super(label, value);
    }

}
