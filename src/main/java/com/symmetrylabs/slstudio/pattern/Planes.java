package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

public class Planes extends SLPattern {

    public final CompoundParameter xPos1 = new CompoundParameter("xPos1");
    public final CompoundParameter width1 = new CompoundParameter("wid1");

    public final CompoundParameter xPos2 = new CompoundParameter("xPos2");
    public final CompoundParameter width2 = new CompoundParameter("wid2");

    public Planes(LX lx) {
        super(lx);
        addParameter(xPos1);
        addParameter(width1);
        addParameter(xPos2);
        addParameter(width2);
    }

    public void run(double deltaMs) {
        setColors(0);

        float xPos1v = model.xMin + (xPos1.getValuef() * model.xRange);
        float xPos2v = model.xMin + (xPos2.getValuef() * model.xRange);

        float width1v = width1.getValuef() * model.xRange;
        float width2v = width2.getValuef() * model.xRange;

        for (LXPoint p : model.points) {
            if (Math.abs(p.x - xPos1v) < width1v
             || Math.abs(p.x - xPos2v) < width2v) {
                colors[p.index] = lx.hsb(palette.getHuef(), 100, 100);
            }
        }
    }

}
