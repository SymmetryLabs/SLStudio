package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.warp.LXWarp;
import java.util.ArrayList;
import java.util.List;
import heronarts.lx.LXChannel.CrossfadeGroup;

public class ChannelWindow extends CloseableWindow {
    private final LX lx;
    private WepUi wepUi;

    public ChannelWindow(LX lx) {
        super("Channels");
        this.lx = lx;
        this.wepUi = new WepUi(lx, () -> UI.closePopup());
    }

    @Override
    protected void windowSetup() {
        UI.setNextWindowDefaults(25, 500, 500, 800);
    }

    @Override
    protected void drawContents() {
        for (LXChannel chan : lx.engine.getChannels()) {
            String chanName = chan.getLabel();
            UI.beginChild(chanName, false, 0, 230, 0);

            UI.selectable(" " + chanName, lx.engine.getFocusedChannel() == chan, 18);
            if (UI.isItemClicked()) {
                lx.engine.addTask(() -> lx.engine.setFocusedChannel(chan));
            }
            UI.spacing();

            float fader = UI.vertSliderFloat("##fader", chan.fader.getValuef(), 0, 1, "LVL", 30, 100);
            if (fader != chan.fader.getValuef()) {
                lx.engine.addTask(() -> chan.fader.setValue(fader));
            }
            UI.sameLine();
            float speed = UI.vertSliderFloat("##speed", chan.speed.getValuef(), 0, 2, "SPD", 30, 100);
            if (speed != chan.speed.getValuef()) {
                lx.engine.addTask(() -> chan.speed.setValue(speed));
            }
            UI.sameLine();

            CrossfadeGroup group = chan.crossfadeGroup.getEnum();

            UI.beginGroup();
            UI.beginColumns(2, "cued-enabled-" + chanName);
            ParameterUI.draw(lx, chan.enabled);
            UI.nextColumn();
            boolean A = UI.checkbox("A", group == CrossfadeGroup.A);
            UI.nextColumn();
            ParameterUI.draw(lx, chan.cueActive, true);
            UI.nextColumn();
            boolean B = UI.checkbox("B", group == CrossfadeGroup.B);

            if (A && group != CrossfadeGroup.A) {
                group = CrossfadeGroup.A;
            } else if (B && group != CrossfadeGroup.B) {
                group = CrossfadeGroup.B;
            } else if (!A && !B) {
                group = CrossfadeGroup.BYPASS;
            }
            chan.crossfadeGroup.setValue(group);
            UI.endColumns();
            ParameterUI.draw(lx, chan.blendMode);
            UI.endGroup();

            UI.separator();
            ChannelUi.draw(lx, chan, wepUi);

            UI.endChild();
            UI.sameLine();
        }
        if (UI.button("+")) {
            /* running this has the potential to cause CME issues in both the UI
               and the engine, so we have to sync the world to do it. */
            WindowManager.runSafelyWithEngine(lx, () -> lx.engine.setFocusedChannel(lx.engine.addChannel()));
        }
    }
}
