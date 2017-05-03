package heronarts.p3lx.ui.studio.clip;

import heronarts.lx.LX;
import heronarts.p3lx.ui.UI;

public class UISceneStop extends UIStop {

    private final LX lx;

    public UISceneStop(UI ui, LX lx) {
        super(ui, UISceneLauncher.WIDTH);
        this.lx = lx;
    }

    @Override
    protected void stop() {
        this.lx.engine.stopClips();

    }
}
