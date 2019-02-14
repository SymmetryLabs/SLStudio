package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.warp.LXWarp;
import java.util.ArrayList;
import java.util.List;

public class ChannelWindow extends CloseableWindow {
    private final LX lx;
    private WepUi wepUi;

    public ChannelWindow(LX lx) {
        super("Channels");
        this.lx = lx;
        this.wepUi = new WepUi(lx, () -> UI.closePopup());
    }

    @Override
    protected void windowSetup() {
        UI.setNextWindowDefaults(25, 500, 500, 800);
    }

    @Override
    protected void drawContents() {
        for (LXChannel chan : lx.engine.getChannels()) {
            UI.beginChild(chan.getLabel(), false, 0, 230, 0);
            ChannelUi.draw(lx, chan, wepUi);
            UI.endChild();
            UI.sameLine();
        }
        if (UI.button("+")) {
            /* running this has the potential to cause CME issues in both the UI
               and the engine, so we have to sync the world to do it. */
            WindowManager.runSafelyWithEngine(lx, () -> lx.engine.setFocusedChannel(lx.engine.addChannel()));
        }
    }
}
