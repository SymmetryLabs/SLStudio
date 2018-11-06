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
    protected final SLStudioLX sllx;

    public SLWarp(LX lx) {
        super(lx);
        this.sllx = lx instanceof SLStudioLX ? (SLStudioLX) lx : null;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (sllx != null) {
            sllx.ui.addMarkerSource(this);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (sllx != null) {
            sllx.ui.removeMarkerSource(this);
        }
    }

    public Collection<Marker> getMarkers() {
        return new ArrayList<>();
    }
}
