package com.symmetrylabs.slstudio.ui;

import processing.core.PGraphics;


public class PGraphicsAdapter implements GraphicsAdapter {
    private final PGraphics pg;

    public PGraphicsAdapter(PGraphics pg) {
        this.pg = pg;
    }

    public void strokeWeight(float weight) {
        pg.strokeWeight(weight);
    }

    public void stroke(int r, int g, int b) {
        pg.stroke(r, g, b);
    }

    public void line(float x0, float y0, float z0, float x1, float y1, float z1) {
        pg.line(x0, y0, z0, x1, y1, z1);
    }
}
