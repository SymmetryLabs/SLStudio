package com.symmetrylabs.util;

import processing.core.PGraphics;
import processing.core.PVector;

public class SphereWithArrow extends SphereMarker implements Marker {
    PVector arrow;
    int arrowRgb;

    public SphereWithArrow(
        PVector pos, float size, int rgb,
        boolean showX, boolean showY, boolean showZ,
        PVector arrow, int arrowRgb
    ) {
        super(pos, size, rgb, showX, showY, showZ);
        this.arrow = arrow;
        this.arrowRgb = arrowRgb;
    }

    public SphereWithArrow(
        PVector pos, float size, int rgb,
        PVector arrow, int arrowRgb
    ) {
        super(pos, size, rgb);
        this.arrow = arrow;
        this.arrowRgb = arrowRgb;
    }

    public void draw(PGraphics pg) {
        super.draw(pg);
        pg.stroke((arrowRgb >> 16) & 0xff, (arrowRgb >> 8) & 0xff, arrowRgb & 0xff);
        pg.line(pos.x, pos.y, pos.z,
            pos.x + arrow.x, pos.y + arrow.y, pos.z + arrow.z
        );
    }
}
