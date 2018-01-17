package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

public class RgbaFilter extends LXEffect {

    public final CompoundParameter red = new CompoundParameter("red", 0, 0, 255);
    public final CompoundParameter green = new CompoundParameter("green", 0, 0, 255);
    public final CompoundParameter blue = new CompoundParameter("blue", 0, 0, 255);

    public RgbaFilter(LX lx) {
        super(lx);
        addParameter(red);
        addParameter(green);
        addParameter(blue);
    }

    @Override
    public void run(double deltaMs, double amount) {
        for (LXPoint p : model.points) {
            float brightness = LXColor.b(colors[p.index]);

            int newColor = LXColor.rgb(
                (int)red.getValue(),
                (int)green.getValue(),
                (int)blue.getValue()
            );

            colors[p.index] = lx.hsb(
                LXColor.h(newColor),
                LXColor.s(newColor),
                brightness
            );
        }
    }
}
