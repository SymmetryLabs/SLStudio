package com.symmetrylabs.slstudio.ui.v2;

import com.symmetrylabs.slstudio.ui.WEPGrouping;
import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.warp.LXWarp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import com.symmetrylabs.slstudio.ApplicationState;

/**
 * A window that shows a tree view of warps, effects and patterns.
 */
public class WepWindow extends CloseableWindow {
    private final WepUi ui;
    private boolean firstDraw = true;

    public WepWindow(LX lx) {
        super("Warps / effects / patterns");
        ui = new WepUi(lx, null);
    }

    @Override
    protected void windowSetup() {
        UI.setNextWindowDefaults(25, 25, UIConstants.DEFAULT_WINDOW_WIDTH, 450);
    }

    @Override
    protected void drawContents() {
        ui.draw(firstDraw);
        firstDraw = false;
    }
}
