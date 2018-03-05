package com.symmetrylabs.slstudio.output;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.BooleanParameter;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.network.ControllerResetModule;
import com.symmetrylabs.slstudio.output.pixlites.NissanPixlite;

 /*
 * Output Component
 *---------------------------------------------------------------------------*/
public final class OutputControl extends LXComponent {
    public final BooleanParameter enabled;

    public final ControllerResetModule controllerResetModule = new ControllerResetModule(SLStudio.applet.lx);

    public final BooleanParameter broadcastPacket = new BooleanParameter("Broadcast packet enabled", false);
    public final BooleanParameter testBroadcast = new BooleanParameter("Test broadcast enabled", false);

    public OutputControl(LX lx) {
        super(lx, "Output Control");

        this.enabled = lx.engine.output.enabled;

        addParameter(testBroadcast);

        enabled.addListener(param -> {
            boolean isEnabled = ((BooleanParameter)param).isOn();

            for (NissanPixlite pixlite : SLStudio.applet.pixlites) {
                pixlite.enabled.setValue(isEnabled);
            }

            for (SLController c : SLStudio.applet.controllers) {
                c.enabled.setValue(isEnabled);
            }
        });
    }
}