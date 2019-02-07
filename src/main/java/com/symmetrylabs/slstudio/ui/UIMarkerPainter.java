package com.symmetrylabs.slstudio.ui;

import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.MarkerSource;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import processing.core.PGraphics;
import com.symmetrylabs.slstudio.ui.v2.NotImplementedInV2Exception;

import java.util.HashSet;
import java.util.Set;


public class UIMarkerPainter extends UI3dComponent {
    Set<MarkerSource> sources = new HashSet<>();

    public synchronized void addSource(MarkerSource source) {
        sources.add(source);
    }

    public synchronized void removeSource(MarkerSource source) {
        sources.remove(source);
    }

    protected synchronized void onDraw(UI ui, PGraphics pg) {
        PGraphicsAdapter graphicsAdapter = new PGraphicsAdapter(pg);
        for (MarkerSource source : sources) {
            for (Marker marker : source.getMarkers()) {
                /* prefer using the method that takes a graphics adapter, but fall
                   back to PGraphics method. */
                try {
                    marker.draw(graphicsAdapter);
                } catch (NotImplementedInV2Exception e) {
                    marker.draw(pg);
                }
            }
        }
    }
}
