package com.symmetrylabs.slstudio.warp;

import java.util.ArrayList;

import javax.swing.border.CompoundBorder;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;
import heronarts.lx.warp.LXWarp;

public class Inversion extends LXWarp {
    private CompoundParameter radiusParam;

    public Inversion(LX lx) {
        super(lx);

        float maxRadius = getMaxRadius(model);
        radiusParam = new CompoundParameter("Radius", maxRadius/2, 1, maxRadius);
        addParameter(radiusParam);
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

    public boolean run(double deltaMs, boolean inputVectorsChanged) {
        if (inputVectorsChanged || getAndClearParameterChangeDetectedFlag()) {
            System.out.println("Recomputing Inversion warp (" + inputVectors.size() + " inputVectors)...");
            float ox = model.cx;
            float oy = model.cy;
            float oz = model.cz;
            float radius = radiusParam.getValuef();
            float sqRadius = radius * radius;

            outputVectors.clear();
            for (LXVector v : inputVectors) {
                float dx = v.x - ox;
                float dy = v.y - oy;
                float dz = v.z - oz;
                float r = (float) Math.sqrt(dx * dx + dy * dy + dz * dz) + 1e-6f;
                float nr = sqRadius / r;
                LXVector ov = new LXVector(v);  // sets ov.point and ov.index
                ov.set(ox + dx * nr / r, oy + dy * nr / r, oz + dz * nr / r);
                outputVectors.add(ov);
            }
            return true;
        }
        return false;
    }
}
