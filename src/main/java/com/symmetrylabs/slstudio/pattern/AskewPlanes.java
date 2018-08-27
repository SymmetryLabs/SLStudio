package com.symmetrylabs.slstudio.pattern;

import com.google.common.collect.ComputationException;
import com.symmetrylabs.slstudio.pattern.base.DPat;
import com.symmetrylabs.util.MathUtils;
import heronarts.lx.LX;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import processing.core.PVector;


public class AskewPlanes extends DPat {


    float huev = 0;

    DiscreteParameter numPlanes = new DiscreteParameter("Number", new String[]{"3", "2", "1"});
    CompoundParameter[] Speeds = new CompoundParameter[3*3];

    CompoundParameter speed1 = new CompoundParameter("speed1", -4500,-10000,10000);
    CompoundParameter speed2 = new CompoundParameter("speed2", 1,-10000,10000);
    CompoundParameter speed3 = new CompoundParameter("speed3", 1, -60000, 6000);
    CompoundParameter thickness = new CompoundParameter("Thick", 0.2, 0.1, 0.9);
    class Plane {
          SinLFO a;
          SinLFO b;
          SinLFO c;
        float av = 1;
        float bv = 1;
        float cv = 1;
        float denom = 0.1f;

        Plane(int i) {
            addModulator(a = new SinLFO(-1, 1,speed1.getValuef()* (4000 + 1029 * i))).trigger();
            addModulator(b = new SinLFO(-1, 1, speed2.getValuef()*(11000 - 1104 * i))).trigger();
            addModulator(c = new SinLFO(-50, 50, speed3.getValuef()*(4000 + 1000 * i * ((i % 2 == 0) ? 1 : -1)))).trigger();
        }

        void run(double deltaMs) {
            float ap = a.getPeriodf();
            float bp = b.getPeriodf();
            float cp = c.getPeriodf();

            a.setPeriod(ap + speed1.getValuef() );
            b.setPeriod(bp + speed2.getValuef());
            c.setPeriod(cp + speed3.getValuef());

            av = a.getValuef();
            bv = b.getValuef();
            cv = c.getValuef();
            denom = MathUtils.sqrt(av * av + bv * bv);
        }
    }

    final Plane[] planes;
    final int NUM_PLANES = 3;

    public AskewPlanes(LX lx) {
        super(lx);
        addParameter(thickness);
        planes = new Plane[NUM_PLANES];
        for (int i = 0; i < planes.length; ++i) {
            planes[i] = new Plane(i);
            Speeds[i] = new CompoundParameter("speed1", 1, 0, 10000);
        }
        pTransX.setValue(1);
        addParameter(numPlanes);
        addParameter(speed1);
        addParameter(speed2);
        addParameter(speed3);
        removeParameter(pRotX);
        removeParameter(pRotY);
        removeParameter(pRotZ);
        removeParameter(pRotX);
        removeParameter(pSpin);
    }
     public    void onParameterChanged(LXParameter parameter){
         if (planes != null){
        for (Plane x : planes) {
            if (x != null) continue;

            x.a.setPeriod(speed1.getValuef());
            x.b.setPeriod(speed2.getValuef());
            x.c.setPeriod(speed3.getValuef());
        }
     }
    }
    @Override
    protected void StartRun(double deltaMs) {
        huev = palette.getHuef();

        // This is super fucking bizarre. But if this is a for loop, the framerate
        // tanks to like 30FPS, instead of 60. Call them manually and it works fine.
        // Doesn't make ANY sense... there must be some weird side effect going on
        // with the Processing internals perhaps?
//    for (Plane plane : planes) {
//      plane.run(deltaMs);
//    }
        planes[0].run(deltaMs);
        planes[1].run(deltaMs);
        planes[2].run(deltaMs);
    }

    @Override
    public int CalcPoint(PVector p) {
        float d = Float.MAX_VALUE;

        int i = 0;
        for (Plane plane : planes) {
            if (i++ <= numPlanes.getValuei() - 1) continue;
            if (plane.denom != 0) {
                d = MathUtils.min(d, MathUtils.abs(plane.av * (p.x - model.cx) + plane.bv * (p.y - model.cy) + plane.cv) / plane.denom);
            }
        }
        return lx.hsb(
            huev + MathUtils.abs(p.x - model.cx) * .3f + p.y * .8f,
            MathUtils.max(0, 100 - .15f * MathUtils.abs(p.x - model.cx)),
            MathUtils.constrain(700f * thickness.getValuef() - 10f * d, 0, 100)
        );
    }
}
