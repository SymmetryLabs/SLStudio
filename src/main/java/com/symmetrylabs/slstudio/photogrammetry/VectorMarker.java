package com.symmetrylabs.slstudio.photogrammetry;

import com.symmetrylabs.slstudio.ui.GraphicsAdapter;
import com.symmetrylabs.util.Marker;
import processing.core.PVector;

public class VectorMarker implements Marker {
    PVector start;
    PVector end;
    float size;
    int rgb;
    static final int NUM_SEGMENTS = 24;

    public VectorMarker(PVector start, PVector end, float size, int rgb) {
        this.start = start.copy();
        this.end = end.copy();
        this.size = size;
        this.rgb = rgb;
    }

    public VectorMarker(Line3D line, float size, int rgb) {
        this.start = line.start.copy();
        this.end = line.end.copy();
        this.size = size;
        this.rgb = rgb;
    }

    @Override
    public void draw(GraphicsAdapter pg) {
        float x = start.x;
        float y = start.y;
        float z = start.z;
        float x0 = end.x;
        float y0 = end.y;
        float z0 = end.z;
        pg.strokeWeight(1);
        pg.stroke((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff);
        pg.line(x, y, z, x + x0, y + y0, z + z0);
    }
}
