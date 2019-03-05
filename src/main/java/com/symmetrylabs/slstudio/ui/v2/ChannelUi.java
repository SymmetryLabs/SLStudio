package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import static heronarts.lx.LXChannel.CrossfadeGroup;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.warp.LXWarp;
import java.util.ArrayList;
import java.util.List;

public class ChannelUi {
    public static void drawWarps(LX lx, String chanName, LXBus chan) {
        List<LXWarp> warps = chan.getWarps();
        if (!warps.isEmpty()) {
            for (int i = 0; i < warps.size(); i++) {
                LXWarp warp = warps.get(i);
                String warpName = String.format("%s##%s/warp/%d", warp.getClass().getSimpleName(), chanName, i);

                UI.spacing();
                if (warp.enabled.getValueb()) {
                    UI.pushColor(UI.COLOR_HEADER, UI.BLUE);
                    UI.pushColor(UI.COLOR_HEADER_ACTIVE, UI.BLUE);
                    UI.pushColor(UI.COLOR_HEADER_HOVERED, UI.BLUE_HOVER);
                }
                UI.CollapseResult section = UI.collapsibleSection(warpName, true, UI.TREE_FLAG_DEFAULT_OPEN);
                if (warp.enabled.getValueb()) {
                    UI.popColor(3);
                }
                if (UI.isItemClicked() && UI.isAltDown()) {
                    warp.enabled.toggle();
                } else if (section.shouldRemove) {
                    lx.engine.addTask(() -> chan.removeWarp(warp));
                } else if (section.isOpen) {
                    new ComponentUI(lx, warp).draw();
                }
            }
            UI.spacing();
        }
    }

    public static void drawEffects(LX lx, String chanName, LXBus chan) {
        List<LXEffect> effects = chan.getEffects();
        if (!effects.isEmpty()) {
            for (int i = 0; i < effects.size(); i++) {
                LXEffect eff = effects.get(i);
                String effName = String.format("%s##%s/effect/%d", eff.getClass().getSimpleName(), chanName, i);

                UI.spacing();
                if (eff.enabled.getValueb()) {
                    UI.pushColor(UI.COLOR_HEADER, UI.BLUE);
                    UI.pushColor(UI.COLOR_HEADER_ACTIVE, UI.BLUE);
                    UI.pushColor(UI.COLOR_HEADER_HOVERED, UI.BLUE_HOVER);
                }
                UI.CollapseResult section = UI.collapsibleSection(effName, true, UI.TREE_FLAG_DEFAULT_OPEN);
                if (eff.enabled.getValueb()) {
                    UI.popColor(3);
                }
                if (UI.isItemClicked() && UI.isAltDown()) {
                    eff.enabled.toggle();
                } else if (section.shouldRemove) {
                    lx.engine.addTask(() -> chan.removeEffect(eff));
                } else if (section.isOpen) {
                    new ComponentUI(lx, eff).draw();
                }
            }
        }
    }

    public static void drawWepPopup(LX lx, LXBus bus, WepUi wepUi) {
        UI.spacing();
        if (UI.button("+")) {
            lx.engine.setFocusedChannel(bus);
            UI.setNextWindowContentSize(300, 600);
            wepUi.resetFilter();
            UI.openPopup("Warps / effects / patterns");
        }
        if (UI.beginPopup("Warps / effects / patterns", false)) {
            wepUi.draw();
            UI.endPopup();
        }
    }

    public static void draw(LX lx, LXChannel chan, WepUi wepUi) {
        String chanName = chan.getLabel();

        drawWarps(lx, chanName, chan);

        int active = chan.getActivePatternIndex();
        List<LXPattern> patterns = chan.getPatterns();
        for (int i = 0; i < patterns.size(); i++) {
            final LXPattern pat = patterns.get(i);
            String patName = pat.getClass().getSimpleName();
            String id = String.format("%s / %s", chanName, patName);

            UI.spacing();

            boolean isActive = active == i;
            boolean isMidiFocused = pat.controlSurfaceSemaphore.getValue() > 0;
            if (isActive && isMidiFocused) {
                UI.pushColor(UI.COLOR_HEADER, UI.PURPLE);
                UI.pushColor(UI.COLOR_HEADER_ACTIVE, UI.PURPLE);
                UI.pushColor(UI.COLOR_HEADER_HOVERED, UI.PURPLE_HOVER);
            } else if (isMidiFocused) {
                UI.pushColor(UI.COLOR_HEADER, UI.RED);
                UI.pushColor(UI.COLOR_HEADER_ACTIVE, UI.RED);
                UI.pushColor(UI.COLOR_HEADER_HOVERED, UI.RED_HOVER);
            } else if (isActive) {
                UI.pushColor(UI.COLOR_HEADER, UI.BLUE);
                UI.pushColor(UI.COLOR_HEADER_ACTIVE, UI.BLUE);
                UI.pushColor(UI.COLOR_HEADER_HOVERED, UI.BLUE_HOVER);
            }
            UI.CollapseResult section = UI.collapsibleSection(patName + "##pattern-" + i, true, UI.TREE_FLAG_DEFAULT_OPEN);
            if (isActive || isMidiFocused) {
                UI.popColor(3);
            }
            if (UI.isItemClicked() && UI.isAltDown()) {
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

        UI.spacing();
        drawEffects(lx, chanName, chan);
        drawWepPopup(lx, chan, wepUi);
    }
}
