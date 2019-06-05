package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.mutation.LXMutationSender;
import heronarts.lx.osc.LXOscEngine;

import java.util.ArrayList;
import java.util.List;

public class RemoteControlWindow extends CloseableWindow {
    private final LX lx;
    private final ViewController vc;
    private final RemoteRenderer renderer;
    private final List<Float> frameTimeAverages = new ArrayList<>();
    String target = "";

    RemoteControlWindow(LX lx, ViewController vc, RemoteRenderer renderer) {
        super("Remote Control");
        this.lx = lx;
        this.vc = vc;
        this.renderer = renderer;
    }

    @Override
    protected void drawContents() {
        LXMutationSender sender = lx.engine.mutations.sender;
        target = UI.inputText("target", target);

        renderer.setCullFactor(UI.sliderInt("downsample", renderer.getCullFactor(), 1, 5));

        if (UI.button("Connect")) {
            sender.connect(target, true);
            renderer.connect(target);
            lx.engine.osc.transmitHost.setValue(target);
            lx.engine.osc.transmitPort.setValue(LXOscEngine.DEFAULT_RECEIVE_PORT);
            lx.engine.osc.transmitActive.setValue(true);
            vc.setRemoteDataDisplayed(true);
        }
        UI.sameLine();
        if (UI.button("Disconnect")) {
            sender.disconnect();
            renderer.disconnect();
            lx.engine.osc.transmitActive.setValue(false);
            vc.setRemoteDataDisplayed(false);
        }

        UI.labelText("status", sender.getStatus().toString());
        vc.setRemoteDataDisplayed(UI.checkbox("streampixels", vc.isRemoteDataDisplayed()));

        UI.intBox("servertick", (int) renderer.latestTick);
        renderer.collectStats = UI.checkbox("collect render statistics", renderer.collectStats);
        if (renderer.collectStats) {
            UI.floatBox("packets/sec", renderer.packetsPerSecond);
            UI.floatBox("Mbps", renderer.megabitsPerSecond);
        }
    }
}
