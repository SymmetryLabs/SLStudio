package com.symmetrylabs.util;

import processing.core.PGraphics;
import processing.core.PVector;

public class MatrixMarker implements Marker {
    PVector pos;
    float scale;
    Matrix matrix;

    public MatrixMarker(PVector pos, float scale, Matrix matrix) {
        this.pos = pos;
        this.scale = scale;
        this.matrix = matrix;
    }

    public void draw(PGraphics pg) {
        float x = pos.x;
        float y = pos.y;
        float z = pos.z;
        for (int i = 0; i < matrix.getWidth(); i++) {
            for (int j = 0; j < matrix.getHeight(); j++) {
                drawCell(pg, x + i * scale, y + j * scale, z, scale, matrix.getColor(i, j));
            }
        }
    }

    public void drawCell(PGraphics pg, float x, float y, float z, float size, int color) {
        pg.strokeWeight(0);
        pg.fill(color);
        pg.beginShape();
        pg.vertex(x, y, z);
        pg.vertex(x + size, y, z);
        pg.vertex(x + size, y + size, z);
        pg.vertex(x, y + size, z);
        pg.endShape();
    }
}
