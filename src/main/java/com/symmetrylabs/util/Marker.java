package com.symmetrylabs.util;

import processing.core.PGraphics;
import com.symmetrylabs.slstudio.ui.GraphicsAdapter;
import com.symmetrylabs.slstudio.ui.v2.NotImplementedInV2Exception;

public interface Marker {
    /**
     * Draw the marker using a {@link GraphicsAdapter}.
     *
     * Implementing this method is preferable, because it allows the marker to be
     * displayed in both UIv1 and UIv2. If a {@link NotImplementedInV2Exception}
     * is raised, UIv2 will ignore and UIv1 will call the Processing method
     * instead.
     */
    default void draw(GraphicsAdapter pg) {
        throw new NotImplementedInV2Exception("marker only supports pgraphics API");
    }

    default void draw(PGraphics pg) {}
}
