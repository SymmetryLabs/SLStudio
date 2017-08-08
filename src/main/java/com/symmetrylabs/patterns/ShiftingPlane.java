package com.symmetrylabs.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.*;
import heronarts.lx.modulator.*;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.util.MathUtils;

public class ShiftingPlane extends LXPattern {

    final CompoundParameter hueShift = new CompoundParameter("hShift", 0.5, 0, 1);

    final SinLFO a = new SinLFO(-.2, .2, 5300);
    final SinLFO b = new SinLFO(1, -1, 13300);
    final SinLFO c = new SinLFO(-1.4, 1.4, 5700);
    final SinLFO d = new SinLFO(-10, 10, 9500);

    public ShiftingPlane(LX lx) {
        super(lx);
        addParameter(hueShift);
        addModulator(a).trigger();
        addModulator(b).trigger();
        addModulator(c).trigger();
        addModulator(d).trigger();
    }

    public void run(double deltaMs) {
        float hv = palette.getHuef();
        float av = a.getValuef();
        float bv = b.getValuef();
        float cv = c.getValuef();
        float dv = d.getValuef();
        float denom = (float)Math.sqrt(av*av + bv*bv + cv*cv);

        for (LXPoint p : model.points) {
            float d = (float)Math.abs(av*(p.x-model.cx) + bv*(p.y-model.cy) + cv*(p.z-model.cz) + dv) / denom;
            colors[p.index] = lx.hsb(
                hv + ((float)Math.abs(p.x - model.cx) * .6f + (float)Math.abs(p.y - model.cy) * .9f + (float)Math.abs(p.z - model.cz)) * hueShift.getValuef(),
                MathUtils.constrain(110 - 6 * d, 0, 100),
                MathUtils.constrain(130 - 7 * d, 0, 100)
            );
        }
    }
}
