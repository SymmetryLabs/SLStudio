package com.symmetrylabs.slstudio.model;

import heronarts.lx.model.LXPoint;
import processing.core.PVector;


public class PanelPoint extends LXPoint {
    /**
     * The normal vector is always a unit vector.
     */
    public final PVector normal;

    public PanelPoint(float x, float y, float z, PVector normal) {
        super(x, y, z);
        this.normal = new PVector(0, 1, 0); // the default normal points up
        setNormal(normal);
    }

    public PanelPoint(double x, double y, double z, PVector normal) {
        this((float) x, (float) y, (float) z, normal);
    }

    public PanelPoint(float x, float y, float z) {
        this(x, y, z, null);
    }

    public PanelPoint(double x, double y, double z) {
        this(x, y, z, null);
    }

    public void setNormal(PVector normal) {
        if (normal != null && normal.mag() > 0) {
            this.normal.set(normal.x, normal.y, normal.z);
            this.normal.normalize();
        }
    }
}

