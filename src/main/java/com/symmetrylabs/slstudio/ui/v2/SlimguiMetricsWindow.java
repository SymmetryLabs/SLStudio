package com.symmetrylabs.slstudio.ui.v2;

public class SlimguiMetricsWindow implements Window {
    public SlimguiMetricsWindow() {
    }

    @Override
    public final void draw() {
        if (!UI.showMetricsWindow()) {
            WindowManager.get().closeWindow(this);
            return;
        }
    }
}
