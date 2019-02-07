package com.symmetrylabs.slstudio.ui;

public interface GraphicsAdapter {
    public void strokeWeight(float weight);
    public void stroke(int r, int g, int b);
    public void line(float x0, float y0, float z0, float x1, float y1, float z1);
}
