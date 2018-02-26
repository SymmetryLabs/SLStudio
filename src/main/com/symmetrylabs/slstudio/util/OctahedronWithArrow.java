package com.symmetrylabs.slstudio.util;

import processing.core.PGraphics;
import processing.core.PVector;

public class OctahedronWithArrow extends Octahedron implements Marker {
    PVector arrow;
    int arrowRgb;

    public OctahedronWithArrow(
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
