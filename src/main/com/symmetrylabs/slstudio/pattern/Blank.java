package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;


public class Blank extends LXPattern {
    public Blank(LX lx) {
        super(lx);
    }

    public void run(double deltaMs) {
        setColors(LXColor.BLACK);
    }
}
