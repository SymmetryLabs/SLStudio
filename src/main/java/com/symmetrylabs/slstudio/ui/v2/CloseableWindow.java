package com.symmetrylabs.slstudio.ui.v2;

public abstract class CloseableWindow implements Window {
    protected final String label;

    public CloseableWindow(String label) {
        this.label = label;
    }

    protected abstract void drawContents();
    protected void windowSetup() {}

    @Override
    public final void draw() {
        windowSetup();
        if (!UI.beginClosable(label)) {
            UI.end();
            WindowManager.closeWindow(this);
            return;
        }
        drawContents();
        UI.end();
    }
}
