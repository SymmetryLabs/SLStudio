package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;

import static com.symmetrylabs.util.MathUtils.*;
import static heronarts.lx.PolyBuffer.Space.SRGB8;

public class ShiftingPlane extends LXPattern {

    final CompoundParameter hueShift = new CompoundParameter("HueVar", 0.5, 0, 1);

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

    public void run(double deltaMs, PolyBuffer.Space space) {
        palette = getActivePalette();
        int[] colors = (int[]) getArray(SRGB8);

        float hv = palette.getHuef();
        float av = a.getValuef();
        float bv = b.getValuef();
        float cv = c.getValuef();
        float dv = d.getValuef();
        float denom = sqrt(av * av + bv * bv + cv * cv);

        for (LXVector v : getVectors()) {
            float d = abs(av * (v.x - model.cx) + bv * (v.y - model.cy) + cv * (v.z - model.cz) + dv) / denom;
            colors[v.index] = LXColor.hsb(
                hv + (abs(v.x - model.cx) * .6f + abs(v.y - model.cy) * .9f + abs(v.z - model.cz)) * hueShift.getValuef(),
                constrain(110 - d * 6, 0, 100),
                constrain(130 - 7 * d, 0, 100)
            );
        }
        markModified(SRGB8);
    }
}
