package com.symmetrylabs.slstudio.warp;

import heronarts.lx.LX;
import heronarts.lx.warp.LXWarp;

public class Radial extends LXWarp {
    public Radial(LX lx) {
        super(lx);
    }

    public boolean run(double deltaMs, boolean dirty) {
        if (dirty) {
            float ox = model.cx;
            float oy = model.cy;
            float oz = model.cz;
            float rMin = 90;
            float rMax = 110;
            float rMid = (rMin + rMax) / 2;
            for (int i = 0; i < vectors.length; i++) {
                float dx = vectors[i].x - ox;
                float dy = vectors[i].y - oy;
                float dz = vectors[i].z - oz;
                float r = (float) Math.sqrt(dx * dx + dy * dy + dz * dz) + 0.001f;
                float nr = rMin + (1f - 1f / (r / rMid + 1f)) * (rMax - rMin);
                warpedVectors[i].set(ox + dx * nr / r, oy + dy * nr / r, oz + dz * nr / r);
            }
        }
        return dirty;
    }
}
