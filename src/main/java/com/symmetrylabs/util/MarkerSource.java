package com.symmetrylabs.util;

import java.util.Collection;
import com.symmetrylabs.slstudio.ui.v2.GdxGraphicsAdapter;

public interface MarkerSource {
    Collection<Marker> getMarkers();

    /**
     * Draw 2D sprites using the SpriteBatch on the graphics adapter.
     *
     * This is part of an alternate marker API that allows for zero-allocation marker drawing.
     */
    default void drawSpriteMarkers(GdxGraphicsAdapter g) {
    }

    /**
     * Draw 3D line sprites using the ShapeRenderer on the graphics adapter.
     *
     * This is part of an alternate marker API that allows for zero-allocation marker drawing.
     *
     * @return true when the source prefers this implementation to the old
     * {@link getMarkers()} implementation. If this or any of the other draw
     * marker methods return true, {@link getMarkers()} will not be called.
     */
    default boolean drawLineMarkers(GdxGraphicsAdapter g) {
        return false;
    }
}
