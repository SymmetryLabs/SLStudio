package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.pattern.base.DPat;
import com.symmetrylabs.util.MathUtils;
import heronarts.lx.LX;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import processing.core.PVector;

public class AskewPlanes extends DPat {


    float huev = 0;

    DiscreteParameter numPlanes = new DiscreteParameter("Number", new String[]{"3", "2", "1"});

    CompoundParameter a1Period = new CompoundParameter( "aP1", 1, 0, 100);
    CompoundParameter b1Period = new CompoundParameter( "bP1", 1, 0, 100);
    CompoundParameter c1Period = new CompoundParameter( "cP1", 1, 0, 100);

    CompoundParameter a2Period = new CompoundParameter( "aP2", 1, 0, 100);
    CompoundParameter b2Period = new CompoundParameter( "bP2", 1, 0, 100);
    CompoundParameter c2Period = new CompoundParameter( "cP2", 1, 0, 100);

    CompoundParameter a3Period = new CompoundParameter( "aP3", 1, 0, 100);
    CompoundParameter b3Period = new CompoundParameter( "bP3", 1, 0, 100);
    CompoundParameter c3Period = new CompoundParameter( "cP3", 1, 0, 100);

    CompoundParameter speed1 = new CompoundParameter("speed1", 1, 0, 1000);
    CompoundParameter speed2= new CompoundParameter("speed2", 1, 0, 1000);
    CompoundParameter speed3 = new CompoundParameter("speed3", 1, 0, 1000);
    CompoundParameter thickness = new CompoundParameter("Thick", 0.2, 0.1, 0.9);
    class Plane {
        private SinLFO a = null;
        private SinLFO b = null;
        private SinLFO c = null;
        float av = 1;
        float bv = 1;
        float cv = 1;
        float denom = 0.1f;

        Plane(int i) {
            if (i == 0) {
                CompoundParameter innera1 = AskewPlanes.this.a1Period;
                CompoundParameter innerb1 = AskewPlanes.this.b1Period;
                CompoundParameter innerc1 = AskewPlanes.this.c1Period;
                addModulator(a = new SinLFO(-1, 1, 4000 + innera1.getValuef() * 1029 * i)).trigger();
                addModulator(b = new SinLFO(-1, 1, 11000 - innerb1.getValuef() * 1104 * i)).trigger();

                addModulator(c = new SinLFO(-50, 50, 4000 + innerc1.getValuef()* 1000 * i * ((i % 2 == 0) ? 1 : -1))).trigger();
            }

            else if (i == 1)  {
                CompoundParameter innera2 = AskewPlanes.this.a2Period;
                CompoundParameter innerb2 = AskewPlanes.this.b2Period;
                CompoundParameter innerc2 = AskewPlanes.this.c2Period;
                addModulator(a = new SinLFO(-1, 1, 4000*speed2.getValuef() + innera2.getValuef() * 1029 * i)).trigger();
                addModulator(b = new SinLFO(-1, 1, 11000*speed2.getValuef() - innerb2.getValuef() * 1104 * i)).trigger();

                addModulator(c = new SinLFO(
                    -50, 50, 4000 *speed2.getValuef() + innerc2.getValuef() * 1000 * i * ((i % 2 == 0) ? 1 : -1))).trigger();
                }

            else if (i == 2) {
            CompoundParameter innera3 = AskewPlanes.this.a3Period;
            CompoundParameter innerb3 = AskewPlanes.this.b3Period;
            CompoundParameter innerc3 = AskewPlanes.this.c3Period;
            addModulator(a = new SinLFO(-1, 1, 4000*speed3.getValuef() + innera3.getValuef() * 1029 * i)).trigger();
            addModulator(b = new SinLFO(-1, 1, 11000*speed3.getValuef() - innerb3.getValuef() * 1104 * i)).trigger();

            addModulator(c = new SinLFO(
                -50, 50, 4000 *speed3.getValuef() + innerc3.getValuef() * 1000 * i * ((i % 2 == 0) ? 1 : -1))).trigger();
        }


        }



        public void run(double deltaMs) {
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

        }
        pTransX.setValue(1);
//        addParameter(a1Period);
//        addParameter(b1Period);
//        addParameter(c1Period);
//
//        addParameter(a2Period);
//        addParameter(b2Period);
//        addParameter(c2Period);
//
//        addParameter(a3Period);
//        addParameter(b3Period);
//        addParameter(c3Period);
//
//        addParameter(speed1);
//        addParameter(speed2);
//        addParameter(speed3);
//        addParameter(numPlanes);
        removeParameter(pRotX);
        removeParameter(pRotY);
        removeParameter(pRotZ);
        removeParameter(pRotX);
        removeParameter(pSpin);
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
