package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;


public class Palette extends LXPattern {
    public Palette(LX lx) {
        super(lx);
    }

    public void run(double deltaMs) {
        for (LXVector v : getVectorList()) {
            colors[v.index] = palette.getColor(v);
        }
    }
}
