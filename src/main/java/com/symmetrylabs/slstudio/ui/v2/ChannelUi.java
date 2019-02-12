package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.warp.LXWarp;
import java.util.ArrayList;
import java.util.List;

public class ChannelUi {
    public static void draw(LX lx, LXChannel chan, WepUi wepUi) {
        String chanName = chan.getLabel();
        boolean isFocused = lx.engine.getFocusedChannel() == chan;

        int chanFlags = UI.TREE_FLAG_DEFAULT_OPEN |
            (isFocused ? UI.TREE_FLAG_SELECTED : 0);
        UI.selectable(chanName, isFocused);
        if (UI.isItemClicked()) {
            lx.engine.addTask(() -> lx.engine.setFocusedChannel(chan));
        }

        ParameterUI.draw(lx, chan.fader);
        ParameterUI.draw(lx, chan.speed);

        ParameterUI.draw(lx, chan.enabled);
        UI.sameLine();
        if (chan.cueActive.getValueb()) {
            UI.pushColor(UI.COLOR_WIDGET, UI.RED);
        }
        ParameterUI.draw(lx, chan.cueActive);
        if (chan.cueActive.getValueb()) {
            UI.popColor();
        }

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

            if (active == i) {
                UI.pushColor(UI.COLOR_HEADER, UI.BLUE);
                UI.pushColor(UI.COLOR_HEADER_ACTIVE, UI.BLUE);
                UI.pushColor(UI.COLOR_HEADER_HOVERED, UI.BLUE_HOVER);
            }
            UI.CollapseResult section = UI.collapsibleSection(patName, patterns.size() > 1);
            if (active == i) {
                UI.popColor(3);
            }
            if (UI.isItemDoubleClicked()) {
                final int patternIndex = i;
                lx.engine.addTask(() -> chan.goIndex(patternIndex));
            }
            if (UI.beginContextMenu(patName)) {
                if (UI.contextMenuItem("Activate")) {
                    final int patternIndex = i;
                    lx.engine.addTask(() -> chan.goIndex(patternIndex));
                }
                if (UI.contextMenuItem("Pop out")) {
                    WindowManager.addTransient(new ComponentWindow(lx, id, pat));
                }
                UI.endContextMenu();
            }

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

                UI.spacing();
                UI.CollapseResult section = UI.collapsibleSection(effName, true);
                if (section.shouldRemove) {
                    lx.engine.addTask(() -> chan.removeEffect(eff));
                } else if (section.isOpen) {
                    new ComponentUI(lx, eff).draw();
                }
            }
        }

        if (UI.button("+")) {
            lx.engine.setFocusedChannel(chan);
            UI.setNextWindowContentSize(300, 600);
            wepUi.resetFilter();
            UI.openPopup("Warps / effects / patterns");
        }
        if (UI.beginPopup("Warps / effects / patterns", false)) {
            wepUi.draw();
            UI.endPopup();
        }
    }
}
