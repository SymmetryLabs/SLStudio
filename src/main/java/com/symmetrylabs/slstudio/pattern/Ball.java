package com.symmetrylabs.slstudio.pattern;

import processing.core.PVector;

import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.slstudio.pattern.base.DPat;

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

    public void StartRun(double deltaMs) {

    }

    public int CalcPoint(PVector p) {
        if (LXUtils.distance(p.x, p.y, xPos.getValuef(), yPos.getValuef()) < size.getValuef()) {
            return lx.hsb(lxh(), 100, 100);
        } else {
            return LXColor.BLACK;
        }
    }
}