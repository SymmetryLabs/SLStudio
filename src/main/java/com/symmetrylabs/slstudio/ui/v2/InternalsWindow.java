package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;

public class InternalsWindow extends CloseableWindow {
    private final LX lx;
    private final SLStudioGDX parent;

    InternalsWindow(LX lx, SLStudioGDX parent) {
        super("Internals");
        this.lx = lx;
        this.parent = parent;
    }

    @Override
    protected void windowSetup() {
        UI.setNextWindowDefaults(25, 500, UI.DEFAULT_WIDTH, 300);
    }

    @Override
    protected void drawContents() {
        UI.text("engine average: % 4.0fms, % 3.0ffps",
                        1e-6f * lx.engine.timer.runAvgNanos,
                        1e9f / lx.engine.timer.runAvgNanos);
        UI.text("    worst-case: % 4.0fms, % 3.0ffps",
                        1e-6f * lx.engine.timer.runWorstNanos,
                        1e9f / lx.engine.timer.runWorstNanos);
        UI.text("ui frame rate:  % 3.0ffps", UI.getFrameRate());
        parent.clearRGB = UI.colorPicker("background", parent.clearRGB);
    }
}
