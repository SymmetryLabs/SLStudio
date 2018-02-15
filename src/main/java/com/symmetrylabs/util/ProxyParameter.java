package com.symmetrylabs.slstudio.util;

import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.LXParameter;


public class ProxyParameter extends BoundedParameter {
    public ProxyParameter(final BoundedParameter parameter) {
        super(parameter.getLabel() + "-proxy");
        setValue(parameter.getValue());

        addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                parameter.setValue(getValue());
            }
        });
    }
}
