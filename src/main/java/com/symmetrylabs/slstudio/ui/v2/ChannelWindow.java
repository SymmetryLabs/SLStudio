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
    }

    @Override
    protected void drawContents() {
        for (LXChannel chan : lx.engine.getChannels()) {
            UI.beginChild(chan.getLabel(), false, 0, 230, 0);
            new ChannelUi(lx, chan, wepUi).draw();
            UI.endChild();
            UI.sameLine();
        }
        if (UI.button("+")) {
            lx.engine.addTask(() -> lx.engine.setFocusedChannel(lx.engine.addChannel()));
        }
    }
}
