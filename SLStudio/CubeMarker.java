package com.symmetrylabs.util;

import processing.core.PGraphics;
import processing.core.PVector;

public class CubeMarker implements Marker {
    PVector pos;
    float size;
    int rgb;

    public CubeMarker(PVector pos, float size, int rgb) {
        this.pos = pos;
        this.size = size;
        this.rgb = rgb;
    }

    public void draw(PGraphics pg) {
        float x = pos.x;
        float y = pos.y;
        float z = pos.z;
        pg.strokeWeight(1);
        pg.stroke((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff);
        for (int d = -1; d < 2; d += 2) {
            for (int e = -1; e < 2; e += 2) {
                pg.line(x - size, y + d*size, z + e*size, x + size, y + d*size, z + e*size);
                pg.line(x + d*size, y - size, z + e*size, x + d*size, y + size, z + e*size);
                pg.line(x + d*size, y + e*size, z - size, x + d*size, y + e*size, z + size);
            }
        }
    }
}
