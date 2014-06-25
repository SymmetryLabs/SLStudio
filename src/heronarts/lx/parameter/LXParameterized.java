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

import java.util.ArrayList;
import java.util.List;

/**
 * Utility base class for objects that have parameters.
 */
public abstract class LXParameterized implements LXParameterListener {

    protected final List<LXParameter> parameters = new ArrayList<LXParameter>();

    protected LXParameterized() {
    }

    protected final LXParameterized addParameter(LXParameter parameter) {
        this.parameters.add(parameter);
        if (parameter instanceof LXListenableParameter) {
            ((LXListenableParameter) parameter).addListener(this);
        }
        return this;
    }

    protected final LXParameterized addParameters(List<LXParameter> parameters) {
        for (LXParameter parameter : parameters) {
            addParameter(parameter);
        }
        return this;
    }

    public final List<LXParameter> getParameters() {
        return this.parameters;
    }

    public final LXParameter getParameter(String label) {
        for (LXParameter parameter : this.parameters) {
            if (parameter.getLabel().equals(label)) {
                return parameter;
            }
        }
        return null;
    }

    /**
     * Subclasses are free to override this, but in case they don't care a default
     * implementation is provided.
     */
    public/* abstract */void onParameterChanged(LXParameter parameter) {
    }

}
