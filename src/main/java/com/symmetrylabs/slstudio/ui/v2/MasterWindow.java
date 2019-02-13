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

        ParameterUI.draw(lx, lx.engine.output.brightness);
        UI.separator();
        ParameterUI.draw(lx, lx.engine.crossfader);
        ParameterUI.draw(lx, lx.engine.crossfaderBlendMode);
        ParameterUI.draw(lx, lx.engine.cueA, true);
        UI.sameLine();
        ParameterUI.draw(lx, lx.engine.cueB, true);
        UI.separator();
        UI.text("Master Bus");
        LXMasterChannel mc = lx.engine.masterChannel;
        ChannelUi.drawEffects(lx, "Master", mc);
        ChannelUi.drawWarps(lx, "Master", mc);
        ChannelUi.drawWepPopup(lx, mc, wepUi);
    }
}
