package com.symmetrylabs.util;

import processing.core.PGraphics;
import processing.core.PVector;
import com.symmetrylabs.slstudio.ui.GraphicsAdapter;

public class SphereMarker implements Marker {
    PVector pos;
    float size;
    int rgb;
    static final int NUM_SEGMENTS = 24;

    public SphereMarker(PVector pos, float size, int rgb) {
        this.pos = pos.copy();
        this.size = size;
        this.rgb = rgb;
    }

    @Override
    public void draw(GraphicsAdapter pg) {
        double TAU = Math.PI * 2;
        float x = pos.x;
        float y = pos.y;
        float z = pos.z;
        pg.strokeWeight(1);
        pg.stroke((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff);
        float p = (float) Math.sin(0) * size;
        float q = (float) Math.cos(0) * size;
        for (int i = 1; i <= NUM_SEGMENTS; i++) {
            float np = (float) Math.sin(TAU * i / NUM_SEGMENTS) * size;
            float nq = (float) Math.cos(TAU * i / NUM_SEGMENTS) * size;
            pg.line(x + p, y + q, z, x + np, y + nq, z);
            pg.line(x, y + p, z + q, x, y + np, z + nq);
            pg.line(x + q, y, z + p, x + nq, y, z + np);
            p = np;
            q = nq;
        }
    }
}
