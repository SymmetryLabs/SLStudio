package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transform.LXVector;

public class Invert extends LXEffect {

    public Invert(LX lx) {
        super(lx);
    }

    @Override
    public void run(double deltaMs, double enabledAmount) {
        for (LXVector v : getVectors()) {
            int c = colors[v.index];
            float h = LXColor.h(c);
            float s = LXColor.s(c);
            float b = LXColor.b(c);
            colors[v.index] = LXColor.hsb(h, s, 100.0f - b);
        }
    }
}
