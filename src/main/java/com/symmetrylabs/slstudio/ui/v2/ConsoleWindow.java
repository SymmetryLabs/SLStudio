package com.symmetrylabs.slstudio.ui.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsoleWindow extends CloseableWindow {
    static final String WINDOW_NAME = "Console";

    private static class Warning {
        String str;
        int lines;
    }

    private static final Object GLOBAL_WARNING_LOCK = new Object();
    private static final Map<String, Warning> warnings = new HashMap<>();
    private static final List<String> keys = new ArrayList<>();

    ConsoleWindow() {
        super("Console");
    }

    @Override
    protected void windowSetup() {
        UI.setNextWindowDefaults(25, 500, 700, 300);
    }

    @Override
    protected void drawContents() {
        /* do not add warnings inside this block, it will cause a deadlock! */
        synchronized (GLOBAL_WARNING_LOCK) {
            UI.beginTable(2, "warnings");
            UI.setColumnWidth(0, 150);
            for (String key : keys) {
                UI.text(key);
                UI.nextCell();
                Warning warning = warnings.get(key);
                UI.inputTextMultiline("##" + key, warning.str, warning.lines + 1, UI.INPUT_TEXT_FLAG_READ_ONLY);
                UI.nextCell();
            }
            UI.endTable();
        }
    }

    public static void setWarning(String key, String message) {
        synchronized (GLOBAL_WARNING_LOCK) {
            if (message != null && !message.isEmpty()) {
                Warning existing = warnings.get(key);
                if (existing == null || !message.equals(existing.str)) {
                    System.err.println("WARNING: " + key + ": " + message);
                }
                Warning w = new Warning();
                w.str = message;
                w.lines = 1;
                for (int i = 0; i < message.length(); i++) {
                    if (message.charAt(i) == '\n') w.lines++;
                }
                warnings.put(key, w);
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
