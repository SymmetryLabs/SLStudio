package com.symmetrylabs.slstudio.ui.v2;

import com.symmetrylabs.slstudio.server.VolumeClient;
import heronarts.lx.LX;
import heronarts.lx.mutation.LXMutationSender;
import heronarts.lx.osc.LXOscEngine;

import java.util.ArrayList;
import java.util.List;

public class RemoteControlWindow extends CloseableWindow {
    private final VolumeClient vc;
    private String target;

    RemoteControlWindow(VolumeClient vc) {
        super("Remote Control");
        this.vc = vc;

        target = vc.getTarget();
        if (target == null) {
            target = "";
        }
    }

    @Override
    protected void drawContents() {
        target = UI.inputText("target", target);

        RemoteRenderer renderer = vc.getRemoteRenderer();
        renderer.setCullFactor(UI.sliderInt("downsample", renderer.getCullFactor(), 1, 5));

        if (UI.button("Connect + Pull")) {
            vc.connect(target, false);
        }
        UI.sameLine();
        if (UI.button("Connect + Push")) {
            vc.connect(target, true);
        }
        UI.sameLine();
        if (UI.button("Disconnect")) {
            vc.disconnect();
        }

        UI.labelText("connected", Boolean.toString(vc.isChannelConnected()));
        UI.labelText("receiving", Boolean.toString(vc.isRendererReceiving()));

        UI.intBox("servertick", (int) renderer.latestTick);
        renderer.collectStats = UI.checkbox("collect render statistics", renderer.collectStats);
        if (renderer.collectStats) {
            UI.floatBox("packets/sec", renderer.packetsPerSecond);
            UI.floatBox("Mbps", renderer.megabitsPerSecond);
        }
    }
}
