package com.symmetrylabs.slstudio.ui.gdx;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
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

        boolean add = UI.button("Add");
        if (add) {
            lx.engine.addTask(() -> { lx.engine.addChannel(); });
        }

        LXBus focused = lx.engine.getFocusedChannel();

        for (LXChannel chan : lx.engine.getChannels()) {
            String chanName = String.format("Channel %d", chan.getIndex() + 1);
            int flags = UI.TREE_FLAG_DEFAULT_OPEN;
            if (chan == focused) {
                flags |= UI.TREE_FLAG_SELECTED;
            }
            if (UI.treeNode(chanName, flags)) {
                if (UI.isItemClicked(1)) {
                    lx.engine.setFocusedChannel(chan);
                }

                float fader = UI.sliderFloat("##fader", chan.fader.getValuef(), 0, 1);
                chan.fader.setValue(fader);

                if (UI.treeNode("Patterns", UI.TREE_FLAG_DEFAULT_OPEN)) {
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

                if (UI.treeNode("Effects", UI.TREE_FLAG_DEFAULT_OPEN)) {
                    List<LXEffect> effects = chan.getEffects();
                    for (int i = 0; i < effects.size(); i++) {
                        LXEffect eff = effects.get(i);
                        String effName = eff.getClass().getSimpleName();
                        if (effName.endsWith("Effect")) {
                            effName = effName.substring(0, effName.length() - "Effect".length());
                        }
                        String name = String.format("%s / %s", chanName, effName);
                        UI.treeNode(name,
                                                UI.TREE_FLAG_LEAF |
                                                (eff.enabled.getValueb() ? UI.TREE_FLAG_SELECTED : 0),
                                                effName);
                        if (UI.isItemClicked(0)) {
                            eff.enabled.setValue(!eff.enabled.getValueb());
                        } else if (UI.isItemClicked(1)) {
                            windows.add(new ComponentWindow(lx, name, eff));
                        }
                        UI.treePop();
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
