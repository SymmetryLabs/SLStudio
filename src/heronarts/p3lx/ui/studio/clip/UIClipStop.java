package heronarts.p3lx.ui.studio.clip;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXMasterChannel;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.studio.mixer.UIMixer;
import processing.event.KeyEvent;

public class UIClipStop extends UIStop {

    private final LX lx;
    private final LXBus bus;

    public UIClipStop(UI ui, UIMixer mixer, LX lx, LXBus bus) {
        super(ui, mixer, UIClipLauncher.WIDTH);
        this.lx = lx;
        this.bus = bus;
    }

    @Override
    protected void stop() {
        this.bus.stopClips();
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        super.onKeyPressed(keyEvent, keyChar, keyCode);
        if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
            if (this.bus instanceof LXMasterChannel) {
                consumeKeyEvent();
                LXChannel target = this.lx.engine.channels.get(this.lx.engine.channels.size() - 1);
                this.mixer.channelStrips.get(target).clipLauncher.stop.focus();
            } else if (this.bus instanceof LXChannel) {
                int channelIndex = ((LXChannel) this.bus).getIndex();
                if (channelIndex > 0) {
                    consumeKeyEvent();
                    this.mixer.channelStrips.get(this.lx.engine.channels.get(channelIndex - 1)).clipLauncher.stop.focus();
                }
            }
        } else if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
            if (this.bus instanceof LXMasterChannel) {
                consumeKeyEvent();
                this.mixer.sceneStrip.sceneLauncher.stop.focus();
            } else if (this.bus instanceof LXChannel) {
                consumeKeyEvent();
                int channelIndex = ((LXChannel) this.bus).getIndex();
                if (channelIndex == this.lx.engine.channels.size() - 1) {
                    this.mixer.masterStrip.clipLauncher.stop.focus();
                } else {
                    this.mixer.channelStrips.get(this.lx.engine.channels.get(channelIndex + 1)).clipLauncher.stop.focus();
                }
            }
        }
    }

}
