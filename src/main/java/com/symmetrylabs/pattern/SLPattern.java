package com.symmetrylabs.pattern;

import com.symmetrylabs.model.SLModel;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */ /* Non-Patterns */
public abstract class SLPattern extends LXPattern {
    public final SLModel model;

    public SLPattern(LX lx) {
        super(lx);
        this.model = (SLModel) lx.model;
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
