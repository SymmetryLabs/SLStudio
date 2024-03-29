package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import java.lang.String;


public class TapTempoWindow extends CloseableWindow {
    private final LX lx;
    private final ParameterUI pui;

    public TapTempoWindow(LX lx) {
        super("Tap Tempo");
        this.lx = lx;
        this.pui = ParameterUI.getDefault(lx);
    }

    @Override
    protected void windowSetup() {
        UI.setNextWindowDefaults(325, 30, 200, 120);
    }

    @Override
    protected void drawContents() {
        pui.draw(lx.tempo.enabled);

        UI.spacing(1, 3);

        UI.pushColor(UI.COLOR_BUTTON, UIConstants.BLUE);
        UI.pushColor(UI.COLOR_BUTTON_HOVERED, UIConstants.BLUE_HOVER);
        if (UI.button("TAP", 50, 30)) {
            lx.tempo.tap();
        }
        UI.popColor(2);

        UI.sameLine();
        if (UI.button("<", 30, 30)) {
            lx.tempo.nudgeDown.setValue(true);
        }

        UI.sameLine();
        if (UI.button(">", 30, 30)) {
            lx.tempo.nudgeUp.setValue(true);
        }

        UI.sameLine();
        if (UI.button("Trigger", 70, 30)) {
            lx.tempo.trigger();
        }

        UI.pushFont(FontLoader.DEFAULT_FONT_L);
        int bpm = UI.intBox("bpm", (int) lx.tempo.bpm.getValue(), 0.5f, (int) lx.tempo.MIN_BPM, (int) lx.tempo.MAX_BPM, null);
        lx.tempo.bpm.setValue(bpm);
        UI.popFont();
    }
}
