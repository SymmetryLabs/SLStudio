package com.symmetrylabs.slstudio.warp;

import com.symmetrylabs.util.CubeMarker;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.SphereMarker;

import java.util.ArrayList;
import java.util.List;

import javax.swing.border.CompoundBorder;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;
import heronarts.lx.warp.LXWarp;
import processing.core.PVector;

public class Inversion extends SLWarp {
    private CompoundParameter radiusParam;
    private CompoundParameter cxParam = new CompoundParameter("cx", model.cx, model.xMin, model.xMax);
    private CompoundParameter cyParam = new CompoundParameter("cy", model.cy, model.yMin, model.yMax);
    private CompoundParameter czParam = new CompoundParameter("cz", model.cz, model.zMin, model.zMax);

    public Inversion(LX lx) {
        super(lx);

        float maxRadius = getMaxRadius(model);
        radiusParam = new CompoundParameter("Radius", maxRadius/2, 1, maxRadius);
        addParameter(radiusParam);
        addParameter(cxParam);
        addParameter(cyParam);
        addParameter(czParam);
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
//            System.out.println("Recomputing Inversion warp (" + inputVectors.length + " inputVectors)...");
            float ox = cxParam.getValuef();
            float oy = cyParam.getValuef();
            float oz = czParam.getValuef();
            float radius = radiusParam.getValuef();
            float sqRadius = radius * radius;

            for (int i = 0; i < inputVectors.length; i++) {
                LXVector iv = inputVectors[i];
                if (iv == null) {
                    outputVectors[i] = null;
                } else {
                    LXVector ov = new LXVector(iv);
                    float dx = iv.x - ox;
                    float dy = iv.y - oy;
                    float dz = iv.z - oz;
                    float r = (float) Math.sqrt(dx*dx + dy*dy + dz*dz) + 1e-6f;
                    float nr = sqRadius/r;
                    ov.set(ox + dx*nr/r, oy + dy*nr/r, oz + dz*nr/r);
                    outputVectors[i] = ov;
                }
            }
            return true;
        }
        return false;
    }

    @Override public List<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<>();
        markers.add(new SphereMarker(
            new PVector(cxParam.getValuef(), cyParam.getValuef(), czParam.getValuef()),
            radiusParam.getValuef(),
            0x40ff00ff
        ));
        return markers;
    }
}
