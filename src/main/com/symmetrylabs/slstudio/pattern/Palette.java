package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;


public class Palette extends LXPattern {
    public Palette(LX lx) {
        super(lx);
    }

    public void run(double deltaMs) {
        for (LXPoint p : model.points) {
            colors[p.index] = palette.getColor(p);
        }
    }
}
