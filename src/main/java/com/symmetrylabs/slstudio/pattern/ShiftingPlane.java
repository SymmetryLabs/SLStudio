package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.LXColor16;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.PolyBuffer;

import static com.symmetrylabs.util.MathUtils.*;


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
        Object array = getArray(space);
        final int[] intColors = (space == PolyBuffer.Space.RGB8) ? (int[]) array : null;
        final long[] longColors = (space == PolyBuffer.Space.RGB16) ? (long[]) array : null;
        
        float hv = palette.getHuef();
        float av = a.getValuef();
        float bv = b.getValuef();
        float cv = c.getValuef();
        float dv = d.getValuef();
        float denom = sqrt(av * av + bv * bv + cv * cv);

        for (LXPoint p : model.points) {
            float d = abs(av * (p.x - model.cx) + bv * (p.y - model.cy) + cv * (p.z - model.cz) + dv) / denom;

            if (space == PolyBuffer.Space.RGB8) {
                intColors[p.index] = LXColor.hsb(
                    hv + (abs(p.x - model.cx) * .6f + abs(p.y - model.cy) * .9f + abs(p.z - model.cz)) * hueShift.getValuef(),
                    constrain(110 - d * 6, 0, 100),
                    constrain(130 - 7 * d, 0, 100)
                );
            }
            else if (space == PolyBuffer.Space.RGB16) {
                longColors[p.index] = LXColor16.hsb(
                    hv + (abs(p.x - model.cx) * .6f + abs(p.y - model.cy) * .9f + abs(p.z - model.cz)) * hueShift.getValuef(),
                    constrain(110 - d * 6, 0, 100),
                    constrain(130 - 7 * d, 0, 100)
                );
            }
        }
        markModified(space);
    }
}
