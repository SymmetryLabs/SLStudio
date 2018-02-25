package com.symmetrylabs.slstudio.model;

import heronarts.lx.model.LXPoint;

import java.awt.*;

/*
A point on a 2D surface which has both world coordinates and also local coordinates.
 */
public class PanelPoint extends LXPoint{
    // the coordinates on a local panel
    int x;
    int y;

    // should probably also have a reference to the panel we belong to.
    // PanelRef ref;


    public PanelPoint(float world_x, float world_y, float world_z, int panel_x, int panel_y){
        super(world_x, world_y, world_z);
        this.x = panel_x;
        this.y = panel_y;
    }
}
