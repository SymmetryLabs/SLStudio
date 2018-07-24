package com.symmetrylabs.slstudio.warp;

import java.util.ArrayList;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;
import heronarts.lx.warp.LXWarp;

public class Radiant extends LXWarp {
    private CompoundParameter radiusParam;
    private CompoundParameter widthParam;
    private boolean parameterChanged = false;

    public Radiant(LX lx) {
        super(lx);

        float maxRadius = Inversion.getMaxRadius(model);
        radiusParam = new CompoundParameter("Radius", maxRadius/2, 1, maxRadius);
        addParameter(radiusParam);
        widthParam = new CompoundParameter("Width", 0.1, 0, 1);
        addParameter(widthParam);
    }

    public boolean run(double deltaMs, boolean inputVectorsChanged) {
        if (inputVectorsChanged || getAndClearParameterChangeDetectedFlag()) {
            parameterChanged = false;

            System.out.println("Recomputing Radiant warp (" + inputVectors.size() + " vectors)...");
            float ox = model.cx;
            float oy = model.cy;
            float oz = model.cz;
            float rMid = radiusParam.getValuef();
            float rMin = rMid * (1 - widthParam.getValuef());
            float rMax = rMid * (1 + widthParam.getValuef());
            outputVectors.clear();
            for (LXVector v : inputVectors) {
                float dx = v.x - ox;
                float dy = v.y - oy;
                float dz = v.z - oz;
                float r = (float) Math.sqrt(dx * dx + dy * dy + dz * dz) + 1e-6f;
                float nr = rMin + (1f - 1f / (r / rMid + 1f)) * (rMax - rMin);
                LXVector ov = new LXVector(v);  // sets ov.point and ov.index
                ov.set(ox + dx * nr / r, oy + dy * nr / r, oz + dz * nr / r);
                outputVectors.add(ov);
            }
            return true;
        }
        return false;
    }
}
