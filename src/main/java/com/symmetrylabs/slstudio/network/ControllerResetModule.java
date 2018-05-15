package com.symmetrylabs.slstudio.network;

import com.symmetrylabs.util.NetworkUtils;
import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

import java.net.InetAddress;


public class ControllerResetModule {
    public final BooleanParameter enabled = new BooleanParameter("Controller reset enabled");

    public ControllerResetModule(LX lx) {
        //moduleRegistrar.modules.add(new Module("Reset all cubes", enabled, true));
        enabled.addListener(parameter -> {
            if (enabled.isOn()) {
                for (InetAddress broadcast : NetworkUtils.getBroadcastAddresses()) {
                    try (OpcSocket socket = new OpcSocket(broadcast)) {
                        // @Deprecated: Switch to SYMMETRY_LABS_RESET sysex after all controllers are updated to Aura.
                        socket.send(new OpcMessage(0x88, 2));
                    }
                }
            }
        });
    }
}
