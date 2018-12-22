package com.symmetrylabs.slstudio.ui;

import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.MarkerSource;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import processing.core.PGraphics;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

public class UIMarkerPainter extends UI3dComponent {
    WeakHashMap<MarkerSource, Object> sources = new WeakHashMap<>();

    public synchronized void addSource(MarkerSource source) {
        sources.put(source, this);
    }

    public synchronized void removeSource(MarkerSource source) {
        sources.remove(source);
    }

    protected synchronized void onDraw(UI ui, PGraphics pg) {
        for (MarkerSource source : sources.keySet()) {
            if (source.isFocused()) {
                for (Marker marker : source.getMarkers()) {
                    marker.draw(pg);
                }
            }
        }
    }
}
