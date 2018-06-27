package com.symmetrylabs.slstudio.warp;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.warp.LXWarp;

public class Radiant extends LXWarp {
    private CompoundParameter radiusParam;
    private CompoundParameter widthParam;

    public Radiant(LX lx) {
        super(lx);

        float maxRadius = Inversion.getMaxRadius(model);
        radiusParam = new CompoundParameter("Radius", Math.sqrt(maxRadius), 1, maxRadius);
        widthParam = new CompoundParameter("Width", 0.1, 0, 1);
    }

    public boolean run(double deltaMs, boolean dirty) {
        if (dirty) {
            System.out.println("Recomputing Radiant warp (" + vectors.length + " vectors)...");
            float ox = model.cx;
            float oy = model.cy;
            float oz = model.cz;
            float rMid = radiusParam.getValuef();
            float rMin = rMid * (1 - widthParam.getValuef());
            float rMax = rMid * (1 + widthParam.getValuef());
            for (int i = 0; i < vectors.length; i++) {
                float dx = vectors[i].x - ox;
                float dy = vectors[i].y - oy;
                float dz = vectors[i].z - oz;
                float r = (float) Math.sqrt(dx * dx + dy * dy + dz * dz) + 1e-6f;
                float nr = rMin + (1f - 1f / (r / rMid + 1f)) * (rMax - rMin);
                warpedVectors[i].set(ox + dx * nr / r, oy + dy * nr / r, oz + dz * nr / r);
            }
        }
        return dirty;
    }
}
