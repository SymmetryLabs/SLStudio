package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.pattern.base.DPat;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.MathUtils;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.modulator.TriangleLFO;

public class Blinders extends SLPattern<StripsModel<Strip>> {
    final SinLFO[] m;
    final TriangleLFO r;
    final SinLFO s;
    final TriangleLFO hs;
//    TriangleLFO hs = new TriangleLFO(0.1, 0.5, 15000);
    public Blinders(LX lx) {
        super(lx);
        m = new SinLFO[12];
        for (int i = 0; i < m.length; ++i) {
            addModulator(m[i] = new SinLFO(0.5, 120, (120000. / (3+i)))).trigger();
        }
//        addModulator(x = new SinLFO(cRad-30, mMax.x - cRad + 30, 0)).trigger();
        addModulator(r = new TriangleLFO(9000, 15000, 29000)).trigger();
        addModulator(s = new SinLFO(-20, 275, 11000)).trigger();
        addModulator(hs = new TriangleLFO(0.1, 0.5, 15000)).trigger();
        s.setPeriod(r);

    }


    public void run(double deltaMs) {
        float hv = palette.getHuef();
        int si = 0;
        for (Strip strip : model.getStrips()) {
            int i = 0;
            float mv = m[si % m.length].getValuef();
            for (LXPoint p : strip.points) {
                colors[p.index] = lx.hsb(
                    hv + p.z + p.y*hs.getValuef(),
                    MathUtils.min(100, MathUtils.abs(p.x - s.getValuef())/2f),
                    MathUtils.max(0, 100 - mv/2f - mv * MathUtils.abs(i - (strip.metrics.numPoints-1f)/2f))
                );
                ++i;
            }
            ++si;
        }
    }
}
