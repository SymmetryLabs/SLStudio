package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import static heronarts.lx.LXChannel.CrossfadeGroup;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.mutation.Mutations;
import heronarts.lx.warp.LXWarp;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.glfw.GLFW;

public class ChannelUI {
    public static void drawWarps(LX lx, String chanName, LXBus chan, ParameterUI pui) {
        List<LXWarp> warps = chan.getWarps();
        if (!warps.isEmpty()) {
            for (int i = 0; i < warps.size(); i++) {
                LXWarp warp = warps.get(i);
                String warpName = String.format("%s##%s/warp/%d", warp.getClass().getSimpleName(), chanName, i);

                UI.spacing(5, 5);
                if (warp.enabled.getValueb()) {
                    UI.pushColor(UI.COLOR_HEADER, UIConstants.BLUE);
                    UI.pushColor(UI.COLOR_HEADER_ACTIVE, UIConstants.BLUE);
                    UI.pushColor(UI.COLOR_HEADER_HOVERED, UIConstants.BLUE_HOVER);
                }
                UI.CollapseResult section = UI.collapsibleSection(
                    warpName, true,
                    warp.expandedInUi.getValueb() ? UI.TREE_FLAG_DEFAULT_OPEN : 0);
                if (warp.enabled.getValueb()) {
                    UI.popColor(3);
                }
                warp.expandedInUi.setValue(section.isOpen);
                if (UI.beginContextMenu(warpName)) {
                    pui.push().preferKnobs(false).draw(warp.enabled).pop();
                    if (UI.contextMenuItem("Pop out")) {
                        WindowManager.addTransient(new ComponentWindow(lx, warpName, warp, pui));
                    }
                    UI.endContextMenu();
                }
                if (UI.isItemClicked() && UI.isAltDown()) {
                    warp.enabled.toggle();
                } else if (section.shouldRemove) {
                    lx.engine.mutations.enqueue(
                        Mutations.RemoveWarp.newBuilder()
                        .setLook(lx.engine.getFocusedLook().getIndex())
                        .setChannel(chan instanceof LXChannel ? ((LXChannel) chan).getIndex() : -1)
                        .setWarp(i));
                } else if (section.isOpen) {
                    new ComponentUI(lx, warp, pui).draw();
                }
            }
            UI.spacing(5, 5);
        }
    }

    public static void drawEffects(LX lx, String chanName, LXBus chan, ParameterUI pui) {
        List<LXEffect> effects = chan.getEffects();
        if (!effects.isEmpty()) {
            for (int i = 0; i < effects.size(); i++) {
                LXEffect eff = effects.get(i);
                String effName = String.format("%s##%s/effect/%d", eff.getClass().getSimpleName(), chanName, i);

                UI.spacing(5, 5);
                if (eff.enabled.getValueb()) {
                    UI.pushColor(UI.COLOR_HEADER, UIConstants.BLUE);
                    UI.pushColor(UI.COLOR_HEADER_ACTIVE, UIConstants.BLUE);
                    UI.pushColor(UI.COLOR_HEADER_HOVERED, UIConstants.BLUE_HOVER);
                }
                UI.CollapseResult section = UI.collapsibleSection(
                    effName, true,
                    eff.expandedInUi.getValueb() ? UI.TREE_FLAG_DEFAULT_OPEN : 0);
                if (eff.enabled.getValueb()) {
                    UI.popColor(3);
                }
                eff.expandedInUi.setValue(section.isOpen);
                if (UI.beginContextMenu(effName)) {
                    pui.push().preferKnobs(false).draw(eff.enabled).pop();
                    if (UI.contextMenuItem("Pop out")) {
                        WindowManager.addTransient(new ComponentWindow(lx, effName, eff, pui));
                    }
                    UI.endContextMenu();
                }
                if (UI.isItemClicked() && UI.isAltDown()) {
                    eff.enabled.toggle();
                } else if (section.shouldRemove) {
                    lx.engine.mutations.enqueue(
                        Mutations.RemoveEffect.newBuilder()
                            .setLook(lx.engine.getFocusedLook().getIndex())
                            .setChannel(chan instanceof LXChannel ? ((LXChannel) chan).getIndex() : -1)
                            .setEffect(i));
                } else if (section.isOpen) {
                    new ComponentUI(lx, eff, pui).draw();
                }
            }
        }
    }

