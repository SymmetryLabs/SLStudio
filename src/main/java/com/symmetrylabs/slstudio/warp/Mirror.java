package com.symmetrylabs.slstudio.warp;

import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;
import heronarts.lx.warp.LXWarp;

public class Mirror extends LXWarp {
    private BooleanParameter xParam = new BooleanParameter("x", false);
    private CompoundParameter cxParam = new CompoundParameter("cx", model.cx, model.xMin, model.xMax);
    private BooleanParameter yParam = new BooleanParameter("y", false);
    private CompoundParameter cyParam = new CompoundParameter("cy", model.cy, model.yMin, model.yMax);
    private BooleanParameter zParam = new BooleanParameter("z", false);
    private CompoundParameter czParam = new CompoundParameter("cz", model.cz, model.zMin, model.zMax);

    public Mirror(LX lx) {
        super(lx);
        addParameter(xParam);
        addParameter(cxParam);
        addParameter(yParam);
        addParameter(cyParam);
        addParameter(zParam);
        addParameter(czParam);
    }

    @Override
    public boolean run(double deltaMs, boolean inputVectorsChanged) {
        if (inputVectorsChanged || getAndClearParameterChangeDetectedFlag()) {
            System.out.println("Recomputing Mirror warp (" + inputVectors.size() + " vectors)...");
            outputVectors.clear();

            boolean x = xParam.getValueb();
            float cx = cxParam.getValuef();
            boolean y = yParam.getValueb();
            float cy = cyParam.getValuef();
            boolean z = zParam.getValueb();
            float cz = czParam.getValuef();
            for (LXVector v : inputVectors) {
                LXVector ov = new LXVector(v);  // sets ov.point and ov.index
                if (x) ov.x = Math.abs(v.x - cx) + cx;
                if (y) ov.y = Math.abs(v.y - cy) + cy;
                if (z) ov.z = Math.abs(v.z - cz) + cz;
                outputVectors.add(ov);
            }
            return true;
        }
        return false;
    }
}
