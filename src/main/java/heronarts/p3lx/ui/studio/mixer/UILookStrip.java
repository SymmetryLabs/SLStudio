package heronarts.p3lx.ui.studio.mixer;

import heronarts.lx.LX;
import heronarts.lx.LXLook;
import heronarts.lx.LXChannel;
import heronarts.p3lx.ui.UI;
import java.util.List;

/**
 * A mixer strip representing an LXLook (group of channels)
 * Visually similar to a channel strip, but can contain/nest channel strips.
 */
public class UILookStrip extends heronarts.p3lx.ui.UI2dContainer {
    public final LXLook look;
    public final List<LXChannel> channels;
    private final UIMixer mixer;

    public UILookStrip(UI ui, UIMixer mixer, LX lx, LXLook look, List<LXChannel> channels) {
        super(0, 0, UIMixerStrip.WIDTH, UIMixerStrip.HEIGHT + channels.size() * UIMixerStrip.HEIGHT);
        this.look = look;
        this.channels = channels;
        this.mixer = mixer;
        // Add channel strips as children
        float y = UIMixerStrip.HEIGHT;
        for (LXChannel channel : channels) {
            UIChannelStrip channelStrip = new UIChannelStrip(ui, mixer, lx, channel);
            channelStrip.setPosition(0, y);
            this.addTopLevelComponent(channelStrip);
            y += UIMixerStrip.HEIGHT;
        }
    }

    public UIMixer getMixer() {
        return this.mixer;
    }

    @Override
    public void onDraw(UI ui, processing.core.PGraphics pg) {
        // Draw group highlight background
        pg.pushStyle();
        pg.noStroke();
        pg.fill(0xFF666666, 64); // semi-transparent gray
        pg.rect(0, 0, this.getWidth(), UIMixerStrip.HEIGHT);
        pg.popStyle();
        // Optionally draw group label, fader, etc. here
        // Draw children (channel strips)
        super.onDraw(ui, pg);
    }





}
