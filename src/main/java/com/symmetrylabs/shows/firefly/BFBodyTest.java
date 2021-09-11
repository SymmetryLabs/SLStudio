package com.symmetrylabs.shows.firefly;

import art.lookingup.KaledoscopeModel;
import art.lookingup.LUButterfly;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;

public class BFBodyTest extends SLPattern {
    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;

    DiscreteParameter which = new DiscreteParameter("which", 0, 0, 4);
    int currentLed = 0;

    public BFBodyTest(LX lx) {
        super(lx);
        addParameter("which", which);
    }

    @Override
    protected void run(double deltaMs) {
        for (LXPoint p : model.points) {
            colors[p.index] = LXColor.rgb(0, 0, 0);
        }

        for (LUButterfly butterfly : KaledoscopeModel.allButterflies) {
            if (which.getValuei() < 3) {
                for (int i = 0; i < butterfly.allPoints.size(); i++) {
                    if (currentLed == i) {
                        LXPoint point;
                        if (which.getValuei() == 0)
                            point = butterfly.pointsClockwise.get(i);
                        else if (which.getValuei() == 1)
                            point = butterfly.pointsCounterClockwise.get(i);
                        else
                            point = butterfly.pointsByRow.get(i);
                        colors[point.index] = LXColor.rgb(255, 255, 255);
                    }
                }
            } else {
                for (int i = 0; i < butterfly.pointsByRow.size()/2; i++) {
                    if (currentLed/2 == i) {
                        LXPoint point = butterfly.pointsByRow.get(i * 2);
                        colors[point.index] = LXColor.rgb(255, 255, 255);
                        point = butterfly.pointsByRow.get(i * 2 + 1);
                        colors[point.index] = LXColor.rgb(255, 0, 0);
                    }
                }
            }
        }

        currentLed = (currentLed + 1) % 16;
    }
}
