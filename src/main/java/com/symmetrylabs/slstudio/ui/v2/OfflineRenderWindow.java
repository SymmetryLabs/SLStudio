package com.symmetrylabs.slstudio.ui.v2;

import com.symmetrylabs.slstudio.output.OfflineRenderOutput;
import heronarts.lx.LX;


public class OfflineRenderWindow extends CloseableWindow {
    protected final LX lx;
    protected final ParameterUI pui;

    private OfflineRenderOutput output;

    public OfflineRenderWindow(LX lx) {
        super("OfflineRender");
        this.lx = lx;
        this.pui = ParameterUI.getDefault(lx).preferKnobs(false).preferIntWidget(ParameterUI.IntWidget.BOX);

        output = new OfflineRenderOutput(lx);
        lx.addOutput(output);
    }

    @Override
    public void drawContents() {
        pui.draw(lx.engine.renderOutputRef.pOutputFile);
        pui.draw(lx.engine.renderOutputRef.pFrameRate);
        pui.draw(lx.engine.renderOutputRef.externalSync);
        pui.draw(lx.engine.renderOutputRef.pFramesToCapture);
        pui.draw(lx.engine.renderOutputRef.pStart);
        pui.draw(lx.engine.renderOutputRef.pStatus);
    }
}
