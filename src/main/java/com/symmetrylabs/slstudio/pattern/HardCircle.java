package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.pattern.base.DPat;
import com.symmetrylabs.util.MathUtils;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import processing.core.PVector;

public class HardCircle extends DPat {
    CompoundParameter pRad;

    public HardCircle(LX lx) {
        super(lx);
        pRad = new CompoundParameter("RAD", 50, 0, 200);
        addParameter(pRad);
    }

    @Override
    protected void StartRun(double deltaMs) {
        // cRad = mMax.x * val(pSize) / 6;
    }

    @Override
    public int CalcPoint(PVector p) {
        return LXColor.hsb(0, 0, 100);
        // float xDist = p.x - model.cx;
        // float yDist = p.y - model.cy;
        // float dist = Math.sqrt(xDist * xDist + yDist * yDist);
        // if (dist < pRad.getValuef()) {
        //     return LXColor.hsb(0, 0, 100);
        // } else {
        //     return  LXColor.hsb(0, 0, 0);
        // }
    }
}
