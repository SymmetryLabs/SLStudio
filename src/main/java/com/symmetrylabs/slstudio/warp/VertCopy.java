package com.symmetrylabs.slstudio.warp;

import com.symmetrylabs.util.CubeMarker;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.slstudio.model.SLModel;

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;
import heronarts.lx.warp.LXWarp;
import processing.core.PVector;

public class VertCopy extends SLWarp<SLModel> {

    public VertCopy(LX lx) {
        super(lx);
    }

    @Override
    public boolean run(double deltaMs, boolean inputVectorsChanged) {
        if (inputVectorsChanged || getAndClearParameterChangeDetectedFlag()) {
            System.out.println("Recomputing VertCopy warp (" + inputVectors.length + " vectors)...");
            

            for (int i = 0; i < inputVectors.length; i++) {
                LXVector iv = inputVectors[i];
                if (iv == null) {
                    outputVectors[i] = null;
                } else {
                    LXVector ov = new LXVector(iv);  // sets ov.point and ov.index
                    if (ov.y > model.cy) {
                        float diff = ov.y - model.cy;
                        ov.y = model.cy - diff;
                    }
                    outputVectors[i] = ov;
                }
            }
            return true;
        }
        return false;
    }
}
