package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;


public class OscWindow extends CloseableWindow {
    protected final LX lx;
    protected final ParameterUI pui;

    public OscWindow(LX lx) {
        super("OSC");
        this.lx = lx;
        this.pui = ParameterUI.getDefault(lx).preferKnobs(false).preferIntWidget(ParameterUI.IntWidget.BOX);
    }

    @Override
    public void drawContents() {
        pui.draw(lx.engine.osc.transmitActive);
        pui.draw(lx.engine.osc.transmitHost);
        pui.draw(lx.engine.osc.transmitPort);
        UI.separator();
        pui.draw(lx.engine.osc.receiveActive);
        pui.draw(lx.engine.osc.receiveHost);
        pui.draw(lx.engine.osc.receivePort);
    }
}
