package com.symmetrylabs.layouts.cubes.patterns;

import com.symmetrylabs.slstudio.model.Strip;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

public class TopoTestPattern extends TopoPattern {
    public TopoTestPattern(LX lx) {
        super(lx);
    }

    @Override
    public void run(double deltaMs) {
        for (TopoEdge e : edges) {
            float h;
            switch (e.dir) {
                case X: h = 60; break;
                case Y: h = 140; break;
                case Z: h = 200; break;
                default: h = 0;
            }
            Strip s = model.getStripByIndex(e.i);
            for (LXPoint p : s.points) {
                colors[p.index] = LXColor.hsb(h, 100, 100);
            }
        }
    }
}
