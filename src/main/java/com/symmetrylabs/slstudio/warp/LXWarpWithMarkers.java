package com.symmetrylabs.slstudio.warp;

import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.util.MarkerSource;

import heronarts.lx.LX;
import heronarts.lx.warp.LXWarp;

public abstract class LXWarpWithMarkers extends LXWarp implements MarkerSource {
    protected SLStudioLX lx;

    public LXWarpWithMarkers(LX lx) {
        super(lx);
        this.lx = (SLStudioLX) lx;
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
