package com.symmetrylabs.slstudio.ui.v2;

import java.util.ArrayList;
import java.util.List;

public class WindowManager {
    private static WindowManager INSTANCE = new WindowManager();

    public static WindowManager get() {
        return INSTANCE;
    }

    static void reset() {
        INSTANCE = new WindowManager();
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
