package com.symmetrylabs.slstudio.ui.gdx;

import java.util.ArrayList;
import java.util.List;

public class WindowManager {
    private static final WindowManager INSTANCE = new WindowManager();

    public static WindowManager get() {
        return INSTANCE;
    }

    private final List<Window> windows;
    private final List<Window> toAdd;
    private final List<Window> toRemove;
    private boolean uiEnabled;

    protected WindowManager() {
        windows = new ArrayList<>();
        toAdd = new ArrayList<>();
        toRemove = new ArrayList<>();
        uiEnabled = true;
    }

    public void add(Window w) {
        toAdd.add(w);
    }

    public void draw() {
        if (!uiEnabled) {
            return;
        }
        for (Window w : windows) {
            w.draw();
        }
        windows.removeAll(toRemove);
        toRemove.clear();
        windows.addAll(toAdd);
        toAdd.clear();
    }

    void windowClosed(Window w) {
        toRemove.add(w);
    }

    void enableUI() {
        uiEnabled = true;
    }

    void disableUI() {
        uiEnabled = false;
    }
}
