package com.symmetrylabs.slstudio.output;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.BooleanParameter;

import com.symmetrylabs.slstudio.network.ControllerResetModule;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

/*
 * Output Component
 *---------------------------------------------------------------------------*/
public final class OutputControl extends LXComponent {
    public final ControllerResetModule controllerResetModule;

    public final BooleanParameter enabled;
    public final BooleanParameter broadcastPacket = new BooleanParameter("Broadcast packet enabled", false);
    public final BooleanParameter testBroadcast = new BooleanParameter("broadcast (10.255.255.255)", false);
    public final BooleanParameter testUnicast = new BooleanParameter("test unicast", false);



    public OutputControl(LX lx) {
        super(lx, "Output Control");

        controllerResetModule = new ControllerResetModule(lx);

        enabled = lx.engine.output.enabled;
        addParameter(broadcastPacket);
        addParameter(testUnicast);
        addParameter(testBroadcast);

        testBroadcast.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) {
                if (parameter == testBroadcast && testBroadcast.isOn()){
                    testUnicast.setValue(!testBroadcast.isOn());
                }
            }
        });
        testUnicast.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) {
                if (parameter == testUnicast && testUnicast.isOn()){
                    testBroadcast.setValue(!testUnicast.isOn());
                }
            }
        });
    }
}
