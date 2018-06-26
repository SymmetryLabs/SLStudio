package com.symmetrylabs.slstudio.warp;

import heronarts.lx.LX;
import heronarts.lx.warp.LXWarp;

public class Mirror extends LXWarp {
    public Mirror(LX lx) {
        super(lx);
    }

    @Override
    public boolean run(double deltaMs, boolean dirty) {
        if (dirty) {
            for (int i = 0; i < warpedVectors.length; i++) {
                warpedVectors[i].x = Math.abs(warpedVectors[i].x - model.cx) + model.cx;
            }
            return true;
        }
        return false;
    }
}
