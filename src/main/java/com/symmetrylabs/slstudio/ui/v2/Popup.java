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
    private boolean opened = false;

    public Popup(String title) {
        this.title = title;
    }

    protected abstract void drawContents();

    @Override
    public final void draw() {
        if (!opened) {
            UI.openPopup(title);
            opened = true;
        }
        if (!UI.beginPopup(title, true)) {
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
