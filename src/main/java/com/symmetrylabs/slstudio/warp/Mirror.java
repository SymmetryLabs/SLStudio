package com.symmetrylabs.slstudio.warp;

import heronarts.lx.LX;
import heronarts.lx.warp.LXWarp;

public class Mirror extends LXWarp {
    public Mirror(LX lx) {
        super(lx);
    }

    @Override
    public boolean run(double deltaMs, boolean inputVectorsChanged) {
        if (inputVectorsChanged) {
            System.out.println("Recomputing Mirror warp (" + inputVectors.length + " vectors)...");
            for (int i = 0; i < outputVectors.length; i++) {
                outputVectors[i].set(
                    Math.abs(inputVectors[i].x - model.cx) + model.cx,
                    inputVectors[i].y,
                    inputVectors[i].z
                );
            }
            return true;
        }
        return false;
    }
}
