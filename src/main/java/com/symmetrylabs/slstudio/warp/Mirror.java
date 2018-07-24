package com.symmetrylabs.slstudio.warp;

import java.util.ArrayList;

import heronarts.lx.LX;
import heronarts.lx.transform.LXVector;
import heronarts.lx.warp.LXWarp;

public class Mirror extends LXWarp {
    public Mirror(LX lx) {
        super(lx);
    }

    @Override
    public boolean run(double deltaMs, boolean inputVectorsChanged) {
        if (inputVectorsChanged) {
            System.out.println("Recomputing Mirror warp (" + inputVectors.size() + " vectors)...");
            outputVectors.clear();
            for (LXVector v : inputVectors) {
                LXVector ov = new LXVector(v);  // sets ov.point and ov.index
                ov.set(Math.abs(v.x - model.cx) + model.cx, v.y, v.z);
                outputVectors.add(ov);
            }
            return true;
        }
        return false;
    }
}
