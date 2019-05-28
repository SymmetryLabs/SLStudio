package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.mutation.LXMutationSender;
import heronarts.lx.osc.LXOscEngine;

public class RemoteControlWindow extends CloseableWindow {
    private final LX lx;
    String target = "";

    RemoteControlWindow(LX lx) {
        super("Remote Control");
        this.lx = lx;
    }

    @Override
    protected void drawContents() {
        LXMutationSender sender = lx.engine.mutations.sender;
        target = UI.inputText("target", target);
        UI.labelText("status", sender.getStatus().toString());

        if (UI.button("Connect")) {
            sender.connect(target);
            lx.engine.osc.transmitHost.setValue(target);
            lx.engine.osc.transmitPort.setValue(LXOscEngine.DEFAULT_RECEIVE_PORT);
            lx.engine.osc.transmitActive.setValue(true);
        }
        UI.sameLine();
        if (UI.button("Disconnect")) {
            sender.disconnect();
            lx.engine.osc.transmitActive.setValue(false);
        }
    }
}
