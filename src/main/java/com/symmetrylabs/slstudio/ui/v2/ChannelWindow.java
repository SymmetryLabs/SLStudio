package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.warp.LXWarp;
import java.util.ArrayList;
import java.util.List;

public class ChannelWindow extends CloseableWindow {
    private final LX lx;
    private final LXChannel chan;

    public ChannelWindow(LX lx, LXChannel chan) {
        super(chan.label.getString());
        this.lx = lx;
        this.chan = chan;
    }

    @Override
    protected void windowSetup() {
        UI.setNextWindowDefaults(25, 500, UI.DEFAULT_WIDTH, 300);
    }

    @Override
    protected void drawContents() {
        String chanName = chan.label.getString();

        ParameterUI.draw(lx, chan.fader);
        ParameterUI.draw(lx, chan.speed);

        ParameterUI.draw(lx, chan.enabled);
        UI.sameLine();
        ParameterUI.draw(lx, chan.cueActive);

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
            final LXPattern pat = patterns.get(i);
            String patName = pat.getClass().getSimpleName();
            String id = String.format("%s / %s", chanName, patName);

            UI.spacing();
            UI.CollapseResult section = UI.collapsibleSection(patName, patterns.size() > 1);
            if (section.shouldRemove) {
                lx.engine.addTask(() -> chan.removePattern(pat));
            } else if (section.isOpen) {
                new ComponentUI(lx, pat).draw();
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

        if (UI.button("+")) {
            lx.engine.setFocusedChannel(chan);
        }
    }
}
