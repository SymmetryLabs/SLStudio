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
 * A listener interface to be notified of changes to the parameter value.
 */
public interface LXParameterListener {

    /**
     * Invoked when the value of a parameter is changed.
     * 
     * @param parameter The parameter that has changed its value
     */
    public void onParameterChanged(LXParameter parameter);
}