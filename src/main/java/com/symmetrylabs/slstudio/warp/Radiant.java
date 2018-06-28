package com.symmetrylabs.slstudio.warp;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
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

    public void onParameterChanged(LXParameter param) {
        super.onParameterChanged(param);
        parameterChanged = true;
    }

    public boolean run(double deltaMs, boolean inputVectorsChanged) {
        if (inputVectorsChanged || parameterChanged) {
            parameterChanged = false;

            System.out.println("Recomputing Radiant warp (" + inputVectors.length + " vectors)...");
            float ox = model.cx;
            float oy = model.cy;
            float oz = model.cz;
            float rMid = radiusParam.getValuef();
            float rMin = rMid * (1 - widthParam.getValuef());
            float rMax = rMid * (1 + widthParam.getValuef());
            for (int i = 0; i < inputVectors.length; i++) {
                float dx = inputVectors[i].x - ox;
                float dy = inputVectors[i].y - oy;
                float dz = inputVectors[i].z - oz;
                float r = (float) Math.sqrt(dx * dx + dy * dy + dz * dz) + 1e-6f;
                float nr = rMin + (1f - 1f / (r / rMid + 1f)) * (rMax - rMin);
                outputVectors[i].set(ox + dx * nr / r, oy + dy * nr / r, oz + dz * nr / r);
            }
            return true;
        }
        return false;
    }
}
