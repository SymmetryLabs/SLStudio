package com.symmetrylabs.slstudio.output;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.BooleanParameter;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.network.ControllerResetModule;
import com.symmetrylabs.layouts.dollywood.ButterflyPixlite;

 /*
 * Output Component
 *---------------------------------------------------------------------------*/
public final class OutputControl extends LXComponent {
    public final ControllerResetModule controllerResetModule;

    public final BooleanParameter enabled;
    public final BooleanParameter broadcastPacket = new BooleanParameter("Broadcast packet enabled", false);
    public final BooleanParameter testBroadcast = new BooleanParameter("Test broadcast enabled", false);

    public OutputControl(LX lx) {
        super(lx, "Output Control");

        controllerResetModule = new ControllerResetModule(lx);

        enabled = lx.engine.output.enabled;
        addParameter(broadcastPacket);
        addParameter(testBroadcast);

        enabled.addListener(param -> {
            boolean isEnabled = ((BooleanParameter) param).isOn();
            for (ButterflyPixlite mappingPixlite : SLStudio.applet.mappingPixlites) {
                mappingPixlite.enabled.setValue(isEnabled);
            }
        });
    }
}
