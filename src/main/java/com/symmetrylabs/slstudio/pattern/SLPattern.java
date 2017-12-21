package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;

 /* Non-Patterns */
public abstract class SLPattern extends SunsPattern {
    public SLPattern(LX lx) {
        super(lx);
    }

    protected <T extends LXParameter> T addParam(T param) {
        addParameter(param);
        return param;
    }

    protected BooleanParameter booleanParam(String name) {
        return addParam(new BooleanParameter(name));
    }

    protected BooleanParameter booleanParam(String name, boolean value) {
        return addParam(new BooleanParameter(name, value));
    }

    protected CompoundParameter compoundParam(String name, double value, double min, double max) {
        return addParam(new CompoundParameter(name, value, min, max));
    }

    protected DiscreteParameter discreteParameter(String name, int value, int min, int max) {
        return addParam(new DiscreteParameter(name, value, min, max));
    }
}
