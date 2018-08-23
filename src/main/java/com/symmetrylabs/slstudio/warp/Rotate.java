package com.symmetrylabs.slstudio.warp;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXProjection;
import heronarts.lx.transform.LXVector;
import heronarts.lx.warp.LXWarp;

public class Rotate extends LXWarp {
    private CompoundParameter cxParam = new CompoundParameter("cx", model.cx, model.xMin, model.xMax);
    private CompoundParameter cyParam = new CompoundParameter("cy", model.cy, model.yMin, model.yMax);
    private CompoundParameter czParam = new CompoundParameter("cz", model.cz, model.zMin, model.zMax);
    private CompoundParameter angle = new CompoundParameter("angle", 0, -4 * Math.PI, 4 * Math.PI);

    public Rotate(LX lx) {
        super(lx);

        addParameter(cxParam);
        addParameter(cyParam);
        addParameter(czParam);
        addParameter(angle);
    }

    public boolean run(double deltaMs, boolean inputVectorsChanged) {
        if (inputVectorsChanged || getAndClearParameterChangeDetectedFlag()) {
            System.out.println("Recomputing Rotate warp (" + inputVectors.length + " inputVectors)...");
            LXProjection proj = new LXProjection(inputVectors);
            float ox = cxParam.getValuef();
            float oy = cyParam.getValuef();
            float oz = czParam.getValuef();
            proj.translate(-ox, -oy, -oz);
            proj.rotateZ(angle.getValuef());
            proj.translate(ox, oy, oz);

            LXVector[] res = proj.getArray();
            for (int i = 0; i < res.length; i++) {
                outputVectors[i].set(res[i]);
            }
            return true;
        }
        return false;
    }
}
