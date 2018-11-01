package com.symmetrylabs.slstudio.ui.gdx;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXPattern;
import java.util.ArrayList;
import java.util.List;

public class ChannelWindow {
    private final LX lx;
    private final List<ComponentWindow> windows;

    public ChannelWindow(LX lx) {
        this.lx = lx;
        windows = new ArrayList<>();
    }

    public void draw() {
        UI.begin("Channels");

        for (LXChannel chan : lx.engine.getChannels()) {
            String chanName = String.format("Channel %d", chan.getIndex() + 1);
            if (UI.treeNode(chanName, UI.TREE_FLAG_DEFAULT_OPEN)) {
                int active = chan.getActivePatternIndex();
                List<LXPattern> patterns = chan.getPatterns();
                for (int i = 0; i < patterns.size(); i++) {
                    LXPattern pat = patterns.get(i);
                    String patName = pat.getClass().getSimpleName();
                    if (patName.endsWith("Pattern")) {
                        patName = patName.substring(0, patName.length() - "Pattern".length());
                    }
                    String name = String.format("%s / %s", chanName, patName);
                    UI.treeNode(name,
                                            UI.TREE_FLAG_LEAF | (i == active ? UI.TREE_FLAG_SELECTED : 0),
                                            patName);
                    if (UI.isItemClicked(0)) {
                        chan.goIndex(i);
                    } else if (UI.isItemClicked(1)) {
                        windows.add(new ComponentWindow(lx, name, pat));
                    }
                    UI.treePop();
                }
                UI.treePop();
            }
        }

        UI.end();

        for (ComponentWindow w : windows) {
            w.draw();
        }
    }
}
