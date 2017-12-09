package com.symmetrylabs.util;

import processing.core.PGraphics;
import processing.core.PVector;

public class CubeMarker implements com.symmetrylabs.util.Marker {
    PVector pos;
    PVector size;
    int rgb;

    public CubeMarker(PVector pos, PVector size, int rgb) {
        this.pos = pos;
        this.size = size;
        this.rgb = rgb;
    }

    public CubeMarker(PVector pos, float size, int rgb) {
        this(pos, new PVector(size, size, size), rgb);
    }

    public void draw(PGraphics pg) {
        float x = pos.x;
        float y = pos.y;
        float z = pos.z;
        pg.strokeWeight(1);
        pg.stroke((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff);
        for (int d = -1; d < 2; d += 2) {
            for (int e = -1; e < 2; e += 2) {
                pg.line(x - size.x, y + d * size.y, z + e * size.z, x + size.x, y + d * size.y, z + e * size.z);
                pg.line(x + d * size.x, y - size.y, z + e * size.z, x + d * size.x, y + size.y, z + e * size.z);
                pg.line(x + d * size.x, y + e * size.y, z - size.z, x + d * size.x, y + e * size.y, z + size.z);
            }
        }
    }
}
