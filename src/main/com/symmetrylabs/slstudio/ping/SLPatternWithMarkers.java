package com.symmetrylabs.slstudio.ping;

import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.pattern.NissanPattern;
import com.symmetrylabs.slstudio.pattern.SunsPattern;
import com.symmetrylabs.slstudio.util.MarkerSource;
import heronarts.lx.LX;


public abstract class SLPatternWithMarkers extends NissanPattern implements MarkerSource {
    public SLPatternWithMarkers(LX lx) {
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
//public abstract class SLPatternWithMarkers extends SunsPattern implements MarkerSource {
//    public SLPatternWithMarkers(LX lx) {
//        super(lx);
//    }
//
//    @Override
//    public void onActive() {
//        super.onActive();
//        ((SLStudioLX) lx).ui.addMarkerSource(this);
//    }
//
//    @Override
//    public void onInactive() {
//        super.onInactive();
//        ((SLStudioLX) lx).ui.removeMarkerSource(this);
//    }
//}
