package com.symmetrylabs.slstudio.warp;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.StripsTopology;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.warp.LXWarp;

public class BundleDirMask<T extends Strip> extends LXWarp {
    BooleanParameter maskX = new BooleanParameter("x", false);
    BooleanParameter maskY = new BooleanParameter("y", false);
    BooleanParameter maskZ = new BooleanParameter("z", false);
    StripsModel<T> stripModel;

    public BundleDirMask(LX lx) {
        super(lx);

        try {
            stripModel = (StripsModel<T>) lx.model;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("BundleDirMask only works with StripsModels");
        }

        addParameter(maskX);
        addParameter(maskY);
        addParameter(maskZ);
    }

    @Override
    protected boolean run(double deltaMs, boolean inputVectorsChanged) {
        if (inputVectorsChanged || getAndClearParameterChangeDetectedFlag()) {
            if (stripModel.getTopology() == null) {
                SLStudio.setWarning("BundleDirMask", "topology isn't present");
                return false;
            } else {
                SLStudio.setWarning("BundleDirMask", null);
            }
            boolean mx = maskX.getValueb();
            boolean my = maskY.getValueb();
            boolean mz = maskZ.getValueb();
            for (StripsTopology.Bundle b : stripModel.getTopology().bundles) {
                for (int sidx : b.strips) {
                    Strip s = stripModel.getStripByIndex(sidx);
                    for (LXPoint p : s.points) {
                        if (mx && b.dir == StripsTopology.Dir.X) {
                            outputVectors[p.index] = null;
                        } else if (my && b.dir == StripsTopology.Dir.Y) {
                            outputVectors[p.index] = null;
                        } else if (mz && b.dir == StripsTopology.Dir.Z) {
                            outputVectors[p.index] = null;
                        } else {
                            outputVectors[p.index] = inputVectors[p.index];
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }
}
