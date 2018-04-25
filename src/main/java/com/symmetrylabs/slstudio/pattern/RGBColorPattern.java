package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.color.LXColor;


public class RGBColorPattern extends LXPattern {
    private final DiscreteParameter red = new DiscreteParameter("RED", 0, 0, 256);
    private final DiscreteParameter green = new DiscreteParameter("GREEN", 0, 0, 256);
    private final DiscreteParameter blue = new DiscreteParameter("BLUE", 0, 0, 256);

    public RGBColorPattern(LX lx) {
        super(lx);

        addParameter(red);
        addParameter(green);
        addParameter(blue);
    }

    public void run(double deltaMs) {
        setColors(0xFF << LXColor.ALPHA_SHIFT | red.getValuei() << LXColor.RED_SHIFT
                | green.getValuei() << LXColor.GREEN_SHIFT | blue.getValuei());
    }
}
