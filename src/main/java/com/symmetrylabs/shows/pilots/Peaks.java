package com.symmetrylabs.shows.pilots;

import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.ColorUtils;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.transform.LXVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Peaks extends SLPattern<SLModel> {
    public static final String GROUP_NAME = PilotsShow.SHOW_NAME;

    private final DiscreteParameter count = new DiscreteParameter("count", 7, 0, 20);
    private final CompoundParameter height = new CompoundParameter("height", 100, 0, 800);
    private final CompoundParameter omega = new CompoundParameter("omega", 120, -960, 960);
    private final CompoundParameter radius = new CompoundParameter("radius", 16, 0, 200);
    private final CompoundParameter highLevel = new CompoundParameter("high", 100, 0, 100);
    private final CompoundParameter midLevel = new CompoundParameter("mid", 80, 0, 100);
    private final CompoundParameter lowLevel = new CompoundParameter("low", 0, 0, 100);

    private float rot = 0;

    public Peaks(LX lx) {
        super(lx);
        addParameter(count);
        addParameter(height);
        addParameter(omega);
        addParameter(radius);
        addParameter(highLevel);
        addParameter(midLevel);
        addParameter(lowLevel);
    }

    @Override
    public void run(double elapsedMs) {
        rot += elapsedMs / 1000 * Math.PI * omega.getValue() / 180;

        int n = count.getValuei();
        float xstep = model.xRange / (n - 1);
        double rad = radius.getValue();
        double h = height.getValue();

        ArrayList<LXVector> centers = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            centers.add(
                new LXVector(
                    (float) (i * xstep + (i % 2 == 0 ? 1 : -1) * rad * Math.sin(rot)),
                    (float) (model.yMin + rad * Math.cos(rot) + h),
                    model.cz));
        }

        final int high = LXColor.gray(highLevel.getValuef());
        final int mid = LXColor.gray(midLevel.getValuef());
        final int low = LXColor.gray(lowLevel.getValuef());

        for (LXVector v : getVectors()) {
            int member = 0;
            for (LXVector center : centers) {
                LXVector d = center.copy().mult(-1).add(v);
                if (d.y < 0 && Math.abs(d.x) < Math.abs(d.y)) {
                    member++;
                }
            }
            colors[v.index] = member > 1 ? high : member == 1 ? mid : low;
        }
    }
}
