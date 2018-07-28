package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.CompoundParameter;

public class TreeSolid extends LXPattern {

    public final CompoundParameter h = new CompoundParameter("Hue", 0, 360);
    public final CompoundParameter s = new CompoundParameter("Sat", 0, 100);
    public final CompoundParameter b = new CompoundParameter("Brt", 100, 100);

    public TreeSolid(LX lx) {
        super(lx);
        addParameter("h", this.h);
        addParameter("s", this.s);
        addParameter("b", this.b);
    }

    public void run(double deltaMs) {
        setColors(LXColor.hsb(this.h.getValue(), this.s.getValue(), this.b.getValue()));
    }
}
