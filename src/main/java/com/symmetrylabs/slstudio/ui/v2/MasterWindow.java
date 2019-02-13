package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.LXMasterChannel;
import heronarts.lx.LXPattern;
import heronarts.lx.warp.LXWarp;
import java.util.ArrayList;
import java.util.List;

public class MasterWindow extends CloseableWindow {
    private final LX lx;
    private WepUi wepUi;

    public MasterWindow(LX lx) {
        super("Master");
        this.lx = lx;
        this.wepUi = new WepUi(lx, false, () -> UI.closePopup());
    }

    @Override
    protected void windowSetup() {
        UI.setNextWindowDefaults(25, 500, 500, 800);
    }

    @Override
    protected void drawContents() {
        LXMasterChannel mc = lx.engine.masterChannel;
        ChannelUi.drawEffects(lx, "Master", mc);
        ChannelUi.drawWarps(lx, "Master", mc);
        ChannelUi.drawWepPopup(lx, mc, wepUi);
    }
}
