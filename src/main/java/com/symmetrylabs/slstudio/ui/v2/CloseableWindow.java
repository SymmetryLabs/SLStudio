package com.symmetrylabs.slstudio.ui.v2;

public abstract class CloseableWindow implements Window {
    protected final String label;
    private boolean closeAfterEnd = false;

    public CloseableWindow(String label) {
        this.label = label;
    }

    protected abstract void drawContents();

    protected void windowSetup() {
        UI.setNextWindowDefaults(300, 50, UI.DEFAULT_WIDTH, 500);
    }

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
        if (closeAfterEnd) {
            WindowManager.closeWindow(this);
        }
    }

    protected void markToClose() {
        closeAfterEnd = true;
    }
}
