package com.symmetrylabs.slstudio.warp;

import com.symmetrylabs.slstudio.model.SLModel;
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
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;
import heronarts.lx.warp.LXWarp;
import processing.core.PVector;

public class Translate extends SLWarp<SLModel> {
    private static int RANGE_MACRO = 3;
    private float xrange = model.xRange*RANGE_MACRO;
    private float yrange = model.yRange*RANGE_MACRO;
    private float zrange = model.zRange*RANGE_MACRO;
    private CompoundParameter cxParam = new CompoundParameter("cx", 0, -xrange, xrange);
    private CompoundParameter cyParam = new CompoundParameter("cy", 0, -yrange, yrange);
    private CompoundParameter czParam = new CompoundParameter("cz", 0, -zrange, zrange);

    public Translate(LX lx) {
        super(lx);

        cxParam.setPolarity(LXParameter.Polarity.BIPOLAR);
        cyParam.setPolarity(LXParameter.Polarity.BIPOLAR);
        czParam.setPolarity(LXParameter.Polarity.BIPOLAR);
        addParameter(cxParam);
        addParameter(cyParam);
        addParameter(czParam);
    }

    public boolean run(double deltaMs, boolean inputVectorsChanged) {
        if (inputVectorsChanged || getAndClearParameterChangeDetectedFlag()) {
            System.out.println("Recomputing Translate warp (" + inputVectors.length + " inputVectors)...");
            float ox = cxParam.getValuef();
            float oy = cyParam.getValuef();
            float oz = czParam.getValuef();

            for (int i = 0; i < inputVectors.length; i++) {
                LXVector iv = inputVectors[i];
                if (iv == null) {
                    outputVectors[i] = null;
                } else {
                    LXVector ov = new LXVector(iv);
                    ov.set(ox + iv.x, oy + iv.y, oz + iv.z);
                    outputVectors[i] = ov;
                }
            }
            return true;
        }
        return false;
    }

    @Override public List<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<>();
        return markers;
    }
}
