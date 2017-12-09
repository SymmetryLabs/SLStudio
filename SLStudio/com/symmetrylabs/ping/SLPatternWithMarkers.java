package com.symmetrylabs.ping;

import com.symmetrylabs.p3lx.LXStudio;
import com.symmetrylabs.pattern.SLPattern;
import com.symmetrylabs.util.MarkerSource;
import heronarts.lx.LX;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public abstract class SLPatternWithMarkers extends SLPattern implements MarkerSource {
    public SLPatternWithMarkers(LX lx) {
        super(lx);
    }

    @Override
    public void onActive() {
        super.onActive();
        ((LXStudio) lx).ui.addMarkerSource(this);
    }

    @Override
    public void onInactive() {
        super.onInactive();
        ((LXStudio) lx).ui.removeMarkerSource(this);
    }
}
