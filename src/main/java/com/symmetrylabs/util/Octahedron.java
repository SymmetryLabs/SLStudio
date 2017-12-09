package com.symmetrylabs.util;

import processing.core.PGraphics;
import processing.core.PVector;

public class Octahedron implements Marker {
    PVector pos;
    float size;
    int rgb;

    public Octahedron(PVector pos, float size, int rgb) {
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
                pg.line(x + d * size, y, z, x, y + e * size, z);
                pg.line(x, y + d * size, z, x, y, z + e * size);
                pg.line(x, y, z + d * size, x + e * size, y, z);
            }
        }
    }
}
