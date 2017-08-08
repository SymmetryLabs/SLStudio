package com.symmetrylabs.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.*;
import heronarts.lx.modulator.*;
import heronarts.lx.model.LXPoint;

public class Test extends LXPattern {

    final CompoundParameter thing = new CompoundParameter("Thing", 0, model.yRange);
    final SinLFO lfo = new SinLFO("Stuff", 0, 1, 2000);

    public Test(LX lx) {
        super(lx);
        addParameter(thing);
        startModulator(lfo);
    }

    @Override
    public void run(double deltaMs) {
        for (LXPoint p : model.points) {
            colors[p.index] = palette.getColor(Math.max(0, 100 - 10 * (float)Math.abs(p.y - thing.getValuef())));
        }
    }
}
