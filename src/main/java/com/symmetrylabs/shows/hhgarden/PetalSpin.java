package com.symmetrylabs.shows.hhgarden;

import com.symmetrylabs.shows.hhgarden.FlowerModel.Direction;
import com.symmetrylabs.shows.hhgarden.FlowerModel.FlowerPoint;
import com.symmetrylabs.shows.hhgarden.FlowerModel.Group;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;

public class PetalSpin extends FlowerPattern {
    private final CompoundParameter rateParam = new CompoundParameter("rate", 150, 1, 2000);
    private final CompoundParameter fadeParam = new CompoundParameter("fade", 0.4, 0, 1);
    private final BooleanParameter invertParam = new BooleanParameter("invert", false);

    double timeSinceSwap = 0;
    Direction cur = Direction.A;

    public PetalSpin(LX lx) {
        super(lx);
        addParameter(rateParam);
        addParameter(fadeParam);
        addParameter(invertParam);
    }

    @Override
    public void run(double elapsedMs) {
        timeSinceSwap += elapsedMs;
        double prop = timeSinceSwap / rateParam.getValue();
        if (prop > 1) {
            prop = 0;
            timeSinceSwap = 0;
            cur = step(cur);
        }
        Direction last = step(step(cur));
        float fade = fadeParam.getValuef();
        float grayLevel = fade == 0 || prop > fade ? 0.f :
            100.f * (float) (1.f - prop / fade);
        float curLevel = 100.f - 100.f * (float) Math.pow(grayLevel / 100.f, 3);
        boolean invert = invertParam.getValueb();
        int curColor = LXColor.gray(invert ? 100.f - curLevel : curLevel);
        int grayColor = LXColor.gray(invert ? 100.f - grayLevel : grayLevel);
        int offColor = invert ? 0xFFFFFFFF : 0xFF000000;

        for (FlowerPoint fp : flowerPoints) {
            colors[fp.index] =
                fp.direction == cur ? curColor : fp.direction == last ? grayColor : offColor;
        }
    }

    private static Direction step(Direction d) {
        switch (d) {
        case A: return Direction.B;
        case B: return Direction.C;
        case C: return Direction.A;
        }
        return d;
    }
}
