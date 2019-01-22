package com.symmetrylabs.slstudio.ui.v2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WindowManager {
    private static WindowManager INSTANCE = new WindowManager();

    public static WindowManager get() {
        return INSTANCE;
    }

    public interface WindowCreator {
        Window create();
    }

    static class WindowSpec {
        String name;
        WindowCreator creator;
        Window current;
    }

    static void reset() {
        INSTANCE = new WindowManager();
    }

    private final List<WindowSpec> specs;
    private final Map<Window, WindowSpec> specWindows;

    private final List<Window> windows;
    private final List<Window> toAdd;
    private final List<Window> toRemove;
    private boolean uiEnabled;

    protected WindowManager() {
        windows = new ArrayList<>();
        toAdd = new ArrayList<>();
        toRemove = new ArrayList<>();
        specs = new ArrayList<>();
        specWindows = new HashMap<>();
        uiEnabled = true;
    }

    public void addSpec(String name, WindowCreator creator, boolean displayByDefault) {
        WindowSpec ws = new WindowSpec();
        ws.name = name;
        ws.creator = creator;
        ws.current = displayByDefault ? creator.create() : null;
        specs.add(ws);
        if (ws.current != null) {
            specWindows.put(ws.current, ws);
        }
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
        for (Window w : specWindows.keySet()) {
            w.draw();
        }
        windows.removeAll(toRemove);
        for (Window w : toRemove) {
            if (specWindows.containsKey(w)) {
                specWindows.get(w).current = null;
                specWindows.remove(w);
            }
        }
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

    Collection<WindowSpec> getSpecs() {
        return specs;
    }

    void show(WindowSpec ws) {
        if (ws.current != null) {
            return;
        }
        ws.current = ws.creator.create();
        specWindows.put(ws.current, ws);
    }

    void hide(WindowSpec ws) {
        if (ws.current == null) {
            return;
        }
        specWindows.remove(ws.current);
        ws.current = null;
    }
}
