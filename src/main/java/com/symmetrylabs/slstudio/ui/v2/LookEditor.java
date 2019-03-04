package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXChannel.CrossfadeGroup;

public class LookEditor {
    private final LX lx;
    private final int HEIGHT = 270;
    private final WepUi wepUi;

    public LookEditor(LX lx) {
        this.lx = lx;
        this.wepUi = new WepUi(lx, () -> UI.closePopup());
    }

    public void draw() {
        UI.setNextWindowPosition(0, UI.height, 0, 1);
        UI.setNextWindowSize(260 + 100 * lx.engine.getChannels().size(), HEIGHT);
        UI.begin(
            "Look Editor Bottom Pane",
            UI.WINDOW_NO_MOVE | UI.WINDOW_NO_RESIZE | UI.WINDOW_NO_DECORATION | UI.WINDOW_NO_DOCKING | UI.WINDOW_NO_SCROLL_WITH_MOUSE);

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
        UI.endGroup();

        for (LXChannel chan : lx.engine.getChannels()) {
            String chanName = chan.getLabel();
            UI.sameLine();
            UI.beginChild(chanName, false, 0, 90, HEIGHT);

            UI.pushFont(FontLoader.DEFAULT_FONT_L);
            UI.pushColor(UI.COLOR_HEADER, chan.editorColor.getColor());
            UI.pushColor(UI.COLOR_HEADER_ACTIVE, chan.editorColor.getColor());
            UI.pushColor(UI.COLOR_HEADER_HOVERED, chan.editorColor.getColor());
            boolean isVisible = chan.editorVisible.getValueb();
            boolean newVisible = UI.selectable(chanName, isVisible);
            if (isVisible != newVisible) {
                lx.engine.addTask(() -> chan.editorVisible.setValue(newVisible));
            }
            UI.popColor(3);
            UI.popFont();

            float fader = UI.vertSliderFloat("##fader-" + chanName, chan.fader.getValuef(), 0, 1, "", 30, 180);
            if (fader != chan.fader.getValuef()) {
                lx.engine.addTask(() -> chan.fader.setValue(fader));
            }
            UI.sameLine();

            UI.beginGroup();
            CrossfadeGroup group = chan.crossfadeGroup.getEnum();

            ParameterUI.toggle(lx, chan.enabled, false, 40);
            ParameterUI.toggle(lx, chan.cueActive, true, 40);
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
            WindowManager.runSafelyWithEngine(lx, () -> lx.engine.setFocusedChannel(lx.engine.addChannel()));
        }
        UI.popFont();

        UI.end();

        for (LXChannel chan : lx.engine.getChannels()) {
            if (chan.editorVisible.getValueb()) {
                boolean open = UI.beginClosable(chan.getLabel());
                if (!open) {
                    chan.editorVisible.setValue(false);
                } else {
                    ChannelUi.draw(lx, chan, wepUi);
                }
                UI.end();
            }
        }
    }
}
