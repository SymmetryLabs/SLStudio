package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.pattern.base.DPat;
import com.symmetrylabs.util.MathUtils;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import processing.core.PVector;

import com.symmetrylabs.shows.summerstage.SummerStageShow;

public class Pong extends DPat {
    public static final String GROUP_NAME = SummerStageShow.SHOW_NAME;

    SinLFO x, y, z, dx, dy, dz;
    float cRad;
    CompoundParameter pSize;
    DiscreteParameter pChoose;
    PVector v = new PVector(), vMir = new PVector();

    public Pong(LX lx) {
        super(lx);
        cRad = mMax.x / 10;
        addModulator(dx = new SinLFO(6000, 500, 30000)).trigger();
        addModulator(dy = new SinLFO(3000, 500, 22472)).trigger();
        addModulator(dz = new SinLFO(1000, 500, 18420)).trigger();
        addModulator(x = new SinLFO(cRad, mMax.x - cRad, 0)).trigger();
        x.setPeriod(dx);
        addModulator(y = new SinLFO(cRad, mMax.y - cRad, 0)).trigger();
        y.setPeriod(dy);
        addModulator(z = new SinLFO(cRad, mMax.z - cRad, 0)).trigger();
        z.setPeriod(dz);
        pChoose = new DiscreteParameter("Anim", new String[]{"Pong", "Ball", "Cone"});
        pSize = addParam("Size", 0.4f);
        pChoose.setValue(2);
        addParameter(pChoose);
        //addNonKnobParameter(pChoose);
        //addSingleParameterUIRow(pChoose);
        removeParameter(pRotX);
        removeParameter(pRotY);
        removeParameter(pRotZ);
        removeParameter(pRotX);
        removeParameter(pSpin);
    }

    @Override
    protected void StartRun(double deltaMs) {
        cRad = mMax.x * val(pSize) / 6;
    }

    @Override
    public int CalcPoint(PVector p) {
        v.set(x.getValuef(), y.getValuef(), z.getValuef());
        v.z = 0;
        p.z = 0;// ignore z dimension
        switch (pChoose.getValuei()) {
            case 0:
                vMir.set(mMax);
                vMir.sub(p);
                return LXColor.hsb(lxh(), 100, c1c(1 - MathUtils.min(v.dist(p), v.dist(vMir)) * .5f / cRad));   // balls
            case 1:
                return LXColor.hsb(lxh(), 100, c1c(1 - v.dist(p) * .5f / cRad));              // ball
            case 2:
                vMir.set(mMax.x / 2, 0, mMax.z / 2);
                return LXColor.hsb(lxh(), 100, c1c(1 - calcCone(p, v, vMir) * MathUtils.max(.02f, .45f - val(pSize))));   // spot
        }
        return LXColor.hsb(0, 0, 0);
    }
}
