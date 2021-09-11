package com.symmetrylabs.slstudio.pattern;

import art.lookingup.KaledoscopeModel;
import art.lookingup.LUButterfly;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

public class BFStrandHue extends BFBase {

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
