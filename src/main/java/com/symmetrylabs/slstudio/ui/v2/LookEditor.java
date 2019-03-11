package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXChannel.CrossfadeGroup;
import heronarts.lx.LXMasterChannel;
import heronarts.lx.color.LXColor;


public class LookEditor implements Window {
    private final LX lx;
    private final int HEIGHT = 300;
    private final int PIPELINE_WIDTH = 230;
    private final int PIPELINE_PAD = 8;
    private final int MENU_HEIGHT = 22;
    private final WepUi wepUi;
    private final WepUi transformWepUi;
    private boolean showLookTransform = false;

    static final int[] MAP_COLORS = new int[10];
    static {
        for (int i = 0; i < MAP_COLORS.length; i++) {
            MAP_COLORS[i] = LXColor.hsb(i * 360.f / MAP_COLORS.length, 50, 50);
        }
    };

    public LookEditor(LX lx) {
        this.lx = lx;
        this.wepUi = new WepUi(lx, () -> UI.closePopup());
        this.transformWepUi = new WepUi(lx, false, () -> UI.closePopup());
    }

    @Override
    public void draw() {
        UI.setNextWindowPosition(0, UI.height, 0, 1);
        float desiredWidth = 260 + 100 * lx.engine.getChannels().size();
        UI.setNextWindowSize(Float.min(desiredWidth + 22, UI.width), HEIGHT);
        UI.setNextWindowContentSize(desiredWidth, HEIGHT - 22);
        UI.begin(
            "Look Editor Bottom Pane",
            UI.WINDOW_NO_MOVE | UI.WINDOW_NO_RESIZE | UI.WINDOW_NO_TITLE_BAR | UI.WINDOW_NO_DOCKING
            | UI.WINDOW_NO_SCROLLBAR | (desiredWidth > UI.width ? UI.WINDOW_FORCE_HORIZ_SCROLL : 0));

        UI.beginGroup();
        UI.pushFont(FontLoader.DEFAULT_FONT_XL);
        UI.text("SLStudio Two");
        UI.popFont();
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 4; col++) {
                UI.knobFloat(String.format("-##shelf-knob/%d/%d", row, col), 0, 0);
                if (col != 3) UI.sameLine();
            }
        }

        for (int col = 0; col < 4; col++) {
            UI.button(String.format("-##shelf-trig/%d", col), 45, 45);
            if (col != 3) UI.sameLine();
        }

        showLookTransform = UI.checkbox("Edit look transform", showLookTransform);
        UI.endGroup();

        int visibleWindowCount = 0;
        for (LXChannel chan : lx.engine.getChannels()) {
            String chanName = chan.getLabel();
            UI.sameLine();
            UI.beginChild(chanName, false, 0, 90, HEIGHT);

            visibleWindowCount = channelHeader(chan, chanName, visibleWindowCount);

            float fader = UI.vertSliderFloat("##fader-" + chanName, chan.fader.getValuef(), 0, 1, "", 30, 180);
            if (fader != chan.fader.getValuef()) {
                lx.engine.addTask(() -> chan.fader.setValue(fader));
            }
            UI.sameLine();

            UI.beginGroup();
            CrossfadeGroup group = chan.crossfadeGroup.getEnum();

            ParameterUI.toggle(lx, chan.enabled, false, 40);
            boolean cueStart = chan.cueActive.getValueb();
            if (ParameterUI.toggle(lx, chan.cueActive, true, 40) && !cueStart) {
                for (LXChannel cc : lx.engine.getChannels()) {
                    if (cc != chan) {
                        cc.cueActive.setValue(false);
                    }
                }
            }
            boolean A = ParameterUI.toggle("A##" + chanName, group == CrossfadeGroup.A, false, 40);
            boolean B = ParameterUI.toggle("B##" + chanName, group == CrossfadeGroup.B, false, 40);

            if (A && group != CrossfadeGroup.A) {
                group = CrossfadeGroup.A;
            } else if (B && group != CrossfadeGroup.B) {
                group = CrossfadeGroup.B;
            } else if (!A && !B) {
                group = CrossfadeGroup.BYPASS;
            }
            chan.crossfadeGroup.setValue(group);
            UI.endGroup();

            UI.pushWidth(80);
            ParameterUI.draw(lx, chan.blendMode, ParameterUI.ShowLabel.NO);
            UI.popWidth();

            UI.endChild();
        }

        UI.sameLine();
        UI.pushFont(FontLoader.DEFAULT_FONT_XL);
        if (UI.button("+", 30, 230)) {
            /* running this has the potential to cause CME issues in both the UI
               and the engine, so we have to sync the world to do it. */
            WindowManager.runSafelyWithEngine(lx, () -> {
                    LXChannel chan = lx.engine.addChannel();
                    lx.engine.setFocusedChannel(chan);
                    chan.editorVisible.setValue(true);
                });
        }
        UI.popFont();

        UI.end();

        if (showLookTransform) {
            visibleWindowCount++;
        }

        if (visibleWindowCount == 0) {
            return;
        }

        UI.setNextWindowPosition(0, MENU_HEIGHT, 0, 0);
        UI.setNextWindowSize(visibleWindowCount * (PIPELINE_WIDTH + PIPELINE_PAD), UI.height - HEIGHT - MENU_HEIGHT);
        UI.begin("Pipeline Windows",
                 UI.WINDOW_NO_MOVE | UI.WINDOW_NO_RESIZE | UI.WINDOW_NO_DECORATION | UI.WINDOW_NO_DOCKING | UI.WINDOW_NO_SCROLL_WITH_MOUSE);
        int pipelineIndex = 0;
        for (LXChannel chan : lx.engine.getChannels()) {
            if (chan.editorVisible.getValueb()) {
                UI.beginChild(chan.getLabel() + "##channel-child", false, 0, PIPELINE_WIDTH, (int) UI.height);
                pipelineIndex = channelHeader(chan, chan.getLabel(), pipelineIndex);
                ChannelUI.draw(lx, chan, wepUi);
                UI.endChild();
                UI.sameLine();
            }
        }
        if (showLookTransform) {
            UI.beginChild("Look Transform##look-transform", false, 0, PIPELINE_WIDTH, (int) UI.height);

            UI.pushFont(FontLoader.DEFAULT_FONT_L);
            showLookTransform = UI.selectable("Look Transform##look-transform-header", true);
            UI.popFont();

            LXMasterChannel mc = lx.engine.masterChannel;
            ChannelUI.drawEffects(lx, "Look", mc);
            ChannelUI.drawWarps(lx, "Look", mc);
            ChannelUI.drawWepPopup(lx, mc, transformWepUi);

            UI.endChild();
        }
        UI.end();
    }

    private final int channelHeader(LXChannel chan, String chanName, int visibleWindowCount) {
        UI.pushFont(FontLoader.DEFAULT_FONT_L);
        boolean isVisible = chan.editorVisible.getValueb();
        if (isVisible) {
            int c = MAP_COLORS[visibleWindowCount % MAP_COLORS.length];
            visibleWindowCount++;
            UI.pushColor(UI.COLOR_HEADER, c);
            UI.pushColor(UI.COLOR_HEADER_ACTIVE, c);
            UI.pushColor(UI.COLOR_HEADER_HOVERED, c);
        }
        boolean newVisible = UI.selectable(chanName + "##header", isVisible);
        if (isVisible != newVisible) {
            lx.engine.addTask(() -> chan.editorVisible.setValue(newVisible));
        }
        if (isVisible) {
            UI.popColor(3);
        }
        UI.popFont();
        return visibleWindowCount;
    }
}
