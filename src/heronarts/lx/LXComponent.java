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

package heronarts.lx;

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.control.LXListenableParameter;
import heronarts.lx.control.LXParameter;
import heronarts.lx.modulator.LXModulator;

public class LXComponent implements LXParameter.Listener {

    protected final List<LXParameter> parameters = new ArrayList<LXParameter>();
    protected final List<LXModulator> modulators = new ArrayList<LXModulator>();
    protected final List<LXLayer> layers = new ArrayList<LXLayer>();
    
    protected final LXModulator addModulator(LXModulator modulator) {
        this.modulators.add(modulator);
        return modulator;
    }
    
    public final List<LXModulator> getModulators() {
        return this.modulators;
    }
    
    protected final LXLayer addLayer(LXLayer layer) {
        this.layers.add(layer);
        for (LXModulator m : layer.getModulators()) {
            addModulator(m);
        }
        for (LXParameter p : layer.getParameters()) {
            addParameter(p);
        }
        return layer;
    }
    
    protected final LXParameter addParameter(LXParameter parameter) {
        this.parameters.add(parameter);
        if (parameter instanceof LXListenableParameter) {
            ((LXListenableParameter)parameter).addListener(this);
        }
        return parameter;
    }
    
    public final List<LXParameter> getParameters() {
        return this.parameters;
    }
    
    public /* abstract */ void onParameterChanged(LXParameter parameter) {}

}
