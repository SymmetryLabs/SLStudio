package com.symmetrylabs.util;

import java.util.ArrayList;
import java.util.Collection;

public interface MarkerSource {
    /** Gets all markers. */
    default Collection<Marker> getMarkers() {
        return new ArrayList<>();
    }

    /** Gets just the markers to show while this item is not focused. */
    default Collection<Marker> getMajorMarkers() {
        return new ArrayList<>();
    }

    /** Gets whether this item is focused. */
    default boolean isFocused() { return false; }
}
