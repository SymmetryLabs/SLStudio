package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import com.symmetrylabs.util.SimplexNoise;
import heronarts.lx.transform.LXVector;
import com.symmetrylabs.color.Ops16;
import static com.symmetrylabs.util.MathUtils.*;
import heronarts.lx.parameter.CompoundParameter;

public class SimplexNoisePattern extends SLPattern<SLModel> {
    private final CompoundParameter scale =
        new CompoundParameter("scale", -1, -3, 1);
    private final CompoundParameter rate = (CompoundParameter)
        new CompoundParameter("rate", 1, 0, 10).setExponent(3);
    private final CompoundParameter exp = (CompoundParameter)
        new CompoundParameter("exp", 1.5, 0.01, 3).setExponent(0.5);

    private double w = 0;

    public SimplexNoisePattern(LX lx) {
        super(lx);
        addParameter(scale);
        addParameter(rate);
        addParameter(exp);
    }

    @Override
    public void run(double elapsedMs, PolyBuffer.Space preferredSpace) {
        w += rate.getValue() * elapsedMs / 1000;
        float sc = (float) Math.pow(10, scale.getValue());
        long[] c = (long[]) getArray(PolyBuffer.Space.RGB16);
        float e = exp.getValuef();
        for (LXVector v : getVectors()) {
            double nv = SimplexNoise.noise(
                sc * (v.x - model.cx),
                sc * (v.y - model.cy),
                sc * (v.z - model.cz), w);
            long g = (long) (0xFFFFL * Math.pow(Math.abs(nv), e));
            c[v.index] = 0xFFFF_0000_0000_0000L | (g << 32) | (g << 16) | g;
        }
        markModified(PolyBuffer.Space.RGB16);
    }
}
