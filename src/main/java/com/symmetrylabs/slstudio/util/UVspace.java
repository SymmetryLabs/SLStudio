package com.symmetrylabs.slstudio.util;

import heronarts.lx.transform.LXTransform;
import processing.core.PGraphics;

public class UVspace implements Marker{
    private LXTransform transform;

    public UVspace(LXTransform transform) {
        this.transform = transform;
    }

    //draws the bounding box of the UV coordinate system
    public void draw(PGraphics pg){
        float x = 5;
        float y = 5;
        float z = 5;
        float size = 5;
        pg.strokeWeight(1);
        pg.stroke(255, 0, 0);
        pg.line(x - size, y, z, x + size, y, z);
        pg.stroke(0, 255, 0);
        pg.line(x, y - size, z, x, y + size, z);
        pg.stroke(0, 0, 255);
        pg.line(x, y, z - size, x, y, z + size);
    }
}
