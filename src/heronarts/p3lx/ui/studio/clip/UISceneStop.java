package heronarts.p3lx.ui.studio.clip;

import heronarts.lx.LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.studio.mixer.UIMixer;
import processing.event.KeyEvent;

public class UISceneStop extends UIStop {

    private final LX lx;

    public UISceneStop(UI ui, UIMixer mixer, LX lx) {
        super(ui, mixer, UISceneLauncher.WIDTH);
        this.lx = lx;
    }

    @Override
    protected void stop() {
        this.lx.engine.stopClips();
    }

    @Override
    protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        super.onKeyPressed(keyEvent, keyChar, keyCode);
        if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
            consumeKeyEvent();
            this.mixer.masterStrip.clipLauncher.stop.focus();
        }
    }
}
