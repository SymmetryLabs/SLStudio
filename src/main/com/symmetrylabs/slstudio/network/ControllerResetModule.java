package com.symmetrylabs.slstudio.network;

import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;


public class ControllerResetModule {
    public final BooleanParameter enabled = new BooleanParameter("Controller reset enabled");

    public ControllerResetModule(LX lx) {
        //moduleRegistrar.modules.add(new Module("Reset all cubes", enabled, true));
        enabled.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                if (enabled.isOn()) new CubeResetter().run();
            }
        });
    }
}
