package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.LXLook;


public class MasterWindow extends CloseableWindow {
    private final LX lx;
    private WepUI wepUi;
    private ParameterUI pui;

    public MasterWindow(LX lx) {
        super("Master", UI.WINDOW_ALWAYS_AUTO_RESIZE | UI.WINDOW_NO_RESIZE | UI.WINDOW_NO_TITLE_BAR);
        this.lx = lx;
        this.wepUi = new WepUI(lx, false, () -> UI.closePopup());
        this.pui = ParameterUI.getDefault(lx);
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

        LXLook look = lx.engine.getFocusedLook();
        UI.separator();
        pui.draw(look.crossfader);
        pui.draw(look.crossfaderBlendMode);
        pui.draw(look.cueA, true);
        UI.sameLine();
        pui.draw(look.cueB, true);
        pui.draw(lx.engine.speed);
    }
}
