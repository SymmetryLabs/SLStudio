package com.symmetrylabs.slstudio.ui.v2;

public class SlimguiDemoWindow implements Window {
    public SlimguiDemoWindow() {
    }

    @Override
    public final void draw() {
        if (!UI.showDemoWindow()) {
            WindowManager.get().closeWindow(this);
            return;
        }
    }
}
