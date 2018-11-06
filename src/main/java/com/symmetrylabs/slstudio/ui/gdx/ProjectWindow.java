package com.symmetrylabs.slstudio.ui.gdx;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.warp.LXWarp;
import java.util.ArrayList;
import java.util.List;

public class ProjectWindow implements Window {
    private final LX lx;

    public ProjectWindow(LX lx) {
        this.lx = lx;
    }

    @Override
    public void draw() {
        UI.setNextWindowDefaults(25, 500, UI.DEFAULT_WIDTH, 300);
        UI.begin("Project");

        boolean add = UI.button("Add");
        if (add) {
            lx.engine.addTask(() -> lx.engine.addChannel());
        }

        LXBus focused = lx.engine.getFocusedChannel();

        for (LXChannel chan : lx.engine.getChannels()) {
            String chanName = String.format("Channel %d", chan.getIndex() + 1);
            int chanFlags = UI.TREE_FLAG_DEFAULT_OPEN |
                (chan == focused ? UI.TREE_FLAG_SELECTED : 0);

            if (UI.treeNode(chanName, chanFlags)) {
                if (UI.isItemClicked(1)) {
                    lx.engine.addTask(() -> lx.engine.setFocusedChannel(chan));
                }

                float fader = UI.sliderFloat("##fader", chan.fader.getValuef(), 0, 1);
                if (fader != chan.fader.getValuef()) {
                    lx.engine.addTask(() -> chan.fader.setValue(fader));
                }

                List<LXWarp> warps = chan.getWarps();
                if (!warps.isEmpty() && UI.treeNode("Warps", UI.TREE_FLAG_DEFAULT_OPEN)) {
                    for (int i = 0; i < warps.size(); i++) {
                        LXWarp warp = warps.get(i);
                        String warpName = warp.getClass().getSimpleName();
                        String id = String.format("%s / %s", chanName, warpName);
                        int flags = UI.TREE_FLAG_LEAF |
                            (warp.enabled.getValueb() ? UI.TREE_FLAG_SELECTED : 0);

                        UI.treeNode(id, flags, warpName);
                        if (UI.isItemClicked(0)) {
                            lx.engine.addTask(() -> warp.enabled.setValue(!warp.enabled.getValueb()));
                        } else if (UI.isItemClicked(1)) {
                            WindowManager.get().add(new ComponentWindow(lx, id, warp));
                        }
                        UI.treePop();
                    }
                    UI.treePop();
                }

                if (UI.treeNode("Patterns", UI.TREE_FLAG_DEFAULT_OPEN)) {
                    int active = chan.getActivePatternIndex();
                    List<LXPattern> patterns = chan.getPatterns();
                    for (int i = 0; i < patterns.size(); i++) {
                        LXPattern pat = patterns.get(i);
                        String patName = pat.getClass().getSimpleName();
                        String id = String.format("%s / %s", chanName, patName);
                        int flags = UI.TREE_FLAG_LEAF | (i == active ? UI.TREE_FLAG_SELECTED : 0);

                        UI.treeNode(id, flags, patName);
                        if (UI.isItemClicked(0)) {
                            final int index = i;
                            lx.engine.addTask(() -> chan.goIndex(index));
                        } else if (UI.isItemClicked(1)) {
                            WindowManager.get().add(new ComponentWindow(lx, id, pat));
                        }
                        UI.treePop();
                    }
                    UI.treePop();
                }

                List<LXEffect> effects = chan.getEffects();
                if (!effects.isEmpty() && UI.treeNode("Effects", UI.TREE_FLAG_DEFAULT_OPEN)) {
                    for (int i = 0; i < effects.size(); i++) {
                        LXEffect eff = effects.get(i);
                        String effName = eff.getClass().getSimpleName();
                        String id = String.format("%s / %s", chanName, effName);
                        int flags = UI.TREE_FLAG_LEAF |
                            (eff.enabled.getValueb() ? UI.TREE_FLAG_SELECTED : 0);

                        UI.treeNode(id, flags, effName);
                        if (UI.isItemClicked(0)) {
                            lx.engine.addTask(() -> eff.enabled.setValue(!eff.enabled.getValueb()));
                        } else if (UI.isItemClicked(1)) {
                            WindowManager.get().add(new ComponentWindow(lx, id, eff));
                        }
                        UI.treePop();
                    }
                    UI.treePop();
                }

                UI.treePop();
            }
        }

        UI.end();
    }
}
