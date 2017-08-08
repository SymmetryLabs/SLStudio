package com.symmetrylabs.patterns;

import heronarts.lx.LX;
import heronarts.lx.parameter.*;
import heronarts.lx.transform.*;
import heronarts.lx.color.LXColor;
import heronarts.lx.LXUtils;

public class Ball extends DPat {

    CompoundParameter xPos = new CompoundParameter("xPos", model.cx, model.xMin, model.xMax);
    CompoundParameter yPos = new CompoundParameter("yPos", model.cy, model.yMin, model.yMax);
    CompoundParameter zPos = new CompoundParameter("zPos", model.cz, model.zMin, model.zMax);

    CompoundParameter size = new CompoundParameter("size", model.xRange*0.1, model.xRange*0.01, model.xRange*0.5);

    public Ball(LX lx) {
        super(lx);

        addParameter(xPos);
        addParameter(yPos);
        addParameter(zPos);
        addParameter(size);
    }

    int CalcPoint(LXVector p) {
        if (LXUtils.distance(p.x, p.y, xPos.getValuef(), yPos.getValuef()) < size.getValuef()) {
            return lx.hsb(lxh(), 100, 100);
        } else {
            return LXColor.BLACK;
        }
    }
}
