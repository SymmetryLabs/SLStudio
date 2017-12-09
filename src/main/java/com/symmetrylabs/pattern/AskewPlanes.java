package com.symmetrylabs.pattern;

import com.symmetrylabs.util.dan.DPat;
import heronarts.lx.LX;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import processing.core.PVector;

import static processing.core.PApplet.*;
import static processing.core.PConstants.MAX_FLOAT;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class AskewPlanes extends DPat {

    CompoundParameter thickness = new CompoundParameter("thck", 0.2, 0.1, 0.9);
    float huev = 0;

    DiscreteParameter numPlanes = new DiscreteParameter("num", new String[]{"3", "2", "1"});

    class Plane {
        private final SinLFO a;
        private final SinLFO b;
        private final SinLFO c;
        float av = 1;
        float bv = 1;
        float cv = 1;
        float denom = 0.1f;

        Plane(int i) {
            addModulator(a = new SinLFO(-1, 1, 4000 + 1029 * i)).trigger();
            addModulator(b = new SinLFO(-1, 1, 11000 - 1104 * i)).trigger();
            addModulator(c = new SinLFO(-50, 50, 4000 + 1000 * i * ((i % 2 == 0) ? 1 : -1))).trigger();
        }

        void run(double deltaMs) {
            av = a.getValuef();
            bv = b.getValuef();
            cv = c.getValuef();
            denom = sqrt(av * av + bv * bv);
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
        addParameter(numPlanes);
        removeParameter(pRotX);
        removeParameter(pRotY);
        removeParameter(pRotZ);
        removeParameter(pRotX);
        removeParameter(pSpin);
    }

    void StartRun(double deltaMs) {
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

    int CalcPoint(PVector p) {
        //for (LXPoint p : model.points) {
        float d = MAX_FLOAT;

        int i = 0;
        for (Plane plane : planes) {
            if (i++ <= numPlanes.getValuei() - 1) continue;
            if (plane.denom != 0) {
                d = min(d, abs(plane.av * (p.x - model.cx) + plane.bv * (p.y - model.cy) + plane.cv) / plane.denom);
            }
        }
        return lx.hsb(
            huev + abs(p.x - model.cx) * .3f + p.y * .8f,
            max(0, 100 - .15f * abs(p.x - model.cx)),
            constrain(700f * thickness.getValuef() - 10f * d, 0, 100)
        );
        //}
    }
}
