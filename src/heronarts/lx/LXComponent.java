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

import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.LXParameterized;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for system components that run in the engine, which have common
 * attributes, such as parameters, modulators, and layers. For instance,
 * patterns, transitions, and effects are all LXComponents.
 */
public abstract class LXComponent extends LXParameterized {

    protected final List<LXModulator> modulators = new ArrayList<LXModulator>();
    protected final List<LXLayer> layers = new ArrayList<LXLayer>();

    protected LXComponent() {
    }

    protected final LXModulator addModulator(LXModulator modulator) {
        this.modulators.add(modulator);
        return modulator;
    }

    public final List<LXModulator> getModulators() {
        return this.modulators;
    }

    protected final LXLayer addLayer(LXLayer layer) {
        this.layers.add(layer);
        return layer;
    }

    public final List<LXLayer> getLayers() {
        return this.layers;
    }

}
