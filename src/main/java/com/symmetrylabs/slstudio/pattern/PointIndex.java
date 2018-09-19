package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.color.LXColor;


public class PointIndex extends LXPattern {

    private DiscreteParameter indexParam;

    public PointIndex(LX lx) {
        super(lx);

        indexParam = new DiscreteParameter("index", 0, 0, lx.model.size);
    }

    public void run(double deltaMs) {
        setColors(LXColor.BLACK);
        setColor(indexParam.getValuei(), palette.getColor());
    }
}
