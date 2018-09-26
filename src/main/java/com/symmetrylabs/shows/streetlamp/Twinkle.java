package com.symmetrylabs.shows.streetlamp;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import java.util.Arrays;
import heronarts.lx.parameter.CompoundParameter;
import java.util.stream.IntStream;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.List;
import heronarts.lx.transform.LXVector;
import com.symmetrylabs.color.Ops16;

public class Twinkle extends SLPattern<SLModel> {
    public static final String GROUP_NAME = StreetlampShow.SHOW_NAME;

    double[] ages;
    double timeSinceReUp = 0;

    private final CompoundParameter reupParam =
        new CompoundParameter("reup", 500, 10000);
    private final CompoundParameter attackParam =
        new CompoundParameter("attack", 500, 10000);
    private final CompoundParameter releaseParam =
        new CompoundParameter("release", 500, 10000);

    public Twinkle(LX lx) {
        super(lx);
        ages = new double[model.size];
        Arrays.fill(ages, Double.MAX_VALUE);

        addParameter(reupParam);
        addParameter(attackParam);
        addParameter(releaseParam);
    }

    private double adsr(double age, double a, double r) {
        if (age > a + r) {
            return 0;
        }
        if (age < 0) {
            return 0;
        }
        if (age > a) {
            age -= a;
            return 1 - Math.pow(age / r, 0.5);
        }
        return Math.pow(age / a, 1.2);
    }

    @Override
    public void run(double elapsedMs, PolyBuffer.Space preferredSpace) {
        for (int i = 0; i < ages.length; i++) {
            ages[i] += elapsedMs;
        }
        timeSinceReUp += elapsedMs;

        double reup = reupParam.getValue();
        double a = attackParam.getValue();
        double r = releaseParam.getValue();
        double lifetime = a + r;

        if (timeSinceReUp > reup) {
            timeSinceReUp = 0;

            List<Integer> indexes = IntStream
                .range(0, ages.length)
                .boxed()
                .collect(Collectors.toList());
            Collections.shuffle(indexes);

            for (Integer i : indexes) {
                if (ages[i] > lifetime) {
                    ages[i] = 0;
                    break;
                }
            }
        }

        long[] colors = (long[]) getArray(PolyBuffer.Space.RGB16);
        for (LXVector v : getVectors()) {
            int t = (int) (0xFFFF * adsr(ages[v.index], a, r));
            colors[v.index] = Ops16.rgba(0xFFFF, 0xFFFF, 0xFFFF, t);
        }
        markModified(PolyBuffer.Space.RGB16);
    }
}
