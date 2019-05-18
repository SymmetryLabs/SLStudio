package com.symmetrylabs.slstudio.ui.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.LXComponent;
import com.symmetrylabs.util.CaptionSource;

public class CaptionWindow implements Window {
    static final String WINDOW_NAME = "Captions";

    private static class Warning {
        String str;
        int lines;
    }

    private final LX lx;

    CaptionWindow(LX lx) {
        this.lx = lx;
    }

    @Override
    public void draw() {
        ArrayList<String> lines = new ArrayList<String>();
        for (LXComponent comp : lx.engine.getFocusedLook().allComponents()) {
            if (comp instanceof CaptionSource) {
                CaptionSource cs = (CaptionSource) comp;
                String caption = cs.getCaption();
                if (caption != null) {
                    lines.add(String.format("%s - %s", comp.getClass().getSimpleName(), caption));
                }
            }
        }
        if (lines.isEmpty()) {
            return;
        }

        UI.setNextWindowSize(600, 20 * (lines.size() + 1));
        UI.setNextWindowPosition(UI.width - 10, UI.height - 10, 1, 1);
        UI.begin("Caption window",
                 UI.WINDOW_NO_RESIZE | UI.WINDOW_NO_MOVE | UI.WINDOW_NO_TITLE_BAR |
                 UI.WINDOW_NO_DOCKING);
        for (String line : lines) {
            UI.textWrapped(line);
        }
        UI.end();
    }
}
