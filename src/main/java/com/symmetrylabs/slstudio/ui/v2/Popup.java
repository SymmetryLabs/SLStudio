package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.nio.file.Files;
import java.io.IOException;


public abstract class Popup implements Window {
    protected final String title;
    protected final boolean modal;
    private boolean opened = false;

    protected Popup(String title) {
        this(title, true);
    }

    protected Popup(String title, boolean modal) {
        this.title = title;
        this.modal = modal;
    }

    protected abstract void drawContents();
    protected void windowSetup() {}

    @Override
    public final void draw() {
        windowSetup();
        if (!opened) {
            UI.openPopup(title);
            opened = true;
        }
        if (!UI.beginPopup(title, true, UI.WINDOW_NO_MOVE | UI.WINDOW_NO_RESIZE)) {
            WindowManager.closeWindow(this);
            return;
        }
        drawContents();
        UI.endPopup();
    }

    protected void close() {
        WindowManager.closeWindow(this);
        UI.closePopup();
    }
}
