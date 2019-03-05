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
        super("Master", UI.WINDOW_ALWAYS_AUTO_RESIZE | UI.WINDOW_NO_RESIZE | UI.WINDOW_NO_TITLE_BAR);
        this.lx = lx;
        this.wepUi = new WepUi(lx, false, () -> UI.closePopup());
    }

    @Override
    protected void windowSetup() {
        UI.setNextWindowPosition(UI.width - 20, 40, 1.0f, 0.0f);
    }

    @Override
    protected void drawContents() {
        /* drawn without ParameterUI so that we can get custom labels */
        boolean live = UI.checkbox("LIVE", lx.engine.output.enabled.getValueb());
        if (live != lx.engine.output.enabled.getValueb()) {
            lx.engine.addTask(() -> lx.engine.output.enabled.setValue(live));
        }
        UI.sameLine();
        float level = UI.sliderFloat("##master-level", lx.engine.output.brightness.getValuef(), 0, 1);
        if (level != lx.engine.output.brightness.getValuef()) {
            lx.engine.addTask(() -> lx.engine.output.brightness.setValue(level));
        }

        UI.separator();
        ParameterUI.draw(lx, lx.engine.crossfader);
        ParameterUI.draw(lx, lx.engine.crossfaderBlendMode);
        ParameterUI.draw(lx, lx.engine.cueA, true);
        UI.sameLine();
        ParameterUI.draw(lx, lx.engine.cueB, true);
        UI.separator();
        UI.text("Master Bus");
        LXMasterChannel mc = lx.engine.masterChannel;
        ChannelUI.drawEffects(lx, "Master", mc);
        ChannelUI.drawWarps(lx, "Master", mc);
        ChannelUI.drawWepPopup(lx, mc, wepUi);
    }
}
