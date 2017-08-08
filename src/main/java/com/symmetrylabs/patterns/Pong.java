package com.symmetrylabs.patterns;

import heronarts.lx.LX;
import heronarts.lx.parameter.*;
import heronarts.lx.modulator.*;
import heronarts.lx.transform.*;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.CubesModel;
import com.symmetrylabs.util.MathUtils;

public class Pong extends DPat {
    SinLFO x,y,z,dx,dy,dz;
    public final CompoundParameter pSize;
    public final DiscreteParameter pChoose;

    float cRad;
    LXVector v = new LXVector(0, 0, 0);
    LXVector vMir = new LXVector(0, 0, 0);

    public Pong(LX lx) {
        super(lx);

        cRad = mMax.x / 10;

        addModulator(dx = new SinLFO(6000,  500, 30000  )).trigger();
        addModulator(dy = new SinLFO(3000,  500, 22472  )).trigger();
        addModulator(dz = new SinLFO(1000,  500, 18420  )).trigger();
        addModulator(x  = new SinLFO(cRad, mMax.x - cRad, 0)).trigger();  x.setPeriod(dx);
        addModulator(y  = new SinLFO(cRad, mMax.y - cRad, 0)).trigger();  y.setPeriod(dy);
        addModulator(z  = new SinLFO(cRad, mMax.z - cRad, 0)).trigger();  z.setPeriod(dz);

        pSize = addParam("Size", 0.4);
        pChoose = new DiscreteParameter("Anim", new String[] {"Pong", "Ball", "Cone"});
        pChoose.setValue(2);
        //addNonKnobParameter(pChoose);
        //addSingleParameterUIRow(pChoose);
    }

    void StartRun(double deltaMs) {
        cRad = mMax.x*val(pSize)/6;
    }

    int CalcPoint(LXVector p) {
        v.set(x.getValuef(), y.getValuef(), z.getValuef());
        v.z=0;p.z=0;// ignore z dimension
        switch(pChoose.getValuei()) {
            case 0:
                vMir.set(mMax);
                vMir.add(p.copy().mult(-1));
                return lx.hsb(
                    lxh(),
                    100,
                    c1c(1 - Math.min(v.dist(p), v.dist(vMir)) * .5f / cRad)
                ); // balls
            case 1:
                return lx.hsb(lxh(), 100, c1c(1 - v.dist(p) * .5f / cRad)); // ball
            case 2:
                vMir.set(mMax.x / 2, 0, mMax.z / 2);
                return lx.hsb(
                    lxh(),
                    100,
                    c1c(1 - calcCone(p,v,vMir) * Math.max(.02f, .45f - val(pSize)))
                ); // spot
        }
        return lx.hsb(0,0,0);
    }
}
