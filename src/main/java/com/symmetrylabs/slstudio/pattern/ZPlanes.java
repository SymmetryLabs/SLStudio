package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

public class ZPlanes extends SLPattern {

    public final CompoundParameter zPos1 = new CompoundParameter("zPos1");
    public final CompoundParameter width1 = new CompoundParameter("wid1");

    public final CompoundParameter zPos2 = new CompoundParameter("zPos2");
    public final CompoundParameter width2 = new CompoundParameter("wid2");

    public ZPlanes(LX lx) {
        super(lx);
        addParameter(zPos1);
        addParameter(width1);
        addParameter(zPos2);
        addParameter(width2);
    }

    public void run(double deltaMs) {
        setColors(0);

        float zPos1v = model.zMin + (zPos1.getValuef() * model.zRange);
        float zPos2v = model.zMin + (zPos2.getValuef() * model.zRange);

        float width1v = width1.getValuef() * model.zRange;
        float width2v = width2.getValuef() * model.zRange;

        for (LXPoint p : model.points) {
            if (Math.abs(p.z - zPos1v) < width1v
             || Math.abs(p.z - zPos2v) < width2v) {
                colors[p.index] = lx.hsb(palette.getHuef(), 100, 100);
            }
        }
    }

}
