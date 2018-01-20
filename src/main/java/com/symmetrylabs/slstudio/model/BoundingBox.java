package com.symmetrylabs.slstudio.model;

import processing.core.PVector;

public class BoundingBox {
    public final PVector origin;
    public final PVector size;

    public BoundingBox(float x, float y, float z, float xSize, float ySize, float zSize) {
        origin = new PVector(x, y, z);
        size = new PVector(xSize, ySize, zSize);
    }
}
