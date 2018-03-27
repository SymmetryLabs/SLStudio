package com.symmetrylabs.slstudio.ping;

import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.pattern.base.SLModelPattern;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.MarkerSource;
import heronarts.lx.LX;

public abstract class SLModelPatternWithMarkers extends SLModelPattern implements MarkerSource {
    public SLModelPatternWithMarkers(LX lx) {
        super(lx);
    }

    @Override
    public void onActive() {
        super.onActive();
        ((SLStudioLX) lx).ui.addMarkerSource(this);
    }

    @Override
    public void onInactive() {
        super.onInactive();
        ((SLStudioLX) lx).ui.removeMarkerSource(this);
    }
}
