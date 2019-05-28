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

        if (UI.button("Connect")) {
            sender.connect(target);
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

        long lastFrameNanos = renderer.getLastFrameTimeNanos();
        if (lastFrameNanos < 0) {
            UI.text("no frame data received");
        } else {
            frameTimeAverages.add(1e9f / (float) renderer.getLastFrameTimeNanos());
            float avg = 0;
            for (int i = 0; i < frameTimeAverages.size(); i++) {
                avg += frameTimeAverages.get(i);
            }
            avg /= frameTimeAverages.size();
            if (frameTimeAverages.size() > 50) {
                frameTimeAverages.remove(0);
            }
            UI.labelText("recv rate", String.format("%.0f fps", avg));
        }
    }
}
