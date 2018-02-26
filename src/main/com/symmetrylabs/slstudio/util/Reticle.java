package com.symmetrylabs.slstudio.util;

import processing.core.PGraphics;
import processing.core.PVector;

public class Reticle implements Marker {
    PVector pos;
    float size;

    public Reticle(PVector pos, float size) {
        this.pos = pos;
        this.size = size;
    }

    public void draw(PGraphics pg) {
        float x = pos.x;
        float y = pos.y;
        float z = pos.z;
        pg.strokeWeight(1);
        pg.stroke(255, 0, 0);
        pg.line(x - size, y, z, x + size, y, z);
        pg.stroke(0, 255, 0);
        pg.line(x, y - size, z, x, y + size, z);
        pg.stroke(0, 0, 255);
        pg.line(x, y, z - size, x, y, z + size);
    }
}
