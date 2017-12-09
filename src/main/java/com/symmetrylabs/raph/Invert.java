package com.symmetrylabs.raph;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;


public class Invert extends LXPattern {
    public Invert(LX lx) {
        super(lx);
    }

    public void run(double deltaMs) {
        for (LXPoint p : model.points) {
            int existing = colors[p.index];
            float h = LXColor.h(existing);
            float s = LXColor.s(existing);
            float b = LXColor.b(existing);
            colors[p.index] = LXColor.hsb(h, s, 100 - b);
        }
    }
}
