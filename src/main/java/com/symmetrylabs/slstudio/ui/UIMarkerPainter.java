package com.symmetrylabs.slstudio.ui;

import com.symmetrylabs.slstudio.util.Marker;
import com.symmetrylabs.slstudio.util.MarkerSource;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import processing.core.PGraphics;

import java.util.HashSet;
import java.util.Set;


public class UIMarkerPainter extends UI3dComponent {
    Set<MarkerSource> sources;

    public UIMarkerPainter() {
        sources = new HashSet<MarkerSource>();
    }

    public void addSource(MarkerSource source) {
        sources.add(source);
    }

    public void removeSource(MarkerSource source) {
        sources.remove(source);
    }

    protected void onDraw(UI ui, PGraphics pg) {
        for (MarkerSource source : sources) {
            for (Marker marker : source.getMarkers()) {
                marker.draw(pg);
            }
        }
    }
}
