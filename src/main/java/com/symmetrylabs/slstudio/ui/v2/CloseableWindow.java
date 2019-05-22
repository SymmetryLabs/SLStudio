package com.symmetrylabs.slstudio.ui.v2;

public abstract class CloseableWindow implements Window {
    protected final String label;
    private final int windowFlags;
    private boolean closeAfterEnd = false;

    protected CloseableWindow(String label) {
        this(label, 0);
    }

    protected CloseableWindow(String label, int flags) {
        this.label = label;
        this.windowFlags = flags;
    }

    protected abstract void drawContents();

    protected void windowSetup() {
        UI.setNextWindowDefaults(300, 50, UIConstants.DEFAULT_WINDOW_WIDTH, 500);
    }

    @Override
    public final void draw() {
        windowSetup();
        if (!UI.beginClosable(label, windowFlags)) {
            UI.end();
            WindowManager.closeWindow(this);
            return;
        }
        try {
            drawContents();
        } finally {
            UI.end();
        }
        if (closeAfterEnd) {
            WindowManager.closeWindow(this);
        }
    }

    protected void markToClose() {
        closeAfterEnd = true;
    }
}
