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

public class BoxMask extends LXWarpWithMarkers {
    private CompoundParameter xSizeParam = new CompoundParameter("xSize", model.xRange/2, 0, model.xRange);
    private CompoundParameter cxParam = new CompoundParameter("cx", model.cx, model.xMin, model.xMax);
    private CompoundParameter ySizeParam = new CompoundParameter("ySize", model.yRange/2, 0, model.yRange);
    private CompoundParameter cyParam = new CompoundParameter("cy", model.cy, model.yMin, model.yMax);
    private CompoundParameter zSizeParam = new CompoundParameter("zSize", model.zRange/2, 0, model.zRange);
    private CompoundParameter czParam = new CompoundParameter("cz", model.cz, model.zMin, model.zMax);

    public BoxMask(LX lx) {
        super(lx);
        addParameter(xSizeParam);
        addParameter(cxParam);
        addParameter(ySizeParam);
        addParameter(cyParam);
        addParameter(zSizeParam);
        addParameter(czParam);
    }

    @Override
    public boolean run(double deltaMs, boolean inputVectorsChanged) {
        if (inputVectorsChanged || getAndClearParameterChangeDetectedFlag()) {
            System.out.println("Recomputing BoxMask warp (" + inputVectors.length + " vectors)...");

            float xSize = xSizeParam.getValuef();
            float cx = cxParam.getValuef();
            float ySize = ySizeParam.getValuef();
            float cy = cyParam.getValuef();
            float zSize = zSizeParam.getValuef();
            float cz = czParam.getValuef();

            for (int i = 0; i < inputVectors.length; i++)  {
                LXVector iv = inputVectors[i];
                if (Math.abs(iv.x - cx) < xSize &&
                      Math.abs(iv.y - cy) < ySize &&
                      Math.abs(iv.z - cz) < zSize) {
                    outputVectors[i] = inputVectors[i];
                } else {
                    outputVectors[i] = null;
                }
            }
            return true;
        }
        return false;
    }

    @Override public List<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<>();
        markers.add(new CubeMarker(
            new PVector(cxParam.getValuef(), cyParam.getValuef(), czParam.getValuef()),
            new PVector(xSizeParam.getValuef(), ySizeParam.getValuef(), zSizeParam.getValuef()),
            0x40ffffff
        ));
        return markers;
    }
}