    public static void drawWepPopup(LX lx, LXBus bus, WepUI wepUi) {
        UI.spacing(5, 5);
        boolean shouldShow = false;
        if (UI.button("+")) {
            shouldShow = true;
        }
        if ((UI.isWindowFocused(UI.FOCUSED_FLAG_CHILD_WINDOWS) || !UI.wantCaptureKeyboard()) &&
            lx.engine.getFocusedLook().getFocusedChannel() == bus && UI.isKeyPressed(GLFW.GLFW_KEY_TAB)) {
            shouldShow = true;
        }
        if (shouldShow) {
            lx.engine.getFocusedLook().setFocusedChannel(bus);
            UI.setNextWindowContentSize(300, 600);
            wepUi.resetFilter();
            UI.openPopup("Warps / effects / patterns");
        }
        if (UI.beginPopup("Warps / effects / patterns", false)) {
            wepUi.draw(UI.isWindowAppearing());
            if (UI.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
                UI.closePopup();
            }
            UI.endPopup();
        }
    }

    public static void draw(LX lx, LXChannel chan, ParameterUI pui, WepUI wepUi) {
        String chanName = chan.getLabel();

        pui.push().allowMapping(true);

        pui.push().preferKnobs(false);
        pui.draw(chan.blendPatterns);
        pui.draw(chan.midiMonitor);
        pui.draw(chan.patternBlendMode);
        pui.draw(chan.midiChannel);
        pui.draw(chan.speed);
        pui.pop();

        drawWarps(lx, chanName, chan, pui);

        boolean blendPatternsOn = chan.blendPatterns.isOn();
        int active = chan.getActivePatternIndex();

        List<LXPattern> patterns = chan.getPatterns();
        for (int i = 0; i < patterns.size(); i++) {
            final LXPattern pat = patterns.get(i);
            String patName = pat.getClass().getSimpleName() + "##pattern-" + i;
            String id = String.format("%s / %s", chanName, patName);

            UI.spacing(5, 5);

            boolean isActive;
            if (blendPatternsOn) {
                isActive = pat.enabled.isOn();
            } else {
                isActive = active == i;
            }
            boolean isMidiFocused = pat.controlSurfaceSemaphore.getValue() > 0;

            if (isActive && isMidiFocused) {
                UI.pushColor(UI.COLOR_HEADER, UIConstants.PURPLE);
                UI.pushColor(UI.COLOR_HEADER_ACTIVE, UIConstants.PURPLE);
                UI.pushColor(UI.COLOR_HEADER_HOVERED, UIConstants.PURPLE_HOVER);
            } else if (isMidiFocused) {
                UI.pushColor(UI.COLOR_HEADER, UIConstants.RED);
                UI.pushColor(UI.COLOR_HEADER_ACTIVE, UIConstants.RED);
                UI.pushColor(UI.COLOR_HEADER_HOVERED, UIConstants.RED_HOVER);
            } else if (isActive) {
                UI.pushColor(UI.COLOR_HEADER, UIConstants.BLUE);
                UI.pushColor(UI.COLOR_HEADER_ACTIVE, UIConstants.BLUE);
                UI.pushColor(UI.COLOR_HEADER_HOVERED, UIConstants.BLUE_HOVER);
            }

            UI.CollapseResult section = UI.collapsibleSection(
                patName, true,
                pat.expandedInUi.getValueb() ? UI.TREE_FLAG_DEFAULT_OPEN : 0);
            if (isActive || isMidiFocused) {
                UI.popColor(3);
            }
            pat.expandedInUi.setValue(section.isOpen);

            if (UI.isItemClicked() && UI.isAltDown()) {
                if (blendPatternsOn) {
                    lx.engine.addTask(() -> pat.enabled.toggle());
                } else {
                    final int patternIndex = i;
                    lx.engine.addTask(() -> chan.goIndex(patternIndex));
                }
            }
            if (UI.beginContextMenu(patName)) {
                if (blendPatternsOn) {
                    pui.push().preferKnobs(false).draw(pat.enabled).pop();
                } else {
                    if (UI.contextMenuItem("Activate")) {
                        final int patternIndex = i;
                        lx.engine.addTask(() -> chan.goIndex(patternIndex));
                    }
                }
                if (UI.contextMenuItem("Pop out")) {
                    WindowManager.addTransient(new ComponentWindow(lx, id, pat, pui));
                }
                UI.endContextMenu();
            }

            if (section.shouldRemove) {
                lx.engine.mutations.enqueue(
                    Mutations.RemovePattern.newBuilder()
                        .setLook(lx.engine.getFocusedLook().getIndex())
                        .setChannel(chan.getIndex())
                        .setPattern(i));
            } else if (section.isOpen) {
                new ComponentUI(lx, pat, pui).draw();
            }
        }

        UI.spacing(5, 5);
        drawEffects(lx, chanName, chan, pui);
        drawWepPopup(lx, chan, wepUi);
        pui.pop();
    }
}
