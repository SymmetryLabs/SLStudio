package com.symmetrylabs.slstudio.warp;

import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.transform.LXProjection;
import heronarts.lx.transform.LXVector;
import heronarts.lx.warp.LXWarp;
import com.symmetrylabs.slstudio.model.SLModel;

public class SwapAxis extends SLWarp<SLModel> {
    private DiscreteParameter xmap = new DiscreteParameter("x", new String[] {"X", "Y", "Z"}, 0);
    private DiscreteParameter ymap = new DiscreteParameter("y", new String[] {"X", "Y", "Z"}, 1);
    private DiscreteParameter zmap = new DiscreteParameter("z", new String[] {"X", "Y", "Z"}, 2);
    private BooleanParameter normalize = new BooleanParameter("norm", true);

    public SwapAxis(LX lx) {
        super(lx);
        addParameter(xmap);
        addParameter(ymap);
        addParameter(zmap);
        addParameter(normalize);
    }

    public boolean run(double deltaMs, boolean inputVectorsChanged) {
        if (inputVectorsChanged || getAndClearParameterChangeDetectedFlag()) {
            System.out.println("Recomputing SwapAxis warp (" + inputVectors.length + " inputVectors)...");
            int x = xmap.getValuei();
            int y = ymap.getValuei();
            int z = zmap.getValuei();
            boolean n = normalize.getValueb();
            for (int i = 0; i < inputVectors.length; i++) {
                LXVector v = inputVectors[i];
                if (n) {
                    outputVectors[i].set(
                        model.xRange * getNormalized(v, x) + model.xMin,
                        model.yRange * getNormalized(v, y) + model.yMin,
                        model.zRange * getNormalized(v, z) + model.zMin);
                } else {
                    outputVectors[i].set(get(v, x), get(v, y), get(v, z));
                }
            }
            return true;
        }
        return false;
    }

    private float getNormalized(LXVector v, int i) {
        switch (i) {
        case 0: return (v.x - model.xMin) / model.xRange;
        case 1: return (v.y - model.yMin) / model.yRange;
        case 2: default: return (v.z - model.zMin) / model.zRange;
        }
    }

    private float get(LXVector v, int i) {
        switch (i) {
        case 0: return v.x;
        case 1: return v.y;
        case 2: default: return v.z;
        }
    }
}
