package com.symmetrylabs.patterns;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;

public class Blank extends CubesPattern {
    public Blank(LX lx) {
        super(lx);
    }

    @Override
    public void run(double deltaMs) {
        setColors(LXColor.BLACK);
    }
}
