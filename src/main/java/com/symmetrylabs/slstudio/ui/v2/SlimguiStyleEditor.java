package com.symmetrylabs.slstudio.ui.v2;

public class SlimguiStyleEditor implements Window {
    public SlimguiStyleEditor() {
    }

    @Override
    public final void draw() {
        if (!UI.showStyleEditor()) {
            WindowManager.get().closeWindow(this);
            return;
        }
    }
}
