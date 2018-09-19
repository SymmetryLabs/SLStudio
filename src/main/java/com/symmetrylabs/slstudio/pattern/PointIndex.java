package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.color.LXColor;


public class PointIndex extends LXPattern {

    private CompoundParameter indexParam;

    public PointIndex(LX lx) {
        super(lx);

        addParameter(indexParam = new CompoundParameter("index", 0, 0, lx.model.size - 1));
    }

    public void run(double deltaMs) {
        setColors(LXColor.BLACK);
        setColor((int)indexParam.getValue(), palette.getColor());
    }
}
