package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;


public class ColorFixed extends TreePattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    public final CompoundParameter hue = new CompoundParameter("Hue", 0, 360);

    public ColorFixed(LX lx) {
        super(lx);
        addParameter("hue", this.hue);
    }

    public void run(double deltaMs) {
        setColors(LXColor.hsb(this.hue.getValuef(), 100, 100));
    }
}
