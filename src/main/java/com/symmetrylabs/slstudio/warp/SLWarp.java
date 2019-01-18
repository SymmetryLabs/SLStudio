package com.symmetrylabs.slstudio.warp;

import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.MarkerSource;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.warp.LXWarp;

import java.util.ArrayList;
import java.util.Collection;

public abstract class SLWarp<M extends SLModel> extends LXWarp implements MarkerSource {
    protected SLStudioLX lx;

    public SLWarp(LX lx) {
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

    public Collection<Marker> getMarkers() {
        return new ArrayList<>();
    }
}
