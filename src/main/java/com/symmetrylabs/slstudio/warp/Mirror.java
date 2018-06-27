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
            System.out.println("Recomputing Mirror warp (" + vectors.length + " vectors)...");
            for (int i = 0; i < warpedVectors.length; i++) {
                warpedVectors[i].set(
                    Math.abs(vectors[i].x - model.cx) + model.cx,
                    vectors[i].y,
                    vectors[i].z
                );
            }
        }
        return dirty;
    }
}
