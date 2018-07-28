package com.symmetrylabs.slstudio.warp;

import com.symmetrylabs.util.CubeMarker;
import com.symmetrylabs.util.Marker;

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;
import heronarts.lx.warp.LXWarp;
import processing.core.PVector;

public class Mirror extends LXWarpWithMarkers {
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
            System.out.println("Recomputing Mirror warp (" + inputVectors.length + " vectors)...");
            boolean x = xParam.getValueb();
            float cx = cxParam.getValuef();
            boolean y = yParam.getValueb();
            float cy = cyParam.getValuef();
            boolean z = zParam.getValueb();
            float cz = czParam.getValuef();

            for (int i = 0; i < inputVectors.length; i++) {
                LXVector iv = inputVectors[i];
                if (iv == null) {
                    outputVectors[i] = null;
                } else {
                    LXVector ov = new LXVector(iv);  // sets ov.point and ov.index
                    if (x) ov.x = Math.abs(iv.x - cx) + cx;
                    if (y) ov.y = Math.abs(iv.y - cy) + cy;
                    if (z) ov.z = Math.abs(iv.z - cz) + cz;
                    outputVectors[i] = ov;
                }
            }
            return true;
        }
        return false;
    }

    @Override public List<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<>();
        if (xParam.getValueb()) {
            markers.add(new CubeMarker(
                new PVector(cxParam.getValuef(), model.cy, model.cz),
                new PVector(0, model.yRange/2, model.zRange/2),
                0x4000ffff
            ));
        }
        if (yParam.getValueb()) {
            markers.add(new CubeMarker(
                new PVector(model.cx, cyParam.getValuef(), model.cz),
                new PVector(model.xRange/2, 0, model.zRange/2),
                0x4000ffff
            ));
        }
        if (zParam.getValueb()) {
            markers.add(new CubeMarker(
                new PVector(model.cx, model.cy, czParam.getValuef()),
                new PVector(model.xRange/2, model.yRange/2, 0),
                0x4000ffff
            ));
        }
        return markers;
    }
}
