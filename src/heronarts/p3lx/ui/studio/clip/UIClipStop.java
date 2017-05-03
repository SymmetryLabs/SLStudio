package heronarts.p3lx.ui.studio.clip;

import heronarts.lx.LXBus;
import heronarts.lx.clip.LXClip;
import heronarts.p3lx.ui.UI;

public class UIClipStop extends UIStop {

    private final LXBus bus;

    public UIClipStop(UI ui, LXBus bus) {
        super(ui, UIClipLauncher.WIDTH);
        this.bus = bus;
    }

    @Override
    protected void stop() {
        for (LXClip clip : this.bus.clips) {
            if (clip != null) {
                clip.stop();
            }
        }
    }

}
