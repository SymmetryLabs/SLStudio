package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.LXLook;
import heronarts.lx.LXChannel;
import heronarts.lx.parameter.CompoundParameter;
import java.util.List;

public class LookUI {
    private final LXLook look;
    private final String lookName;
    private final List<LXChannel> channels;
    private final ParameterUI pui;

    public LookUI(LXLook look, String lookName, List<LXChannel> channels, ParameterUI pui) {
        this.look = look;
        this.lookName = lookName;
        this.channels = channels;
        this.pui = pui;
    }

    public void draw(LX lx) {
        // Draw the look fader and controls (similar to ChannelUI)
        UI.spacing(5, 2);
        UI.separator();
        UI.labelText("", lookName);
        // Fader for group output (commented out, as LXLook does not have getFader())
        // CompoundParameter groupFader = look.getFader();
        // if (groupFader != null) {
        //     pui.push().preferKnobs(false).draw(groupFader).pop();
        // }
        // Draw group buttons (mute, solo, etc.) if needed
        // ... (copy from ChannelUI as appropriate)

        // Draw nested channels
        WepUI wepUi = new WepUI(lx, false, () -> {});
        for (LXChannel channel : channels) {
            ChannelUI.draw(lx, channel, pui, wepUi);
        }
    }
}
