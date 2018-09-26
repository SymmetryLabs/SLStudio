package com.symmetrylabs.shows.streetlamp;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.parameter.CompoundParameter;
import java.util.Arrays;

public abstract class FramePattern extends SLPattern<SLModel> {
    public static final String GROUP_NAME = StreetlampShow.SHOW_NAME;

    private final CompoundParameter period =
        new CompoundParameter("period", 500, 1, 10000);
    private final CompoundParameter exp =
        new CompoundParameter("xfade", 4, 0.7, 10);

    private double t;
    private String caption;
    private final int[][] frames;

    public FramePattern(LX lx) {
        super(lx);
        addParameter(period);
        addParameter(exp);
        caption = null;
        frames = getFrames();
    }

    protected abstract int[][] getFrames();

    @Override
    public void run(double elapsedMs, PolyBuffer.Space preferredSpace) {
        final double p = period.getValue();
        final int n = frames.length;
        t = (t + elapsedMs) % (n * p);

        final int f1 = (int) Math.floor(t / p);
        final int f2 = (f1 + 1) % n;

        /* x is the position of t in the interval between frames 1 and 2 */
        final double x = t - p * f1;
        final double f1w = Math.pow(1 - x / p, exp.getValue());
        final double f2w = Math.pow(x / p, exp.getValue());

        final int f1g = (int)(0xFFFF * f1w);
        final long f1c = Ops16.rgba(f1g, f1g, f1g, 0xFFFF);
        final int f2g = (int)(0xFFFF * f2w);
        final long f2c = Ops16.rgba(f2g, f2g, f2g, 0xFFFF);

        long[] colors = (long[]) getArray(PolyBuffer.Space.RGB16);
        Arrays.fill(colors, 0xFFFF_0000_0000_0000L);
        for (int i : frames[f1]) {
            colors[i] = f1c;
        }
        for (int i : frames[f2]) {
            colors[i] = f2c;
        }
        markModified(PolyBuffer.Space.RGB16);
    }
}
