package com.symmetrylabs.shows.firefly;

import art.lookingup.LUButterfly;
import com.symmetrylabs.shows.firefly.BFBase;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

public class BFHueFlies extends BFBase {

    public BFHueFlies(LX lx) {
        super(lx);
    }
    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;

    protected void renderButterfly(double drawDeltaMs, LUButterfly butterfly, int randomInt) {
        for (LXPoint p : butterfly.allPoints) {
            float hue = 360 * (float)(randomInt)/1000f;
            colors[p.index] = LXColor.hsb(hue, 100, 100);
        }
    }
}
