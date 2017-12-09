package com.symmetrylabs.ui;

import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.MarkerSource;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import processing.core.PGraphics;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
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
