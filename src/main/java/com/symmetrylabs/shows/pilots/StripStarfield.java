package com.symmetrylabs.shows.pilots;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.ColorUtils;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StripStarfield<T extends Strip> extends SLPattern<StripsModel<T>> {
    public static final String GROUP_NAME = PilotsShow.SHOW_NAME;

    private final CompoundParameter period = new CompoundParameter("period", 100, 1, 1000);
    private final CompoundParameter release = new CompoundParameter("release", 250, 0, 5000);

    private List<List<Integer>> stripKeys;
    private float t = 0;
    private int stage = 0;
    private final int N;
    private final int L;

    public StripStarfield(LX lx) {
        super(lx);
        addParameter(period);
        addParameter(release);

        N = model.getStrips().size();
        L = N == 0 ? 0 : model.getStripByIndex(0).size;
        stripKeys = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            List<Integer> strip = new ArrayList<>();
            for (int j = 0; j < L; j++) {
                strip.add(j);
            }
            Collections.shuffle(strip);
            stripKeys.add(strip);
        }
    }

    @Override
    public void run(double elapsedMs) {
        t += elapsedMs;
        float p = period.getValuef();
        boolean firstFrame = false;
        if (t > p) {
            stage = (stage + (int) (t / p)) % L;
            t = t % p;
            firstFrame = true;
        }

        final int white = LXColor.rgb(255, 255, 255);
        final int black = 0;
        final float duration = release.getValuef();

        for (int i = 0; i < N; i++) {
            Strip s = model.getStripByIndex(i);
            List<Integer> skeys = stripKeys.get(i);
            for (int j = 0; j < s.points.length; j++) {
                int pstage = stage - skeys.get(j);
                if (pstage < 0) {
                    pstage += L;
                }
                float age = p * pstage + t;
                colors[s.points[j].index] =
                    age > duration ? black :
                    pstage == 0 && firstFrame ? white :
                    ColorUtils.setAlpha(white, 1f - (age / duration));
            }
        }
    }
}
