package com.symmetrylabs.shows.streetlamp;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.MathUtils;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;

import java.util.Random;

public class SWave extends SLPattern<SLModel> {
    public static final String GROUP_NAME = StreetlampShow.SHOW_NAME;

    private static final int HUE_GROUPS = 4;

    private final CompoundParameter period = new CompoundParameter("period", 6000, 30000);
    private final CompoundParameter width = new CompoundParameter("width", 12, 12);
    private final CompoundParameter amplitude = new CompoundParameter("amp", 0.8, 1);
    private final CompoundParameter saturation = new CompoundParameter("sat", 10, 100);
    private final CompoundParameter hmin = new CompoundParameter("hmin", 0, 360);
    private final CompoundParameter hmax = new CompoundParameter("hmax", 60, 360);
    private final CompoundParameter speed = new CompoundParameter("speed", 0.1, 1);

    private double[] hues = null;
    private int[] hueGroups = new int[model.size];
    private final Random random = new Random();

    float t;

    public SWave(LX lx) {
        super(lx);

        addParameter(period);
        addParameter(width);
        addParameter(amplitude);
        addParameter(saturation);
        addParameter(hmin);
        addParameter(hmax);
        addParameter(speed);

        for (int i = 0; i < model.size; i++) {
            hueGroups[i] = random.nextInt(HUE_GROUPS);
        }
    }

    @Override
    public void run(double elapsedMs, PolyBuffer.Space preferredSpace) {
        int[] colors = (int[]) getArray(PolyBuffer.Space.SRGB8);

        if (hues == null) {
            hues = new double[HUE_GROUPS];
            double hm = hmin.getValue();
            double hr = hmax.getValue() - hm;
            for (int i = 0; i < HUE_GROUPS; i++) {
                hues[i] = hm + (i * hr / HUE_GROUPS);
            }
        } else {
            for (int i = 0; i < HUE_GROUPS; i++) {
                hues[i] = MathUtils.constrain(
                    elapsedMs * speed.getValue() * (random.nextFloat() - 0.5) + hues[i],
                    hmin.getValue(), hmax.getValue());
            }
        }

        double p = period.getValue();
        t += elapsedMs;
        if (t > p) {
            t -= p;
        }

        final float sat = saturation.getValuef();
        final float w = width.getValuef();
        final float amp = amplitude.getValuef();
        final float hlo = model.yMin - 4 * w;
        final float hhi = model.yMax + 4 * w;
        final float hrange = hhi - hlo;
        final float h = (float) (hlo + hrange * Math.sin(t * Math.PI / p));

        for (LXVector v : getVectors()) {
            double s = sat - Math.cos((v.y - h) / (0.5 * Math.PI * w)) * 100 * amp;
            s = s > 100 ? 100 : s < 0 ? 0 : s;
            colors[v.index] = LXColor.hsb(hues[hueGroups[v.index]], s, 100);
        }

        markModified(PolyBuffer.Space.SRGB8);
    }
}
