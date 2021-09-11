package com.symmetrylabs.shows.firefly;

import art.lookingup.KaledoscopeModel;
import art.lookingup.LUButterfly;
import com.symmetrylabs.shows.firefly.BFBase;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

public class BFStrandHue extends BFBase {
    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;

    public BFStrandHue(LX lx) {
        super(lx);
    }

    protected void renderButterfly(double deltaMs, LUButterfly butterfly, int randomInt) {
        for (LXPoint p : butterfly.allPoints) {
            float hue = 360f * (float)butterfly.runIndex / (float) KaledoscopeModel.allRuns.get(0).butterflies.size();
            colors[p.index] = LXColor.hsb(hue, 100, 100);
        }
    }
}
