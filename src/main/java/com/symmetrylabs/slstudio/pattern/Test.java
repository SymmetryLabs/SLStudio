package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;

import static processing.core.PApplet.*;


public class Test extends LXPattern {

    final CompoundParameter thing = new CompoundParameter("Thing", 0, model.yRange);
    final SinLFO lfo = new SinLFO("Stuff", 0, 1, 2000);

    public Test(LX lx) {
        super(lx);
        addParameter(thing);
        startModulator(lfo);
    }

    public void run(double deltaMs) {
        for (LXPoint p : model.points) {
            colors[p.index] = palette.getColor(max(0, 100 - 10 * abs(p.y - thing.getValuef())));
        }
    }
}
