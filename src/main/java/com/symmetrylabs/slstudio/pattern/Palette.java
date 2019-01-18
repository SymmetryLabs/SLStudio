package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.transform.LXVector;


public class Palette extends LXPattern {
    public DiscreteParameter selectPalette = new DiscreteParameter("select palette", lx.NUM_PALLETS);

    public Palette(LX lx) {
        super(lx);
        addParameter(selectPalette);
    }

    public void run(double deltaMs) {
        for (LXVector v : getVectors()) {
            colors[v.index] = palette.getColor(v);
        }
    }
}
