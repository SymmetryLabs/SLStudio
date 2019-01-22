package com.symmetrylabs.slstudio.ui.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsoleWindow extends CloseableWindow {
    static final String WINDOW_NAME = "Console";

    private static final Object GLOBAL_WARNING_LOCK = new Object();
    private static final Map<String, String> warnings = new HashMap<>();
    private static final List<String> keys = new ArrayList<>();

    ConsoleWindow() {
        super("Console");
    }

    @Override
    protected void windowSetup() {
        UI.setNextWindowDefaults(25, 500, 500, 300);
    }

    @Override
    protected void drawContents() {
        /* do not add warnings inside this block, it will cause a deadlock! */
        synchronized (GLOBAL_WARNING_LOCK) {
            for (String key : keys) {
                UI.labelText(key, warnings.get(key));
            }
        }
    }

    public static void setWarning(String key, String message) {
        synchronized (GLOBAL_WARNING_LOCK) {
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

    /** Remove all warnings from the console. */
    public static void reset() {
        synchronized (GLOBAL_WARNING_LOCK) {
            warnings.clear();
            keys.clear();
        }
    }
}
