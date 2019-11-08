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

public class Stretch extends SLWarp<SLModel> {
    private CompoundParameter xParam = new CompoundParameter("x", 1, 0.1f, 10.0f);
    private CompoundParameter yParam = new CompoundParameter("y", 1, 0.1f, 10.0f);
    private CompoundParameter zParam = new CompoundParameter("z", 1, 0.1f, 10.0f);


    public Stretch(LX lx) {
        super(lx);
        addParameter(xParam);
        addParameter(yParam);
        addParameter(zParam);
    }

    @Override
    public boolean run(double deltaMs, boolean inputVectorsChanged) {
        if (inputVectorsChanged || getAndClearParameterChangeDetectedFlag()) {
            System.out.println("Recomputing Stretch warp (" + inputVectors.length + " vectors)...");
            float x = xParam.getValuef(); 
            float y = yParam.getValuef(); 
            float z = zParam.getValuef(); 

            for (int i = 0; i < inputVectors.length; i++) {
                LXVector iv = inputVectors[i];
                if (iv == null) {
                    outputVectors[i] = null;
                } else {
                    LXVector ov = new LXVector(iv);  // sets ov.point and ov.index
                    ov.x -= (ov.x - model.cx) / x;
                    ov.y -= (ov.y - model.cy)  / y;
                    ov.z -= (ov.z - model.cz) / z;
                    outputVectors[i] = ov;
                }
            }
            return true;
        }
        return false;
    }
}
