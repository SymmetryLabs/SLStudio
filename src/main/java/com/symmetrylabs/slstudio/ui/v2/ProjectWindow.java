package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.warp.LXWarp;
import java.util.ArrayList;
import java.util.List;

public class ProjectWindow extends CloseableWindow {
    private final LX lx;

    public ProjectWindow(LX lx) {
        super("Project");
        this.lx = lx;
    }

    @Override
    protected void windowSetup() {
        UI.setNextWindowDefaults(25, 500, UI.DEFAULT_WIDTH, 300);
    }

    @Override
    protected void drawContents() {
        boolean add = UI.button("Add");
        if (add) {
            lx.engine.addTask(() -> {
                    lx.engine.setFocusedChannel(lx.engine.addChannel());
                });
        }

        List<LXChannel> chans = lx.engine.getChannels();
        UI.setNextWindowContentSize(180 * chans.size(), 0);
        UI.beginChild("channels", false, UI.WINDOW_HORIZ_SCROLL);
        UI.columnsStart(chans.isEmpty() ? 1 : chans.size(), "channels");
        LXBus focused = lx.engine.getFocusedChannel();
        for (LXChannel chan : chans) {
            drawChannel(chan, chan == focused);
            UI.nextColumn();
        }
        UI.columnsEnd();
        UI.endChild();
    }

    private void drawChannel(LXChannel chan, boolean isFocused) {
        String chanName = chan.label.getString();
        int chanFlags = UI.TREE_FLAG_DEFAULT_OPEN |
            (isFocused ? UI.TREE_FLAG_SELECTED : 0);

        UI.selectable(chanName, isFocused);
        if (UI.isItemClicked()) {
            lx.engine.addTask(() -> lx.engine.setFocusedChannel(chan));
        }

        UI.beginGroup();
        float fader = UI.sliderFloat(
            String.format("##fader-%d", chan.getIndex()),
            chan.fader.getValuef(), 0, 1, true);
        if (fader != chan.fader.getValuef()) {
            lx.engine.addTask(() -> chan.fader.setValue(fader));
        }
        UI.endGroup();

        UI.sameLine();

        UI.beginGroup();
        List<LXWarp> warps = chan.getWarps();
        if (!warps.isEmpty()) {
            for (int i = 0; i < warps.size(); i++) {
                LXWarp warp = warps.get(i);
                String warpName = String.format(
                    "%s##%d/warp/%d", warp.getClass().getSimpleName(), chan.getIndex(), i);
                String id = String.format("%s / %s", chanName, warpName);

                UI.selectable(warpName, warp.enabled.getValueb());
                if (UI.isItemClicked(0)) {
                    lx.engine.addTask(() -> warp.enabled.setValue(!warp.enabled.getValueb()));
                } else if (UI.isItemClicked(1)) {
                    WindowManager.addTransient(new ComponentWindow(lx, id, warp));
                }
            }
            UI.spacing();
        }

        int active = chan.getActivePatternIndex();
        List<LXPattern> patterns = chan.getPatterns();
        for (int i = 0; i < patterns.size(); i++) {
            LXPattern pat = patterns.get(i);
            String patName = pat.getClass().getSimpleName();
            String id = String.format("%s / %s", chanName, patName);
            int flags = UI.TREE_FLAG_LEAF | (i == active ? UI.TREE_FLAG_SELECTED : 0);

            UI.selectable(patName, i == active);
            if (UI.isItemClicked(0)) {
                final int index = i;
                lx.engine.addTask(() -> chan.goIndex(index));
            } else if (UI.isItemClicked(1)) {
                WindowManager.addTransient(new ComponentWindow(lx, id, pat));
            }
        }

        List<LXEffect> effects = chan.getEffects();
        if (!effects.isEmpty()) {
            UI.spacing();
            for (int i = 0; i < effects.size(); i++) {
                LXEffect eff = effects.get(i);
                String effName = eff.getClass().getSimpleName();
                String id = String.format("%s / %s", chanName, effName);
                int flags = UI.TREE_FLAG_LEAF |
                    (eff.enabled.getValueb() ? UI.TREE_FLAG_SELECTED : 0);

                UI.selectable(effName, eff.enabled.getValueb());
                if (UI.isItemClicked(0)) {
                    lx.engine.addTask(() -> eff.enabled.setValue(!eff.enabled.getValueb()));
                } else if (UI.isItemClicked(1)) {
                    WindowManager.addTransient(new ComponentWindow(lx, id, eff));
                }
            }
        }
        UI.endGroup();

        final boolean enabled = UI.checkbox(
            String.format("ENABLED##enabled-%d", chan.getIndex()), chan.enabled.getValueb());
        if (enabled != chan.enabled.getValueb()) {
            lx.engine.addTask(() -> chan.enabled.setValue(enabled));
        }
        UI.sameLine();
        final boolean cued = UI.checkbox(
            String.format("CUE##cue-%d", chan.getIndex()), chan.cueActive.getValueb());
        if (cued != chan.cueActive.getValueb()) {
            lx.engine.addTask(() -> chan.cueActive.setValue(cued));
        }
    }
}
