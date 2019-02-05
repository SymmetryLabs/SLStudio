package com.symmetrylabs.slstudio.ui.v2;

public class SlimguiAboutWindow implements Window {
    public SlimguiAboutWindow() {
    }

    @Override
    public final void draw() {
        if (!UI.showAboutWindow()) {
            WindowManager.get().closeWindow(this);
            return;
        }
    }
}
