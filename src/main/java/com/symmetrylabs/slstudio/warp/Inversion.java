package com.symmetrylabs.slstudio.warp;

import javax.swing.border.CompoundBorder;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.warp.LXWarp;

public class Inversion extends LXWarp {
    private CompoundParameter radiusParam;

    public Inversion(LX lx) {
        super(lx);

        float maxRadius = getMaxRadius(model);
        radiusParam = new CompoundParameter("Radius", Math.sqrt(maxRadius), 1, maxRadius);
    }

    public static float getMaxRadius(LXModel model) {
        float maxSqRadius = 0;
        for (LXPoint p : model.points) {
            float dx = p.x - model.cx;
            float dy = p.y - model.cy;
            float dz = p.z - model.cz;
            float sqRadius = dx * dx + dy * dy + dz * dz;
            if (sqRadius > maxSqRadius) {
                maxSqRadius = sqRadius;
            }
        }
        return (float) Math.sqrt(maxSqRadius);
    }

    public boolean run(double deltaMs, boolean dirty) {
        if (dirty) {
            System.out.println("Recomputing Inversion warp (" + vectors.length + " vectors)...");
            float ox = model.cx;
            float oy = model.cy;
            float oz = model.cz;
            float radius = radiusParam.getValuef();
            float sqRadius = radius * radius;
            for (int i = 0; i < vectors.length; i++) {
                float dx = vectors[i].x - ox;
                float dy = vectors[i].y - oy;
                float dz = vectors[i].z - oz;
                float r = (float) Math.sqrt(dx * dx + dy * dy + dz * dz) + 1e-6f;
                float nr = sqRadius / r;
                warpedVectors[i].set(ox + dx * nr / r, oy + dy * nr / r, oz + dz * nr / r);
            }
        }
        return dirty;
    }
}
