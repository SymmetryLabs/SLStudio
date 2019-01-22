package com.symmetrylabs.slstudio.ui.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsoleWindow extends CloseableWindow {
    private static final Map<String, String> warnings = new HashMap<>();
    private static final List<String> keys = new ArrayList<>();

    ConsoleWindow() {
        super("Console");
    }

    @Override
    protected void windowSetup() {
        UI.setNextWindowDefaults(25, 500, UI.DEFAULT_WIDTH, 300);
    }

    @Override
    protected void drawContents() {
        /* do not add warnings inside this block, it will cause a deadlock! */
        synchronized (ConsoleWindow.class) {
            UI.columnsStart(2, "ConsoleWindow.Warnings");
            for (String key : keys) {
                UI.text(key);
                UI.nextColumn();
                UI.text(warnings.get(key));
                UI.nextColumn();
            }
            UI.columnsEnd();
        }
    }

    public void setWarning(String key, String message) {
        synchronized (ConsoleWindow.class) {
            if (message != null && !message.isEmpty()) {
                warnings.put(key, message);
            } else {
                warnings.remove(key);
            }
            keys.clear();
            keys.addAll(warnings.keySet());
            keys.sort(String::compareToIgnoreCase);
        }
    }
}
