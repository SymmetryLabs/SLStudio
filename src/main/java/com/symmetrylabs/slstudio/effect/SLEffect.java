package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;

import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.util.MarkerSource;

public abstract class SLEffect extends LXEffect implements MarkerSource {
    protected final SLStudioLX lx;

    public SLEffect(LX lx) {
        super(lx);

        this.lx = (SLStudioLX)lx;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        lx.ui.addMarkerSource(this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        lx.ui.removeMarkerSource(this);
    }

    public boolean isFocused() {
        return lx.engine.getFocusedChannel() == getBus();
    }
}
