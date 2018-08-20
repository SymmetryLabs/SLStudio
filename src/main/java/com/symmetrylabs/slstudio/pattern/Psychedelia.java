package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
//import com.symmetrylabs.slstudio.pattern.SLPattern;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.MathUtils;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.modulator.TriangleLFO;

//import static com.symmetrylabs.slstudio.util.MathUtils.constrain;

//import static com.symmetrylabs.util.MathUtils.*;

import com.symmetrylabs.shows.summerstage.SummerStageShow;

public class Psychedelia extends SLPattern<StripsModel<Strip>> {
    public static final String GROUP_NAME = SummerStageShow.SHOW_NAME;

    final int NUM = 3;
    SinLFO m = new SinLFO(-0.5, NUM-0.5, 9000);
    SinLFO s = new SinLFO(-20, 147, 11000);
    TriangleLFO h = new TriangleLFO(0, 240, 19000);
    SinLFO c = new SinLFO(-.2, .8, 31000);

    public Psychedelia(LX lx) {
        super(lx);
        addModulator(m).trigger();
        addModulator(s).trigger();
        addModulator(h).trigger();
        addModulator(c).trigger();
    }
@Override
    public void run(double deltaMs) {
        float huev = h.getValuef();
        float cv = c.getValuef();
        float sv = s.getValuef();
        float mv = m.getValuef();
        int i = 0;
        for (Strip strip : model.getStrips()) {
            for (LXPoint p : strip.points) {
                colors[p.index] = lx.hsb(
                    huev + i*MathUtils.constrain(cv, 0, 2f) + p.z/2f + p.x/4f,
                    MathUtils.min(100, MathUtils.abs(p.y-sv)),
                    MathUtils.max(0, 100 - 50*MathUtils.abs((i%NUM) - mv))
                );
            }
            ++i;
        }
    }
}
