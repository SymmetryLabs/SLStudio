package com.symmetrylabs.slstudio.model;

import heronarts.lx.model.LXPoint;
import processing.core.PVector;


public class PanelPoint extends LXPointNormal {
    /**
     * The normal vector is always a unit vector.
     */
    public int panel_x;
    public int panel_y;

    public PanelPoint(float x, float y, float z, int panel_x, int panel_y) {
        super(x, y, z);
        this.panel_x = panel_x;
        this.panel_y = panel_y;
    }

    public int getPanel_x(){
        return this.panel_x;
    }

    public int getPanel_y() {
        return this.panel_y;
    }
}

