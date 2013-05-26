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

import java.util.ArrayList;
import java.util.List;

public abstract class LXParameterized implements LXListenableParameter.Listener {
    
    private final List<LXParameter> parameters = new ArrayList<LXParameter>();
    
    protected final void addParameter(LXParameter parameter) {
        parameters.add(parameter);
        if (parameter instanceof LXListenableParameter) {
            ((LXListenableParameter)parameter).addListener(this);
        }
    }
    
    public final List<LXParameter> getParameters() {
        return parameters;
    }
    
    public /* abstract */ void onParameterChanged(LXParameter parameter) {}
}
