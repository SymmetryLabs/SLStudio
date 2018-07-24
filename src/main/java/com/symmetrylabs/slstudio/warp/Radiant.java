package com.symmetrylabs.slstudio.warp;

import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.SphereMarker;

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;
import heronarts.lx.warp.LXWarp;
import processing.core.PVector;

public class Radiant extends LXWarpWithMarkers {
    private CompoundParameter radiusParam;
    private CompoundParameter widthParam;
    private CompoundParameter cxParam = new CompoundParameter("cx", model.cx, model.xMin, model.xMax);
    private CompoundParameter cyParam = new CompoundParameter("cy", model.cy, model.yMin, model.yMax);
    private CompoundParameter czParam = new CompoundParameter("cz", model.cz, model.zMin, model.zMax);

    public Radiant(LX lx) {
        super(lx);

        float maxRadius = Inversion.getMaxRadius(model);
        radiusParam = new CompoundParameter("Radius", maxRadius/2, 1, maxRadius);
        addParameter(radiusParam);
        widthParam = new CompoundParameter("Width", 0.1, 0, 1);
        addParameter(widthParam);
        addParameter(cxParam);
        addParameter(cyParam);
        addParameter(czParam);
    }

    public boolean run(double deltaMs, boolean inputVectorsChanged) {
        if (inputVectorsChanged || getAndClearParameterChangeDetectedFlag()) {
            System.out.println("Recomputing Radiant warp (" + inputVectors.size() + " vectors)...");
            float ox = cxParam.getValuef();
            float oy = cyParam.getValuef();
            float oz = czParam.getValuef();
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

    @Override public List<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<>();
        markers.add(new SphereMarker(
            new PVector(cxParam.getValuef(), cyParam.getValuef(), czParam.getValuef()),
            radiusParam.getValuef() * (1 - widthParam.getValuef()),
            0x40ffff00
        ));
        markers.add(new SphereMarker(
            new PVector(cxParam.getValuef(), cyParam.getValuef(), czParam.getValuef()),
            radiusParam.getValuef() * (1 + widthParam.getValuef()),
            0x40ffc000
        ));
        return markers;
    }
}
